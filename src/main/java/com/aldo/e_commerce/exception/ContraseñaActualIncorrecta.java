package com.aldo.e_commerce.exception;

public class ContraseñaActualIncorrecta extends RuntimeException{
    public ContraseñaActualIncorrecta(){
        super("La contraseña actual es incorrecta");
    }
}
