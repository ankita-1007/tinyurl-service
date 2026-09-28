package com.example.tinyurl.service;

import com.example.tinyurl.counter.CounterService;
import com.example.tinyurl.dto.CreateUrlResponse;
import com.example.tinyurl.entity.UrlMapping;
import com.example.tinyurl.exception.UrlNotFoundException;
import com.example.tinyurl.repository.UrlRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UrlService {

    private final CounterService counterService;
    private final Base62Encoder base62Encoder;
    private final UrlRepository urlRepository;

    public UrlService(
            CounterService counterService,
            Base62Encoder base62Encoder,
            UrlRepository urlRepository) {

        this.counterService = counterService;
        this.base62Encoder = base62Encoder;
        this.urlRepository = urlRepository;
    }

    public CreateUrlResponse createShortUrl(String longUrl) {

        Optional<UrlMapping> existing =
                urlRepository.findByLongUrl(longUrl);

        if (existing.isPresent()) {
            UrlMapping mapping = existing.get();

            return new CreateUrlResponse(
                    mapping.getShortCode(),
                    "http://localhost:8080/" + mapping.getShortCode()
            );
        }

        long id = counterService.getNextId();

        String shortCode = base62Encoder.encode(id);

        UrlMapping mapping = new UrlMapping();

        mapping.setId(id);
        mapping.setShortCode(shortCode);
        mapping.setLongUrl(longUrl);
        mapping.setCreatedAt(LocalDateTime.now());

        urlRepository.save(mapping);

        return new CreateUrlResponse(
                shortCode,
                "http://localhost:8080/" + shortCode
        );
    }

    public String getLongUrl(String shortCode) {

        UrlMapping mapping = urlRepository
                .findByShortCode(shortCode)
                .orElseThrow(
                        () -> new UrlNotFoundException("URL not found")
                );

        return mapping.getLongUrl();
    }


}