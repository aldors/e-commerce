package com.aldo.e_commerce.dto.orden;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class OrdenDetalleResponse {
    private String producto;
    private Integer cantidad;
    private Double precioUnitario;
}
