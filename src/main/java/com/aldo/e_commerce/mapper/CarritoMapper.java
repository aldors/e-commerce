package com.aldo.e_commerce.mapper;

import java.util.List;

import com.aldo.e_commerce.dto.carrito.CarritoItemResponse;
import com.aldo.e_commerce.dto.carrito.CarritoResponse;
import com.aldo.e_commerce.entity.Carrito;
import com.aldo.e_commerce.entity.CarritoItem;

public class CarritoMapper {

    public static CarritoItemResponse toItemResponse(CarritoItem item) {
        return new CarritoItemResponse(
                item.getProducto().getId(),
                item.getProducto().getNombre(),
                item.getCantidad(),
                item.getProducto().getPrecio()
        );
    }

    public static CarritoResponse toResponse(Carrito carrito) {

        List<CarritoItemResponse> items = carrito.getItems()
                .stream()
                .map(CarritoMapper::toItemResponse)
                .toList();

        Double total = carrito.getItems()
                .stream()
                .mapToDouble(i -> i.getProducto().getPrecio() * i.getCantidad())
                .sum();

        return new CarritoResponse(items, total);
    }
}