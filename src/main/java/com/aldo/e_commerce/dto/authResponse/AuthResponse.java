package com.aldo.e_commerce.dto.authResponse;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class AuthResponse {
    private String accessToken;
    private String refreshToken;

}
