package com.aldo.e_commerce.exception;

public class RefreshTokenExpiradoException extends RuntimeException{
    public RefreshTokenExpiradoException(String token){
        super("Refresh token expirado: " + token);
    }
    
}
