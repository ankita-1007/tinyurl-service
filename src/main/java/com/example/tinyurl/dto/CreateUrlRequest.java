package com.example.tinyurl.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateUrlRequest(

        @NotBlank
        @Pattern(
                regexp = "https?://.*",
                message = "URL must start with http:// or https://"
        )
        String longUrl

) {
}