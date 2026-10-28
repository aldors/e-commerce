package com.aldo.e_commerce.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class AuthResponse {
    private String accessToken;
    private String refreshToken;

}
