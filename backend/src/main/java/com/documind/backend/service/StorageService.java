package com.documind.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class StorageService {

    private final Path racine;

    public StorageService(@Value("${app.storage.dir}") String dossier) {
        this.racine = Paths.get(dossier).toAbsolutePath().normalize();
        try {
            Files.createDirectories(racine);
        } catch (IOException e) {
            throw new UncheckedIOException("Impossible de créer le dossier de stockage", e);
        }
    }

    // enregistre le PDF sous un nom généré et retourne ce nom
    public String sauvegarder(MultipartFile fichier) {
        String nom = UUID.randomUUID() + ".pdf";
        try (InputStream in = fichier.getInputStream()) {
            Files.copy(in, racine.resolve(nom));
        } catch (IOException e) {
            throw new UncheckedIOException("Échec de l'enregistrement du fichier", e);
        }
        return nom;
    }

    // retourne le chemin complet d'un fichier déjà enregistré
    public Path resoudre(String nom) {
        Path cible = racine.resolve(nom).normalize();
        if (!cible.startsWith(racine)) {
            throw new IllegalArgumentException("Chemin invalide");
        }
        return cible;
    }

    public void supprimer(String nom) {
        try {
            Files.deleteIfExists(resoudre(nom));
        } catch (IOException e) {
            throw new UncheckedIOException("Échec de la suppression du fichier", e);
        }
    }
}