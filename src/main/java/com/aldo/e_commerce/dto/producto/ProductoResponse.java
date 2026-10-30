package com.aldo.e_commerce.dto.producto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Schema(description = "Datos para devolver datos del producto")
public class ProductoResponse {

    @Schema(description = "Id del producto", example = "1")
    private Long id;

    @Schema(description = "Nombre del producto", example = "Laptop Gamer")
    private String nombre;

    @Schema(description = "Descripción del producto", example = "Laptop Gamer 16GB RAM 128SSD Intel Core i9 Ultra")
    private String descripcion;

    @Schema(description = "Precio del producto", example = "15000.0")
    private Double precio;

    @Schema(description = "Stock del producto", example = "30")
    private Integer stock;

    @Schema(description = "Categoria del producto", example = "Tecnología")
    private String categoria;

    // Atributo agregado para que el admin y usuarios puedan ver la disponibilidad
    // del producto
    private Boolean activo;

    private Boolean disponible;

}
