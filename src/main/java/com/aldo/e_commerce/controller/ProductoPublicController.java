package com.aldo.e_commerce.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aldo.e_commerce.dto.producto.ProductoResponse;
import com.aldo.e_commerce.dto.response.ApiResponsee;
import com.aldo.e_commerce.service.interfaces.ProductoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(
        name = "Productos",
        description = "Catálogo público de productos"
)

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/productos")
@RequiredArgsConstructor
@Validated
public class ProductoPublicController {

    private final ProductoService productoService;

@Operation(
        summary = "Listar productos",
        description = "Permite a cualquier usuario, incluso sin iniciar sesión, listar los productos activos"
)
@ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Productos listados correctamente"
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Datos inválidos"
        )
})

    @SecurityRequirements
    @GetMapping("/listar")
    public ResponseEntity<ApiResponsee<Page<ProductoResponse>>> listar(
        @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable,
        @RequestParam(required = false) String categoria,
        @RequestParam(required = false) Double minPrecio,
        @RequestParam(required = false) Double maxPrecio) {

        return ResponseEntity.ok(new ApiResponsee<>(true, "Productos paginados con exito", productoService.listar(
                    pageable,
                    categoria,
                    minPrecio,
                    maxPrecio,
                    false
        )));
    }
}
