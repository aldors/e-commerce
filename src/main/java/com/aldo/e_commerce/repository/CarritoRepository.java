package com.aldo.e_commerce.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aldo.e_commerce.entity.Carrito;

public interface CarritoRepository extends JpaRepository<Carrito, Long>{
    Optional<Carrito> findByUsuarioId(Long usuarioId);
}
