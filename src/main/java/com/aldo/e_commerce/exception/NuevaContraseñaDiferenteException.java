package com.aldo.e_commerce.exception;

public class NuevaContraseñaDiferenteException extends RuntimeException{
    public NuevaContraseñaDiferenteException(){
        super("La nueva contraseña debe ser diferente a la actual");
    }
}
