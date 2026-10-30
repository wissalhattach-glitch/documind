package com.documind.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChunkingService {

    private final int tailleMax;
    private final int chevauchement;

    public ChunkingService(
            @Value("${app.chunking.size:2000}") int tailleMax,
            @Value("${app.chunking.overlap:300}") int chevauchement) {
        this.tailleMax = tailleMax;
        this.chevauchement = chevauchement;
    }

    // pages.get(0) = page 1. Retourne tous les chunks du document, dans l'ordre.
    public List<Chunk> decouper(List<String> pages) {
        List<Chunk> chunks = new ArrayList<>();
        int position = 0;

        for (int i = 0; i < pages.size(); i++) {
            String texte = pages.get(i).strip();
            int debut = 0;

            while (debut < texte.length()) {
                int fin = Math.min(debut + tailleMax, texte.length());

                // on évite de couper un mot en deux : on recule jusqu'au dernier espace
                if (fin < texte.length()) {
                    int espace = dernierEspace(texte, debut + tailleMax / 2, fin);
                    if (espace > 0) {
                        fin = espace;
                    }
                }

                String contenu = texte.substring(debut, fin).strip();
                if (!contenu.isEmpty()) {
                    chunks.add(new Chunk(i + 1, position++, contenu));
                }

                if (fin >= texte.length()) {
                    break;
                }

                // le chunk suivant recommence un peu avant la fin (chevauchement)
                int suivant = fin - chevauchement;
                if (suivant <= debut) {
                    suivant = fin;
                }
                // on avance jusqu'au début d'un mot
                while (suivant < fin && !Character.isWhitespace(texte.charAt(suivant))) {
                    suivant++;
                }
                debut = suivant;
            }
        }
        return chunks;
    }

    private int dernierEspace(String texte, int min, int max) {
        for (int i = max; i > min; i--) {
            if (Character.isWhitespace(texte.charAt(i))) {
                return i;
            }
        }
        return -1;
    }
}