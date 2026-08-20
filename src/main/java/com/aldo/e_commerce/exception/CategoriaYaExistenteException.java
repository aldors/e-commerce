package com.aldo.e_commerce.exception;

public class CategoriaYaExistenteException extends RuntimeException {

    public CategoriaYaExistenteException(String nombre) {
        super("Ya existe una categoria con este nombre: '" + nombre + "'");
    }
    
}
