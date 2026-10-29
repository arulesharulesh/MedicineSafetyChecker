package com.example.drugchecker.controller;

import com.example.drugchecker.dto.InteractionCheckRequest;
import com.example.drugchecker.dto.InteractionCheckResponse;
import com.example.drugchecker.service.InteractionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/interactions")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:5500", "http://localhost:5500"}, allowCredentials = "true")
public class InteractionController {

    private final InteractionService interactionService;

    public InteractionController(InteractionService interactionService) {
        this.interactionService = interactionService;
    }

    @PostMapping("/check")
    public ResponseEntity<InteractionCheckResponse> check(@Valid @RequestBody InteractionCheckRequest request) {
        return ResponseEntity.ok(interactionService.checkInteractions(request));
    }
}
