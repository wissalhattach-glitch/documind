package com.documind.backend.service;

// un morceau de texte, avec la page d'où il vient
public record Chunk(int numeroPage, int position, String contenu) {
}