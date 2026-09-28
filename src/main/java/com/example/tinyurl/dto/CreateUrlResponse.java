package com.example.tinyurl.dto;

public record CreateUrlResponse(
        String shortCode,
        String shortUrl
) {
}