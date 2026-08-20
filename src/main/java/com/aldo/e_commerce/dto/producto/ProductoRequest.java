package com.aldo.e_commerce.dto.producto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Datos necesarios para crear un producto")
public class ProductoRequest {

    @Schema(description = "Nombre del producto", example = "Laptop Gamer")
    @NotBlank(message = "El nombre del producto es obligatorio")
    private String nombre;

    @Schema(description = "Descripción del producto", example = "Laptop Gamer 16GB RAM 128SSD Intel Core i9 Ultra")
    private String descripcion;

    @Schema(description = "Precio del producto", example = "15000.0")
    @NotNull(message = "El precio del producto es obligatorio")
    @Positive(message = "El precio debe ser mayor a 0")
    private Double precio;

    @Schema(description = "Stock del producto", example = "30")
    @NotNull(message = "El stock del producto es obligatorio")
    @PositiveOrZero(message = "El stock no puede ser negativo")
    private Integer stock;

    @Schema(description = "Categoria del producto", example = "1")
    @NotNull(message = "La categoría del producto es obligatoria")
    private Long categoriaId;

    // Esto lo agregamos para poder reactivar el producto despues de eliminarlo con el soft delete
    private Boolean activo;
}
