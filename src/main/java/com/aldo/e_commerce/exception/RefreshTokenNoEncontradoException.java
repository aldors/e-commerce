package com.aldo.e_commerce.exception;

public class RefreshTokenNoEncontradoException extends RuntimeException {
    public RefreshTokenNoEncontradoException(String token) {
        super("Refresh token no encontrado: " + token);
    }
    
}
