package com.aldo.e_commerce.mapper;

import java.util.List;

import com.aldo.e_commerce.dto.orden.OrdenDetalleResponse;
import com.aldo.e_commerce.dto.orden.OrdenResponse;
import com.aldo.e_commerce.entity.Orden;
import com.aldo.e_commerce.entity.OrdenDetalle;

public class OrdenMapper {
    
    public static OrdenDetalleResponse toDetalleResponse(OrdenDetalle detalle){
        return new OrdenDetalleResponse(
                detalle.getProducto().getNombre(),
                detalle.getCantidad(),
                detalle.getPrecioUnitario()
        );
    }

    public static OrdenResponse toResponse(Orden orden){
        List<OrdenDetalleResponse> detalles = orden.getDetalles()
                .stream()
                .map(OrdenMapper::toDetalleResponse)
                .toList();

        return new OrdenResponse(
                orden.getId(),
                orden.getTotal(),
                orden.getEstado().name(),
                orden.getFecha(),
                detalles
        );
    }
}
