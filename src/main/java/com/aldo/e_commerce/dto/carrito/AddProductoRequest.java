package com.aldo.e_commerce.dto.carrito;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Datos necesarios para agregar un producto al carrito")
public class AddProductoRequest {
    
    @Schema(
        description = "Id del producto a agregar al carrito",
        example = "1"
    )
    private Long productoId;

    @Schema(
        description = "Cantidad a comprar del producto",
        example = "5"
    )
    @Positive(message = "La cantidad debe ser mayor a 0")
    private Integer cantidad;
}
