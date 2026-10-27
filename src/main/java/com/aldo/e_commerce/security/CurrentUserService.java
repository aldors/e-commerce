package com.aldo.e_commerce.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.aldo.e_commerce.entity.Usuario;
import com.aldo.e_commerce.exception.UsuarioNoEncontradoException;
import com.aldo.e_commerce.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UsuarioRepository usuarioRepository;

    public Usuario obtenerUsuarioActual(){
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())){
            throw new RuntimeException("Usuario no autenticado");
        }

        String email = authentication.getName();

        return usuarioRepository.findByEmail(email).orElseThrow(() -> new UsuarioNoEncontradoException());
    }
}