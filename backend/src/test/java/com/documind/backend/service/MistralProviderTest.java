package com.documind.backend.service;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MistralProviderTest {

    @Test
    void repondAUneQuestionSimple() {
        String cle = lireCle();
        // sans clé, le test est ignoré (pas en échec)
        Assumptions.assumeTrue(cle != null && !cle.isBlank(), "MISTRAL_API_KEY absente");

        MistralProvider provider = new MistralProvider(
                "https://api.mistral.ai", cle, "mistral-small-latest");

        String reponse = provider.generer(
                "Tu réponds en un seul mot, sans phrase.",
                "Quelle est la capitale du Maroc ?");
        System.out.println("Réponse de Mistral : " + reponse);

        assertTrue(reponse.toLowerCase().contains("rabat"));
    }

    // cherche la clé dans l'environnement, sinon dans le fichier .env à la racine du projet
    private String lireCle() {
        String cle = System.getenv("MISTRAL_API_KEY");
        if (cle != null && !cle.isBlank()) {
            return cle;
        }
        try {
            for (String ligne : Files.readAllLines(Path.of("../.env"))) {
                if (ligne.startsWith("MISTRAL_API_KEY=")) {
                    return ligne.substring("MISTRAL_API_KEY=".length()).strip();
                }
            }
        } catch (IOException e) {
            // pas de .env lisible : on laisse le test s'ignorer
        }
        return null;
    }
}