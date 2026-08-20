package com.aldo.e_commerce.mapper;

import com.aldo.e_commerce.dto.categoria.CategoriaRequest;
import com.aldo.e_commerce.dto.categoria.CategoriaResponse;
import com.aldo.e_commerce.entity.Categoria;

public class CategoriaMapper {
    
    public static Categoria toEntity(CategoriaRequest request){
        return Categoria.builder()
                .nombre(request.getNombre())
                .build();
    }

    public static CategoriaResponse toResponse(Categoria categoria){
        return new CategoriaResponse(
                categoria.getId(),
                categoria.getNombre()
                );
    }
}
