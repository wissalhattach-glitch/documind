package com.documind.backend.service;

import com.documind.backend.exception.FichierInvalideException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PdfExtractorTest {

    private final PdfExtractor extracteur = new PdfExtractor();

    @TempDir
    Path dossier;

    // fabrique un PDF avec une page par texte donné
    private Path creerPdf(String... textesParPage) throws IOException {
        Path fichier = dossier.resolve("test.pdf");
        try (PDDocument document = new PDDocument()) {
            PDType1Font police = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            for (String texte : textesParPage) {
                PDPage page = new PDPage();
                document.addPage(page);
                try (PDPageContentStream flux = new PDPageContentStream(document, page)) {
                    flux.beginText();
                    flux.setFont(police, 12);
                    flux.newLineAtOffset(50, 700);
                    flux.showText(texte);
                    flux.endText();
                }
            }
            document.save(fichier.toFile());
        }
        return fichier;
    }

    @Test
    void compteLesPages() throws IOException {
        Path pdf = creerPdf("Page un", "Page deux", "Page trois");

        assertEquals(3, extracteur.compterPages(pdf));
    }

    @Test
    void extraitLeTexteParPage() throws IOException {
        Path pdf = creerPdf("Bonjour", "Monde");

        List<String> pages = extracteur.extraireParPage(pdf);

        assertEquals(2, pages.size());
        assertTrue(pages.get(0).contains("Bonjour"));
        assertTrue(pages.get(1).contains("Monde"));
    }

    @Test
    void refuseUnPdfCorrompu() throws IOException {
        Path faux = dossier.resolve("faux.pdf");
        Files.writeString(faux, "%PDF-1.4 faux contenu");

        assertThrows(FichierInvalideException.class, () -> extracteur.compterPages(faux));
    }
}