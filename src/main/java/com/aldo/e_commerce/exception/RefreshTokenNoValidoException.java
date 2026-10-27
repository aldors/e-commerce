package com.aldo.e_commerce.exception;

public class RefreshTokenNoValidoException extends RuntimeException {
    public RefreshTokenNoValidoException(){
        super("Refresh Token no válido");
    }
}
