package com.aldo.e_commerce.exception;

public class StockInsuficienteException extends RuntimeException{
    public StockInsuficienteException(String nombre, int disponible){
        super("Stock insuficiente para: '" + nombre + "' | Stock disponible: " + disponible);
    }
}
