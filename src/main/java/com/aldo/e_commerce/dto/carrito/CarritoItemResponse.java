package com.aldo.e_commerce.dto.carrito;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CarritoItemResponse {
    private Long productoId;
    private String nombre;
    private Integer cantidad;
    private Double precio;
}
