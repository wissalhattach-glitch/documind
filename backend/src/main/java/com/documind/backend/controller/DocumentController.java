package com.documind.backend.controller;

import com.documind.backend.dto.DocumentResponse;
import com.documind.backend.dto.PageResponse;
import com.documind.backend.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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

    @GetMapping
    public PageResponse<DocumentResponse> lister(
            @AuthenticationPrincipal Long userId,
            @PageableDefault(size = 20, sort = "dateUpload", direction = Sort.Direction.DESC) Pageable pageable) {
        return PageResponse.de(documentService.lister(userId, pageable).map(DocumentResponse::de));
    }

    @GetMapping("/{id}")
    public DocumentResponse trouver(@PathVariable Long id,
                                    @AuthenticationPrincipal Long userId) {
        return DocumentResponse.de(documentService.trouver(id, userId));
    }
}