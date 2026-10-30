package com.documind.backend.service;

import com.documind.backend.dto.ReponseRag;
import com.documind.backend.dto.ResultatRecherche;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class RagServiceTest {

    private final RechercheService recherche = mock(RechercheService.class);
    private final LlmProvider llm = mock(LlmProvider.class);
    private final RagService service = new RagService(recherche, llm, 0.30);

    private ResultatRecherche extrait(long id, int page, double score) {
        return new ResultatRecherche(id, 1L, "cours.pdf", page, "Texte " + id, score);
    }

    @Test
    void sousLeSeuilOnNAppellePasLIA() {
        when(recherche.rechercher("question", 1L)).thenReturn(List.of(extrait(1, 1, 0.20)));

        ReponseRag reponse = service.repondre("question", 1L);

        assertFalse(reponse.trouve());
        assertTrue(reponse.sources().isEmpty());
        verifyNoInteractions(llm);
    }

    @Test
    void informationAbsenteDonneUneReponseSansSource() {
        when(recherche.rechercher("question", 1L)).thenReturn(List.of(extrait(1, 1, 0.60)));
        when(llm.generer(anyString(), anyString())).thenReturn("INFORMATION_ABSENTE");

        ReponseRag reponse = service.repondre("question", 1L);

        assertFalse(reponse.trouve());
        assertTrue(reponse.sources().isEmpty());
    }

    @Test
    void seulesLesSourcesCiteesSontRenvoyees() {
        when(recherche.rechercher("question", 1L)).thenReturn(List.of(
                extrait(1, 1, 0.60), extrait(2, 3, 0.50), extrait(3, 4, 0.40)));
        when(llm.generer(anyString(), anyString())).thenReturn("Voici la réponse [2].");

        ReponseRag reponse = service.repondre("question", 1L);

        assertTrue(reponse.trouve());
        assertEquals(1, reponse.sources().size());
        assertEquals(3, reponse.sources().get(0).numeroPage());
    }

    @Test
    void sansCitationOnGardeToutesLesSources() {
        when(recherche.rechercher("question", 1L)).thenReturn(List.of(
                extrait(1, 1, 0.60), extrait(2, 3, 0.50)));
        when(llm.generer(anyString(), anyString())).thenReturn("Une réponse sans numéro.");

        ReponseRag reponse = service.repondre("question", 1L);

        assertEquals(2, reponse.sources().size());
    }
}