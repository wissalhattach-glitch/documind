
package com.documind.backend.dto;

public record ErrorResponse(int status, String code, String message) {}