package com.documind.backend.service;

import com.documind.backend.exception.ServiceIaIndisponibleException;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeminiProviderTest {

    private static final String URL = "https://generativelanguage.googleapis.com";
    private static final String MODELE = "gemini-3.1-flash-lite";

    // ne demande aucune vraie clé : une fausse clé doit donner une erreur claire
    @Test
    void refuseUneCleInvalide() {
        GeminiProvider provider = new GeminiProvider(URL, "fausse-cle", MODELE);

        ServiceIaIndisponibleException erreur = assertThrows(
                ServiceIaIndisponibleException.class,
                () -> provider.generer("Tu réponds en un mot.", "Bonjour"));

        assertTrue(erreur.getMessage().contains("Clé API Gemini"));
    }

    @Test
    void repondAUneQuestionSimple() {
        String cle = lireCle();
        // sans clé, le test est ignoré (pas en échec)
        Assumptions.assumeTrue(cle != null && !cle.isBlank(), "GEMINI_API_KEY absente");

        GeminiProvider provider = new GeminiProvider(URL, cle, MODELE);

        String reponse = provider.generer(
                "Tu réponds en un seul mot, sans phrase.",
                "Quelle est la capitale du Maroc ?");
        System.out.println("Réponse de Gemini : " + reponse);

        assertTrue(reponse.toLowerCase().contains("rabat"));
    }

    // simule le RAG : l'IA doit répondre à partir de l'extrait fourni
    @Test
    void repondEnSeBasantSurUnExtrait() {
        String cle = lireCle();
        Assumptions.assumeTrue(cle != null && !cle.isBlank(), "GEMINI_API_KEY absente");

        GeminiProvider provider = new GeminiProvider(URL, cle, MODELE);

        String consigne = "Réponds uniquement à partir de l'extrait fourni, en une phrase courte.";
        String question = """
                Extrait (page 1) : Ce TP porte sur la classification intelligente des déchets
                avec Hugging Face.

                Question : Quel est le sujet du TP ?
                """;

        String reponse = provider.generer(consigne, question);
        System.out.println("Réponse de Gemini : " + reponse);

        assertTrue(reponse.toLowerCase().contains("déchets"));
    }

    // cherche la clé dans l'environnement, sinon dans le fichier .env à la racine du projet
    private String lireCle() {
        String cle = System.getenv("GEMINI_API_KEY");
        if (cle != null && !cle.isBlank()) {
            return cle;
        }
        try {
            for (String ligne : Files.readAllLines(Path.of("../.env"))) {
                if (ligne.startsWith("GEMINI_API_KEY=")) {
                    return ligne.substring("GEMINI_API_KEY=".length()).strip();
                }
            }
        } catch (IOException e) {
            // pas de .env lisible : le test s'ignorera
        }
        return null;
    }
}