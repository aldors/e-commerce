package com.aldo.e_commerce.dto.registro;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Datos necesarios para registrarse en el sistema")
public class RegistroRequest {

    //Aquí si se registra el username, el email y la contraseña, para luego iniciar sesion solo con email y contraseña
    
    @Schema(
        description = "Nombre del usuario",
        example = "Aldo"
    )
    @NotBlank(message = "El nombre de usuario es obligatorio")
    private String nombre;

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
