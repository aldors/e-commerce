package com.aldo.e_commerce.exception;

public class ProductoNoEncontradoException extends RuntimeException {

    public ProductoNoEncontradoException(Long id) {
        super("No se encontro el producto con el ID: '" + id + "'");
    }
    
}
