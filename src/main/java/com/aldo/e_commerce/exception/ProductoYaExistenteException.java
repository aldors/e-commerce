package com.aldo.e_commerce.exception;

public class ProductoYaExistenteException extends RuntimeException {

    public ProductoYaExistenteException(String nombre) {
        super("ya existe un producto con este nombre: " + nombre);
    }
    
}
