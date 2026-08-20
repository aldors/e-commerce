package com.aldo.e_commerce.dto.orden;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class OrdenResponse {
    private Long id;
    private Double total;
    private String estado;
    private LocalDateTime fecha;
    private List<OrdenDetalleResponse> detalles;
}
