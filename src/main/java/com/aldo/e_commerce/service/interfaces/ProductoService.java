package com.aldo.e_commerce.service.interfaces;

import org.springframework.data.domain.Page;

import com.aldo.e_commerce.dto.producto.ProductoRequest;
import com.aldo.e_commerce.dto.producto.ProductoResponse;

public interface ProductoService {
    public ProductoResponse crear(ProductoRequest request);
    Page<ProductoResponse> listar(
        int page,
        int size,
        String sortBy,
        String direction,
        String categoria,
        Double minPrecio,
        Double maxPrecio);
    public ProductoResponse actualizar(ProductoRequest request, Long id);
    public void eliminar(Long id);
}
