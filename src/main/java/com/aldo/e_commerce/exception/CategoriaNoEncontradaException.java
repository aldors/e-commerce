package com.aldo.e_commerce.exception;

public class CategoriaNoEncontradaException extends RuntimeException {

    public CategoriaNoEncontradaException(Long id) {
        super("No se encontro la categoria con el ID: '" + id + "'");
    }
    
}
