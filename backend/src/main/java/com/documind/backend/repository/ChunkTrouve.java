package com.documind.backend.repository;

public interface ChunkTrouve {
    Long getId();
    Long getDocumentId();
    String getNomDocument();
    Integer getNumeroPage();
    String getContenu();
    Double getScore();
}