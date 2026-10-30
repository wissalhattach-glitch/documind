package com.documind.backend.dto;

import java.util.List;

public record ReponseRag(
        String reponse,
        boolean trouve,
        List<SourceReponse> sources) {

    public static ReponseRag nonTrouve() {
        return new ReponseRag(
                "Je n'ai pas trouvé cette information dans tes documents.",
                false,
                List.of());
    }
}