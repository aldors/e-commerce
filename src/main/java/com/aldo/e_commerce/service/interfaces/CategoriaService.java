package com.aldo.e_commerce.service.interfaces;

import java.util.List;

import com.aldo.e_commerce.dto.categoria.CategoriaRequest;
import com.aldo.e_commerce.dto.categoria.CategoriaResponse;

public interface CategoriaService {
    public CategoriaResponse crear(CategoriaRequest request);
    public List<CategoriaResponse> listar();
    public CategoriaResponse actualizar(CategoriaRequest request, Long id);
    public void eliminar(Long id);
}
