package com.smartspend.exception;

import com.smartspend.dto.common.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleNotFound_returns404WithMessage() {
        ResponseEntity<ApiResponse<Object>> response =
                handler.handleNotFound(new ResourceNotFoundException("Expense not found"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Expense not found", response.getBody().getMessage());
    }

    @Test
    void handleDuplicate_returns409() {
        ResponseEntity<ApiResponse<Object>> response =
                handler.handleDuplicate(new DuplicateResourceException("Email already exists"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void handleBadRequest_returns400() {
        ResponseEntity<ApiResponse<Object>> response =
                handler.handleBadRequest(new BadRequestException("Invalid input"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void handleTokenRefresh_returns403() {
        ResponseEntity<ApiResponse<Object>> response =
                handler.handleTokenRefresh(new TokenRefreshException("abc123", "expired"));

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
}
