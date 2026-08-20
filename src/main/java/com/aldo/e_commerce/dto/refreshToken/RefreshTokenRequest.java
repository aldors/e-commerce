package com.aldo.e_commerce.dto.refreshToken;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Datos necesarios para generar un nuevo Access Token")
public class RefreshTokenRequest {

    @Schema(
        description = "Refresh Token válido actual de la sesión",
        example = "ey321refreshtoken..."
    )
    private String refreshToken;
}
