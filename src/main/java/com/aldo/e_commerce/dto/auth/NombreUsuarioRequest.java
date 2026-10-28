package com.aldo.e_commerce.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NombreUsuarioRequest {
    
    @NotBlank(message = "El nuevo nombre de usuaio es obligatorio")
    private String nombre;
}
