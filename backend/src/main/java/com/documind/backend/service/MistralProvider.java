package com.documind.backend.service;

import com.documind.backend.exception.ServiceIaIndisponibleException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.time.Duration;
import java.util.List;

@Service
@ConditionalOnProperty(name = "app.llm.provider", havingValue = "mistral")
public class MistralProvider implements LlmProvider {

    private final RestClient client;
    private final String modele;

    public MistralProvider(
            @Value("${app.llm.url:https://api.mistral.ai}") String url,
            @Value("${mistral.api-key}") String cle,
            @Value("${app.llm.model:mistral-small-latest}") String modele) {

        // on n'attend pas indéfiniment une réponse
        SimpleClientHttpRequestFactory fabrique = new SimpleClientHttpRequestFactory();
        fabrique.setConnectTimeout(Duration.ofSeconds(10));
        fabrique.setReadTimeout(Duration.ofSeconds(60));

        this.client = RestClient.builder()
                .baseUrl(url)
                .requestFactory(fabrique)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + cle)
                .build();
        this.modele = modele;
    }

    // ce qu'on envoie : le modèle + la liste des messages
    record Message(String role, String content) {
    }

    record ChatRequest(String model, List<Message> messages, double temperature) {
    }

    // ce que Mistral répond : {"choices": [{"message": {"content": "..."}}]}
    record Choice(Message message) {
    }

    record ChatResponse(List<Choice> choices) {
    }

    @Override
    public String generer(String consigne, String question) {
        try {
            ChatRequest requete = new ChatRequest(
                    modele,
                    List.of(new Message("system", consigne), new Message("user", question)),
                    0.2); // bas = réponses plus fidèles aux documents, moins créatives

            ChatResponse reponse = client.post()
                    .uri("/v1/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requete)
                    .retrieve()
                    .body(ChatResponse.class);

            if (reponse == null || reponse.choices() == null || reponse.choices().isEmpty()
                    || reponse.choices().get(0).message() == null
                    || reponse.choices().get(0).message().content() == null) {
                throw new ServiceIaIndisponibleException("Réponse vide du service d'IA");
            }
            return reponse.choices().get(0).message().content().strip();

        } catch (RestClientResponseException e) {
            int code = e.getStatusCode().value();
            if (code == 401) {
                throw new ServiceIaIndisponibleException("Clé API Mistral invalide", e);
            }
            if (code == 429) {
                throw new ServiceIaIndisponibleException(
                        "Limite d'utilisation de l'IA atteinte, réessaie dans un instant", e);
            }
            throw new ServiceIaIndisponibleException("Le service d'IA a renvoyé une erreur (" + code + ")", e);
        } catch (RestClientException e) {
            throw new ServiceIaIndisponibleException("Le service d'IA est injoignable", e);
        }
    }
}