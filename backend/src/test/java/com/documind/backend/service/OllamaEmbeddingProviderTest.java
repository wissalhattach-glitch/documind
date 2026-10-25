package com.documind.backend.service;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OllamaEmbeddingProviderTest {

    private final OllamaEmbeddingProvider provider =
            new OllamaEmbeddingProvider("http://localhost:11434", "bge-m3");

    @Test
    void textesDeMemeSensSontPlusProchesQueTextesDifferents() {
        // si Ollama n'est pas lancé, le test est ignoré (pas en échec)
        Assumptions.assumeTrue(ollamaRepond(), "Ollama n'est pas lancé");

        List<float[]> v = provider.embedderDocuments(List.of(
                "Le chat dort sur le canapé",
                "Un félin se repose sur le sofa",
                "La bourse de Paris a chuté hier"));

        assertEquals(1024, v.get(0).length);

        double proche = cosinus(v.get(0), v.get(1));
        double loin = cosinus(v.get(0), v.get(2));
        System.out.println("chat/félin = " + proche + " | chat/bourse = " + loin);

        assertTrue(proche > loin);
    }

    // 1 = même direction (même sens), proche de 0 = sans rapport
    private double cosinus(float[] a, float[] b) {
        double produit = 0, na = 0, nb = 0;
        for (int i = 0; i < a.length; i++) {
            produit += a[i] * b[i];
            na += a[i] * a[i];
            nb += b[i] * b[i];
        }
        return produit / (Math.sqrt(na) * Math.sqrt(nb));
    }

    private boolean ollamaRepond() {
        try {
            HttpRequest requete = HttpRequest.newBuilder(URI.create("http://localhost:11434/api/tags")).build();
            HttpClient.newHttpClient().send(requete, HttpResponse.BodyHandlers.discarding());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}