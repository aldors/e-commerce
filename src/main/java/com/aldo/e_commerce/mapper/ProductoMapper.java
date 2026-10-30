package com.aldo.e_commerce.mapper;

import com.aldo.e_commerce.dto.producto.ProductoRequest;
import com.aldo.e_commerce.dto.producto.ProductoResponse;
import com.aldo.e_commerce.entity.Categoria;
import com.aldo.e_commerce.entity.Producto;

public class ProductoMapper {

    public static Producto toEntity(ProductoRequest request, Categoria categoria) {
        return Producto.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .precio(request.getPrecio())
                .stock(request.getStock())
                .categoria(categoria)
                .build();
    }

    public static ProductoResponse toResponse(Producto producto) {
        boolean disponible = producto.getActivo() && producto.getStock() != null && producto.getStock() > 0;

        return new ProductoResponse(
                producto.getId(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getPrecio(),
                producto.getStock(),
                producto.getCategoria().getNombre(),
                // Agregado recientemente para permitir ver la disponibilidad del producto
                producto.getActivo(),
                disponible
            );
    }
}
