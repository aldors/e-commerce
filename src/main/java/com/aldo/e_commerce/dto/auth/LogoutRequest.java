package com.aldo.e_commerce.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Datos necesarios para cerrar sesión")
public class LogoutRequest {

    @Schema(
        description = "Refresh Token válido actual de la sesión",
        example = "ey321refreshtoken..."
    )
    @NotBlank(message = "El refresh token es obligatorio")
    private String refreshToken;
}
