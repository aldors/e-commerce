package com.aldo.e_commerce.exception;

public class ProductoNoDisponibleException extends RuntimeException{
    public ProductoNoDisponibleException(String nombre){
        super("El producto '" + nombre + "' no se encuentra disponible actualmente");
    }
}
