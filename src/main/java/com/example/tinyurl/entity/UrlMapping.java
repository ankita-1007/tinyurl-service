package com.example.tinyurl.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "url_mapping")
@Getter
@Setter
public class UrlMapping {

    @Id
    private Long id;

    @Column(name = "short_code", unique = true, nullable = false)
    private String shortCode;

    @Column(name = "long_url", nullable = false)
    private String longUrl;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}