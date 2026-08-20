package com.aldo.e_commerce.service.interfaces;

import com.aldo.e_commerce.dto.addProductoAlCarrito.AddProductoRequest;
import com.aldo.e_commerce.dto.carrito.CarritoResponse;

public interface CarritoService {
    CarritoResponse obtenerCarrito();
    CarritoResponse agregarProducto(AddProductoRequest request);
    CarritoResponse eliminarProducto(Long productoId);
}
