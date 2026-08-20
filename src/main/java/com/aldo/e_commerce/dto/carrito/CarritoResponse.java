package com.aldo.e_commerce.dto.carrito;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CarritoResponse {
    private List<CarritoItemResponse> items;
    private Double total;
}
