package com.aldo.e_commerce.dto.loginAndLogout;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Datos necesarios para iniciar sesión en tu cuenta")
public class LoginRequest {
    
    //Solo se inicia sesion con email y contraseña, el username es para el registro

    @Schema(
        description = "Email del usuario",
        example = "aldo@gmail.documentacion"
    )
    @Email(message = "El email debe ser válido")
    @NotBlank(message = "El email es obligatorio")
    private String email;

    @Schema(
        description = "Password del usuario",
        example = "docpass321"
    )
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;
}
