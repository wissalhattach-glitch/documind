package com.documind.backend.controller;

import com.documind.backend.dto.RechercheRequest;
import com.documind.backend.dto.ResultatRecherche;
import com.documind.backend.service.RechercheService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recherche")
@RequiredArgsConstructor
public class RechercheController {

    private final RechercheService rechercheService;

    @PostMapping
    public List<ResultatRecherche> rechercher(@Valid @RequestBody RechercheRequest requete,
                                              @AuthenticationPrincipal Long userId) {
        return rechercheService.rechercher(requete.question(), userId);
    }
}