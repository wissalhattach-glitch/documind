package com.documind.backend.service;

import com.documind.backend.exception.FichierInvalideException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
public class PdfExtractor {

    public int compterPages(Path pdf) {
        try (PDDocument document = ouvrir(pdf)) {
            return document.getNumberOfPages();
        } catch (IOException e) {
            throw new FichierInvalideException("Le PDF est illisible ou corrompu");
        }
    }

    // retourne le texte de chaque page : la page 1 est à l'indice 0
    public List<String> extraireParPage(Path pdf) {
        try (PDDocument document = ouvrir(pdf)) {
            PDFTextStripper lecteur = new PDFTextStripper();
            int total = document.getNumberOfPages();
            List<String> pages = new ArrayList<>(total);
            for (int numero = 1; numero <= total; numero++) {
                lecteur.setStartPage(numero);
                lecteur.setEndPage(numero);
                pages.add(lecteur.getText(document).strip());
            }
            return pages;
        } catch (IOException e) {
            throw new FichierInvalideException("Impossible de lire le texte du PDF");
        }
    }

    private PDDocument ouvrir(Path pdf) {
        try {
            return Loader.loadPDF(pdf.toFile());
        } catch (InvalidPasswordException e) {
            throw new FichierInvalideException("Le PDF est protégé par un mot de passe");
        } catch (IOException e) {
            throw new FichierInvalideException("Le PDF est illisible ou corrompu");
        }
    }
}