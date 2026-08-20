package com.aldo.e_commerce.mapper;

import com.aldo.e_commerce.dto.authResponse.AuthResponse;

public class AuthMapper {
    
    public static AuthResponse toResponse(String accessToken, String refreshToken){
        return new AuthResponse(accessToken, refreshToken);
    }
}
