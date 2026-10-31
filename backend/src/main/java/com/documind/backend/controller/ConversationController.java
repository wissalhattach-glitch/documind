package com.documind.backend.controller;

import com.documind.backend.dto.ConversationDetail;
import com.documind.backend.dto.ConversationResume;
import com.documind.backend.dto.CreerConversationRequest;
import com.documind.backend.dto.PageResponse;
import com.documind.backend.dto.QuestionRequest;
import com.documind.backend.dto.RenommerRequest;
import com.documind.backend.dto.ReponseRag;
import com.documind.backend.service.ConversationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ConversationDetail creer(@Valid @RequestBody CreerConversationRequest requete,
                                    @AuthenticationPrincipal Long userId) {
        return conversationService.creer(requete, userId);
    }

    @GetMapping
    public PageResponse<ConversationResume> lister(
            @AuthenticationPrincipal Long userId,
            @PageableDefault(size = 20, sort = "dateCreation", direction = Sort.Direction.DESC) Pageable pageable) {
        return PageResponse.de(conversationService.lister(userId, pageable));
    }

    @GetMapping("/{id}")
    public ConversationDetail detail(@PathVariable Long id,
                                     @AuthenticationPrincipal Long userId) {
        return conversationService.detail(id, userId);
    }

    @PatchMapping("/{id}")
    public ConversationResume renommer(@PathVariable Long id,
                                       @Valid @RequestBody RenommerRequest requete,
                                       @AuthenticationPrincipal Long userId) {
        return conversationService.renommer(id, requete.titre(), userId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id,
                          @AuthenticationPrincipal Long userId) {
        conversationService.supprimer(id, userId);
    }

    @PostMapping("/{id}/messages")
    public ReponseRag poserQuestion(@PathVariable Long id,
                                    @Valid @RequestBody QuestionRequest requete,
                                    @AuthenticationPrincipal Long userId) {
        return conversationService.poserQuestion(id, requete.question(), userId);
    }
}