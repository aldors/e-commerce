package com.aldo.e_commerce.exception;

public class TamañoMaximoDePaginaException extends RuntimeException{
    public TamañoMaximoDePaginaException(){
        super("El tamaño máximo de página es 10");
    }
}
