package com.aldo.e_commerce.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aldo.e_commerce.dto.apiResponse.ApiResponsee;
import com.aldo.e_commerce.dto.producto.ProductoRequest;
import com.aldo.e_commerce.dto.producto.ProductoResponse;
import com.aldo.e_commerce.service.interfaces.ProductoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(
        name = "Productos",
        description = "Administración de productos del sistema"
)

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/productos")
@RequiredArgsConstructor
public class ProductoController {
    
    private final ProductoService productoService;

@Operation(summary = "Crear producto", description = "Permite a un administrador crear un nuevo producto"
)
@ApiResponses({
        @ApiResponse(
                responseCode = "201",
                description = "Producto creado correctamente"
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Datos inválidos"
        ),
        @ApiResponse(
                responseCode = "401",
                description = "No autenticado"
        ),
        @ApiResponse(
                responseCode = "403",
                description = "No autorizado"
        )
})
    
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/crear")
    public ResponseEntity<ApiResponsee<ProductoResponse>> crear(@Valid @RequestBody ProductoRequest productoRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponsee<>(true, "Producto creado correctamente", productoService.crear(productoRequest)));
    }


@Operation(
        summary = "Listar productos",
        description = "Permite a usuarios y administradores listar los productos"
)
@ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Productos listados correctamente"
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Datos inválidos"
        ),
        @ApiResponse(
                responseCode = "401",
                description = "No autenticado"
        )
})

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/listar")
    public ResponseEntity<ApiResponsee<Page<ProductoResponse>>> listar(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,

        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "asc") String direction,

        @RequestParam(required = false) String categoria,

        @RequestParam(required = false) Double minPrecio,
        @RequestParam(required = false) Double maxPrecio) {

        return ResponseEntity.ok(new ApiResponsee<>(true, "Productos paginados con exito", productoService.listar(
                    page,
                    size,
                    sortBy,
                    direction,
                    categoria,
                    minPrecio,
                    maxPrecio
        )));
    }


@Operation(
        summary = "Actualizar producto",
        description = "Permite a un administrador actualizar un producto"
)
@ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Producto actualizado correctamente"
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Datos inválidos"
        ),
        @ApiResponse(
                responseCode = "401",
                description = "No autenticado"
        ),
        @ApiResponse(
                responseCode = "403",
                description = "No autorizado"
        )
})

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/actualizar/{id}")
    public ResponseEntity<ApiResponsee<ProductoResponse>> actualizar(@Valid @RequestBody ProductoRequest productoRequest, @PathVariable Long id){
        return ResponseEntity.ok(new ApiResponsee<>(true, "Producto actualizado correctamente", productoService.actualizar(productoRequest, id)));
    }


@Operation(
        summary = "Eliminar producto",
        description = "Permite a un administrador eliminar un producto"
)
@ApiResponses({
        @ApiResponse(
                responseCode = "204",
                description = "Producto eliminado correctamente"
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Datos inválidos"
        ),
        @ApiResponse(
                responseCode = "401",
                description = "No autenticado"
        ),
        @ApiResponse(
                responseCode = "403",
                description = "No autorizado"
        )
})

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id){
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
