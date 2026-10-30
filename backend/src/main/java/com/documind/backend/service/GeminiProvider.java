package com.documind.backend.service;

import com.documind.backend.exception.ServiceIaIndisponibleException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.util.List;

@Service
@ConditionalOnProperty(name = "app.llm.provider", havingValue = "gemini")
public class GeminiProvider implements LlmProvider {

    private static final int MAX_TENTATIVES = 3;

    private final RestClient client;
    private final String modele;

    public GeminiProvider(
            @Value("${app.llm.gemini.url:https://generativelanguage.googleapis.com}") String url,
            @Value("${gemini.api-key}") String cle,
            @Value("${app.llm.gemini.model:gemini-3.1-flash-lite}") String modele) {

        // on n'attend pas indéfiniment une réponse
        SimpleClientHttpRequestFactory fabrique = new SimpleClientHttpRequestFactory();
        fabrique.setConnectTimeout(Duration.ofSeconds(10));
        fabrique.setReadTimeout(Duration.ofSeconds(60));

        this.client = RestClient.builder()
                .baseUrl(url)
                .requestFactory(fabrique)
                .defaultHeader("x-goog-api-key", cle)
                .build();
        this.modele = modele;
    }

    // ce qu'on envoie : {"systemInstruction": {...}, "contents": [...], "generationConfig": {...}}
    record Part(String text) {
    }

    record Instruction(List<Part> parts) {
    }

    record Content(String role, List<Part> parts) {
    }

    record GenerationConfig(double temperature) {
    }

    record GenerateRequest(Instruction systemInstruction,
                           List<Content> contents,
                           GenerationConfig generationConfig) {
    }

    // ce que Gemini répond : {"candidates": [{"content": {"parts": [{"text": "..."}]}}]}
    record Candidate(Content content) {
    }

    record GenerateResponse(List<Candidate> candidates) {
    }

    @Override
    public String generer(String consigne, String question) {
        for (int tentative = 1; ; tentative++) {
            try {
                return appeler(consigne, question);
            } catch (RestClientResponseException e) {
                int code = e.getStatusCode().value();
                // 429 = trop de requêtes, 503 = service surchargé : on réessaie après une pause
                boolean temporaire = code == 429 || code == 503;
                if (temporaire && tentative < MAX_TENTATIVES) {
                    pause(2000L * tentative);
                    continue;
                }
                throw traduire(code, e);
            } catch (RestClientException e) {
                throw new ServiceIaIndisponibleException("Le service d'IA est injoignable", e);
            }
        }
    }

    private String appeler(String consigne, String question) {
        GenerateRequest requete = new GenerateRequest(
                new Instruction(List.of(new Part(consigne))),
                List.of(new Content("user", List.of(new Part(question)))),
                new GenerationConfig(0.2)); // bas = réponses plus fidèles aux documents

        GenerateResponse reponse = client.post()
                .uri("/v1beta/models/{modele}:generateContent", modele)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requete)
                .retrieve()
                .body(GenerateResponse.class);

        String texte = extraireTexte(reponse);
        if (texte.isBlank()) {
            throw new ServiceIaIndisponibleException(
                    "L'IA n'a pas pu produire de réponse (contenu vide ou bloqué)");
        }
        return texte.strip();
    }

    // la réponse peut être découpée en plusieurs "parts" : on les assemble
    private String extraireTexte(GenerateResponse reponse) {
        if (reponse == null || reponse.candidates() == null || reponse.candidates().isEmpty()) {
            return "";
        }
        Content contenu = reponse.candidates().get(0).content();
        if (contenu == null || contenu.parts() == null) {
            return "";
        }
        StringBuilder texte = new StringBuilder();
        for (Part part : contenu.parts()) {
            if (part.text() != null) {
                texte.append(part.text());
            }
        }
        return texte.toString();
    }

    private ServiceIaIndisponibleException traduire(int code, Exception cause) {
        return switch (code) {
            case 400, 401, 403 -> new ServiceIaIndisponibleException(
                    "Clé API Gemini invalide ou non autorisée", cause);
            case 404 -> new ServiceIaIndisponibleException(
                    "Modèle Gemini '" + modele + "' introuvable", cause);
            case 429 -> new ServiceIaIndisponibleException(
                    "Limite d'utilisation de l'IA atteinte, réessaie dans un instant", cause);
            case 503 -> new ServiceIaIndisponibleException(
                    "Le service d'IA est surchargé, réessaie dans un instant", cause);
            default -> new ServiceIaIndisponibleException(
                    "Le service d'IA a renvoyé une erreur (" + code + ")", cause);
        };
    }

    private void pause(long millisecondes) {
        try {
            Thread.sleep(millisecondes);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServiceIaIndisponibleException("Appel à l'IA interrompu");
        }
    }
}