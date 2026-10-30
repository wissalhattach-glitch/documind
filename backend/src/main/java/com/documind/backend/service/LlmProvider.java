package com.documind.backend.service;

public interface LlmProvider {

    // consigne = les règles données à l'IA (system), question = ce que l'utilisateur demande (user)
    String generer(String consigne, String question);
}