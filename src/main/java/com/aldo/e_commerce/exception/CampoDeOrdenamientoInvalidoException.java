package com.aldo.e_commerce.exception;

public class CampoDeOrdenamientoInvalidoException extends RuntimeException{
    public CampoDeOrdenamientoInvalidoException(String ordenamiento){
        super("Campo de ordenamiento inválido: " + ordenamiento);
    }
}
