package com.aldo.e_commerce.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.aldo.e_commerce.entity.Orden;

public interface OrdenRepository extends JpaRepository<Orden, Long>{
    List<Orden> findByUsuarioId(Long usuarioId);
}
