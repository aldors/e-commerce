package com.aldo.e_commerce.exception;

public class EmailYaExistenteException extends RuntimeException {
    public EmailYaExistenteException(String email) {
        super("El email '" + email + "' ya está registrado.");
    }
    
}
