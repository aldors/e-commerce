package com.aldo.e_commerce.exception;

public class CarritoVacioException extends RuntimeException{
    public CarritoVacioException(){
        super("El carrito se encuentra vacío");
    }
}
