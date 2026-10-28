package com.aldo.e_commerce.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class RegistroResponse {
    private Long id;
    private String nombre;
    private String role;
}
