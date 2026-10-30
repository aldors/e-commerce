package com.aldo.e_commerce.exception;

public class StockInsuficienteException extends RuntimeException{
    public StockInsuficienteException(String nombre, int disponible){
        super("Stock insuficiente para: '" + nombre + "' | Stock disponible: " + disponible);
    }

    public StockInsuficienteException(String nombre, int stockTotal, int cantidadEnCarrito, int cantidadSolicitada){
        super("Stock insuficiente para: '" + nombre + "' | Stock: " + stockTotal + " | Cantidad en tu carrito: " + cantidadEnCarrito + " | Cantidad solicitada: " + cantidadSolicitada);
    }
}
