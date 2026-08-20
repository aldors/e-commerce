package com.aldo.e_commerce.dto.loginAndLogout;

import io.swagger.v3.oas.annotations.media.Schema;
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
    private String refreshToken;
}
