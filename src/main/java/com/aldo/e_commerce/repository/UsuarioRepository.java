package com.aldo.e_commerce.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aldo.e_commerce.entity.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long>{
    
    //Metodo personalizado para buscar un usuario por su email
    Optional<Usuario> findByEmail(String email);
}
