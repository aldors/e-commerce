package com.aldo.e_commerce.dto.auth;

import com.aldo.e_commerce.entity.Role;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class MeResponse {
    
    private String nombre;
    private String email;
    private Role rol;
}
