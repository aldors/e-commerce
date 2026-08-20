package com.aldo.e_commerce.dto.dtosUsuarioContraseñaEmail;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailUsuarioRequest {
    
    @Email(message = "El nuevo email debe ser válido")
    @NotBlank(message = "El nuevo email es obligatorio")
    private String email;
}
