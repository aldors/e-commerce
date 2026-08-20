package com.aldo.e_commerce.exception;

public class NuevoEmailDiferenteException extends RuntimeException{
    public NuevoEmailDiferenteException(){
        super("El nuevo email debe ser diferente al actual");
    }
}
