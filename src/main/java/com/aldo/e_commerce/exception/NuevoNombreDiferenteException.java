package com.aldo.e_commerce.exception;

public class NuevoNombreDiferenteException extends RuntimeException{
    public NuevoNombreDiferenteException(){
        super("El nuevo nombre debe ser diferente al actual");
    }
}
