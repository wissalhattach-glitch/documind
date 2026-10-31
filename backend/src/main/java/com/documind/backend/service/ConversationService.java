package com.documind.backend.service;

import com.documind.backend.dto.ConversationDetail;
import com.documind.backend.dto.ConversationResume;
import com.documind.backend.dto.CreerConversationRequest;
import com.documind.backend.dto.DocumentResume;
import com.documind.backend.dto.MessageResponse;
import com.documind.backend.dto.ReponseRag;
import com.documind.backend.dto.SourceHistorique;
import com.documind.backend.dto.SourceReponse;
import com.documind.backend.entity.Conversation;
import com.documind.backend.entity.Document;
import com.documind.backend.entity.DocumentChunk;
import com.documind.backend.entity.DocumentStatus;
import com.documind.backend.entity.Message;
import com.documind.backend.entity.Role;
import com.documind.backend.entity.SourceReference;
import com.documind.backend.exception.ConversationIntrouvableException;
import com.documind.backend.exception.DocumentsInvalidesException;
import com.documind.backend.repository.ConversationRepository;
import com.documind.backend.repository.DocumentChunkRepository;
import com.documind.backend.repository.DocumentRepository;
import com.documind.backend.repository.MessageRepository;
import com.documind.backend.repository.SourceReferenceRepository;
import com.documind.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private static final String TITRE_PAR_DEFAUT = "Nouvelle conversation";

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final SourceReferenceRepository sourceRepository;
    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository chunkRepository;
    private final UserRepository userRepository;
    private final RagService ragService;
    private final TransactionTemplate transaction;

    @Transactional
    public ConversationDetail creer(CreerConversationRequest requete, Long userId) {
        List<Long> ids = requete.documentIds() == null
                ? List.of()
                : requete.documentIds().stream().distinct().toList();

        Set<Document> documents = new HashSet<>();
        if (!ids.isEmpty()) {
            // seulement des documents de cet utilisateur, et prêts à être interrogés
            List<Document> trouves = documentRepository
                    .findByIdInAndUserIdAndStatut(ids, userId, DocumentStatus.READY);
            if (trouves.size() != ids.size()) {
                throw new DocumentsInvalidesException();
            }
            documents.addAll(trouves);
        }

        String titre = (requete.titre() == null || requete.titre().isBlank())
                ? TITRE_PAR_DEFAUT
                : requete.titre().strip();

        Conversation conversation = conversationRepository.save(Conversation.builder()
                .user(userRepository.getReferenceById(userId))
                .titre(titre)
                .documents(documents)
                .build());

        return versDetail(conversation, List.of(), Map.of());
    }

    @Transactional(readOnly = true)
    public Page<ConversationResume> lister(Long userId, Pageable pageable) {
        return conversationRepository.findByUserId(userId, pageable).map(ConversationResume::de);
    }

    @Transactional(readOnly = true)
    public ConversationDetail detail(Long id, Long userId) {
        Conversation conversation = conversationRepository.findWithDocumentsByIdAndUserId(id, userId)
                .orElseThrow(ConversationIntrouvableException::new);

        List<Message> messages = messageRepository.findByConversationIdOrderByIdAsc(id);
        Map<Long, List<SourceReference>> sources = messages.isEmpty()
                ? Map.of()
                : sourceRepository.findByMessageIdIn(messages.stream().map(Message::getId).toList())
                        .stream()
                        .collect(Collectors.groupingBy(s -> s.getMessage().getId()));

        return versDetail(conversation, messages, sources);
    }

    @Transactional
    public ConversationResume renommer(Long id, String titre, Long userId) {
        Conversation conversation = conversationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(ConversationIntrouvableException::new);
        conversation.renommer(titre.strip());
        return ConversationResume.de(conversation);
    }

    @Transactional
    public void supprimer(Long id, Long userId) {
        Conversation conversation = conversationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(ConversationIntrouvableException::new);
        conversationRepository.delete(conversation);
    }

    // pas de @Transactional ici : la base n'est pas bloquée pendant que l'IA répond
    public ReponseRag poserQuestion(Long conversationId, String question, Long userId) {
        Conversation conversation = conversationRepository
                .findWithDocumentsByIdAndUserId(conversationId, userId)
                .orElseThrow(ConversationIntrouvableException::new);

        // aucun document choisi = on cherche dans tous les documents de l'utilisateur
        List<Long> documentIds = conversation.getDocuments().stream().map(Document::getId).toList();
        List<Echange> historique = chargerHistorique(conversationId);

        ReponseRag reponse = ragService.repondre(question, userId, documentIds, historique);

        // on n'enregistre qu'une fois la réponse reçue : si l'IA échoue, rien n'est sauvegardé
        transaction.executeWithoutResult(status -> enregistrer(conversationId, question, reponse));
        return reponse;
    }

    private List<Echange> chargerHistorique(Long conversationId) {
        List<Message> recents = new ArrayList<>(
                messageRepository.findTop10ByConversationIdOrderByIdDesc(conversationId));
        Collections.reverse(recents); // du plus ancien au plus récent
        return recents.stream()
                .map(m -> new Echange(m.getRole() == Role.USER ? "Utilisateur" : "Assistant", m.getContenu()))
                .toList();
    }

    private void enregistrer(Long conversationId, String question, ReponseRag reponse) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(ConversationIntrouvableException::new);

        messageRepository.save(Message.builder()
                .conversation(conversation)
                .role(Role.USER)
                .contenu(question.strip())
                .build());

        Message assistant = messageRepository.save(Message.builder()
                .conversation(conversation)
                .role(Role.ASSISTANT)
                .contenu(reponse.reponse())
                .build());

        // les passages ont pu être supprimés entre-temps (réindexation) : on ne lie que ceux qui existent
        List<Long> chunkIds = reponse.sources().stream().map(SourceReponse::chunkId).toList();
        Map<Long, DocumentChunk> chunks = chunkRepository.findAllById(chunkIds).stream()
                .collect(Collectors.toMap(DocumentChunk::getId, Function.identity()));

        List<SourceReference> sources = reponse.sources().stream()
                .map(s -> SourceReference.builder()
                        .message(assistant)
                        .chunk(chunks.get(s.chunkId()))
                        .nomDocument(s.nomDocument())
                        .numeroPage(s.numeroPage())
                        .extrait(s.extrait())
                        .scoreSimilarite(s.score())
                        .build())
                .toList();
        sourceRepository.saveAll(sources);

        // la première question donne son titre à la conversation
        if (TITRE_PAR_DEFAUT.equals(conversation.getTitre())) {
            conversation.renommer(titreDepuis(question));
        }
    }

    private String titreDepuis(String question) {
        String propre = question.strip().replaceAll("\\s+", " ");
        return propre.length() > 60 ? propre.substring(0, 60) + "…" : propre;
    }

    private ConversationDetail versDetail(Conversation conversation, List<Message> messages,
                                          Map<Long, List<SourceReference>> sourcesParMessage) {
        List<DocumentResume> documents = conversation.getDocuments().stream()
                .sorted(Comparator.comparing(Document::getId))
                .map(d -> new DocumentResume(d.getId(), d.getNomFichier()))
                .toList();

        List<MessageResponse> reponses = messages.stream()
                .map(m -> new MessageResponse(
                        m.getId(),
                        m.getRole(),
                        m.getContenu(),
                        m.getDateCreation(),
                        sourcesParMessage.getOrDefault(m.getId(), List.of()).stream()
                                .sorted(Comparator.comparing(SourceReference::getId))
                                .map(s -> new SourceHistorique(s.getNomDocument(), s.getNumeroPage(),
                                        s.getExtrait(), s.getScoreSimilarite()))
                                .toList()))
                .toList();

        return new ConversationDetail(conversation.getId(), conversation.getTitre(),
                conversation.getDateCreation(), documents, reponses);
    }
}