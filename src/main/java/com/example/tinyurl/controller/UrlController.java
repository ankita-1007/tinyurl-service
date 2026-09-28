package com.example.tinyurl.controller;

import com.example.tinyurl.dto.CreateUrlRequest;
import com.example.tinyurl.dto.CreateUrlResponse;
import com.example.tinyurl.service.UrlService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class UrlController {

    private final UrlService urlService;

    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    @PostMapping("/api/urls")
    public ResponseEntity<CreateUrlResponse> createUrl(
            @Valid @RequestBody CreateUrlRequest request) {

        return ResponseEntity.ok(
                urlService.createShortUrl(
                        request.longUrl()
                )
        );
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(
            @PathVariable String shortCode) {

        String longUrl =
                urlService.getLongUrl(shortCode);

        return ResponseEntity
                .status(302)
                .header("Location", longUrl)
                .build();
    }
}