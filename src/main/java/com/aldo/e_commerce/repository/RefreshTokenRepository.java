package com.aldo.e_commerce.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.aldo.e_commerce.entity.RefreshToken;
import com.aldo.e_commerce.entity.Usuario;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);
    void deleteByToken(String token);

    @Transactional
    void deleteByUsuario(Usuario usuario);

    @Transactional
    void deleteByUsuario_Email(String email);

}
