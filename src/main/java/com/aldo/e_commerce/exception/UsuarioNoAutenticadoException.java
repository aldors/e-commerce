package com.aldo.e_commerce.exception;

public class UsuarioNoAutenticadoException extends RuntimeException{
    public UsuarioNoAutenticadoException(){
        super("Usuario no autenticado");
    }
}
