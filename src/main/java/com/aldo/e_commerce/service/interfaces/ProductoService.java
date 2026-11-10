package com.aldo.e_commerce.service.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.aldo.e_commerce.dto.producto.ProductoRequest;
import com.aldo.e_commerce.dto.producto.ProductoResponse;

public interface ProductoService {
    public ProductoResponse crear(ProductoRequest request);

    Page<ProductoResponse> listar(
        Pageable pageable,
        String categoria,
        Double minPrecio,
        Double maxPrecio,
        boolean incluirInactivos);
        
    public ProductoResponse actualizar(ProductoRequest request, Long id);
    public void eliminar(Long id);
}
