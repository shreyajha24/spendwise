package com.shreya.spendwise.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class ValidationErrorResponse {
    private int status;

    private List<String> errors;

    private LocalDateTime timestamp;
}
