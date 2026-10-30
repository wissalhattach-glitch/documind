package com.documind.backend.service;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChunkingServiceTest {

    // petits chunks pour tester facilement : 200 caractères, 50 de chevauchement
    private final ChunkingService service = new ChunkingService(200, 50);

    private String texteLong(int nbMots) {
        return IntStream.range(0, nbMots)
                .mapToObj(i -> "m" + i)
                .collect(Collectors.joining(" "));
    }

    @Test
    void petitTexteDonneUnSeulChunk() {
        List<Chunk> chunks = service.decouper(List.of("Bonjour le monde"));

        assertEquals(1, chunks.size());
        assertEquals(1, chunks.get(0).numeroPage());
        assertEquals(0, chunks.get(0).position());
    }

    @Test
    void pageVideIgnoreeEtNumeroDePageConserve() {
        List<Chunk> chunks = service.decouper(List.of("", "Texte de la page deux"));

        assertEquals(1, chunks.size());
        assertEquals(2, chunks.get(0).numeroPage());
    }

    @Test
    void longTexteDecoupeEnPlusieursChunksDeTailleMax() {
        List<Chunk> chunks = service.decouper(List.of(texteLong(300)));

        assertTrue(chunks.size() > 1);
        for (Chunk c : chunks) {
            assertTrue(c.contenu().length() <= 200);
        }
        for (int i = 0; i < chunks.size(); i++) {
            assertEquals(i, chunks.get(i).position());
        }
    }

    @Test
    void chunksSeChevauchent() {
        List<Chunk> chunks = service.decouper(List.of(texteLong(300)));

        String premierMotDuSecond = chunks.get(1).contenu().split(" ")[0];
        List<String> motsDuPremier = Arrays.asList(chunks.get(0).contenu().split(" "));

        assertTrue(motsDuPremier.contains(premierMotDuSecond));
    }

    @Test
    void aucunTexteNEstPerdu() {
        List<Chunk> chunks = service.decouper(List.of(texteLong(300)));

        String tout = chunks.stream().map(Chunk::contenu).collect(Collectors.joining(" "));
        assertTrue(tout.contains("m0 "));
        assertTrue(tout.endsWith("m299"));
    }

    @Test
    void chaquePageGardeSonNumero() {
        List<Chunk> chunks = service.decouper(List.of(texteLong(100), texteLong(100)));

        assertEquals(1, chunks.get(0).numeroPage());
        assertEquals(2, chunks.get(chunks.size() - 1).numeroPage());
    }
}