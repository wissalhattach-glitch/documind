package com.documind.backend.controller;

import com.documind.backend.dto.DocumentResponse;
import com.documind.backend.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentResponse importer(@RequestParam("fichier") MultipartFile fichier,
                                     @AuthenticationPrincipal Long userId) {
        return DocumentResponse.de(documentService.importer(fichier, userId));
    }
}