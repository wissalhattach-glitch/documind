package com.documind.backend.service;

import com.documind.backend.exception.ServiceIaIndisponibleException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.List;

@Service
public class OllamaEmbeddingProvider implements EmbeddingProvider {

    // bge-m3 n'a pas besoin de préfixe (nomic-embed-text, lui, en demandait)
    private static final String PREFIXE_DOCUMENT = "";
    private static final String PREFIXE_QUESTION = "";

    private final RestClient client;
    private final String modele;

    public OllamaEmbeddingProvider(
            @Value("${app.ollama.url:http://localhost:11434}") String url,
            @Value("${app.embedding.model:bge-m3}") String modele) {

        // on n'attend pas indéfiniment Ollama : après 2 minutes sans réponse, l'appel échoue
        SimpleClientHttpRequestFactory fabrique = new SimpleClientHttpRequestFactory();
        fabrique.setConnectTimeout(Duration.ofSeconds(10));
        fabrique.setReadTimeout(Duration.ofSeconds(120));

        this.client = RestClient.builder().baseUrl(url).requestFactory(fabrique).build();
        this.modele = modele;
    }

    // ce qu'on envoie à Ollama : {"model": "...", "input": ["texte1", "texte2"]}
    record EmbedRequest(String model, List<String> input) {
    }

    // ce qu'Ollama répond : {"embeddings": [[0.1, ...], [0.3, ...]]}
    record EmbedResponse(List<float[]> embeddings) {
    }

    @Override
    public List<float[]> embedderDocuments(List<String> textes) {
        List<String> prepares = textes.stream().map(t -> PREFIXE_DOCUMENT + t).toList();
        return appeler(prepares);
    }

    @Override
    public float[] embedderQuestion(String question) {
        return appeler(List.of(PREFIXE_QUESTION + question)).get(0);
    }

    @Override
    public String nomModele() {
        return modele;
    }

    private List<float[]> appeler(List<String> textes) {
        try {
            EmbedResponse reponse = client.post()
                    .uri("/api/embed")
                    .body(new EmbedRequest(modele, textes))
                    .retrieve()
                    .body(EmbedResponse.class);

            if (reponse == null || reponse.embeddings() == null
                    || reponse.embeddings().size() != textes.size()) {
                throw new IllegalStateException("Réponse d'embedding invalide");
            }
            return reponse.embeddings();
        } catch (RestClientException e) {
            throw new ServiceIaIndisponibleException(
                    "Ollama est injoignable ou le modèle '" + modele + "' n'est pas installé", e);
        }
    }
}