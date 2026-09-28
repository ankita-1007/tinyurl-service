package com.example.tinyurl.exception;

import com.example.tinyurl.exception.UrlNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UrlNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleUrlNotFound(
            UrlNotFoundException exception) {

        return Map.of(
                "error", exception.getMessage()
        );
    }
}