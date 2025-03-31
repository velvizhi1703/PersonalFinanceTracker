package com.tus.finance.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
    	 if (e.getMessage().contains("Invalid token") || 
    	            e.getMessage().contains("JWT validation error")) {
    	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
    	                   .body("Invalid authentication token");
    	        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }
}
