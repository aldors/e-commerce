package com.aldo.e_commerce.exception;

public class UsuarioNoEncontradoException extends RuntimeException{
    public UsuarioNoEncontradoException(){
        super("Usuario no encontrado");
    }
}
