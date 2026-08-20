package com.aldo.e_commerce.exception;

public class CarritoInexistenteException extends RuntimeException{
    public CarritoInexistenteException(String nombre){
        super("'" + nombre + "' no tiene un carrito creado");
    }
}
