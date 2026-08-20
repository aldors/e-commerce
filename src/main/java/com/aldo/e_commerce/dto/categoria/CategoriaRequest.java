package com.aldo.e_commerce.dto.categoria;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Datos necesarios para crear una categoria")
public class CategoriaRequest {
    
    @Schema(
        description = "Nombre de la categoria",
        example = "Tecnología"
    )
    @NotBlank(message = "El nombre de la categoría es obligatorio") 
    private String nombre;
}
