package com.aldo.e_commerce.mapper;

import com.aldo.e_commerce.dto.auth.RegistroRequest;
import com.aldo.e_commerce.dto.auth.RegistroResponse;
import com.aldo.e_commerce.entity.Role;
import com.aldo.e_commerce.entity.Usuario;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RegistroMapper {
    
    public static Usuario toEntity(RegistroRequest request) {
        return Usuario.builder()
                .nombre(request.getNombre().toLowerCase()) // Para que todos lo nombres se guarden en minusculas
                .email(request.getEmail())
                .password(request.getPassword())
                .role(Role.USER)
                .build();
    }

    
    public static RegistroResponse toResponse(Usuario usuario) {
        return new RegistroResponse(usuario.getId(),
                                    usuario.getNombre(),
                                    usuario.getRole().name());
    }

}
