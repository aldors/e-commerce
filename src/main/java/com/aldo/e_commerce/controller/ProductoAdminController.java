package com.aldo.e_commerce.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
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

import com.aldo.e_commerce.dto.producto.ProductoRequest;
import com.aldo.e_commerce.dto.producto.ProductoResponse;
import com.aldo.e_commerce.dto.response.ApiResponsee;
import com.aldo.e_commerce.service.interfaces.ProductoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(
        name = "Productos Admin",
        description = "Administración de productos del sistema"
)

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/admin/productos")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Validated
public class ProductoAdminController {

    private final ProductoService productoService;

@Operation(
        summary = "Listar productos (admin)",
        description = "Permite a un administrador listar todos los productos, incluyendo los inactivos"
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
        ),
        @ApiResponse(
                responseCode = "403",
                description = "No autorizado"
        )
})

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
                    true
        )));
    }

@Operation(
        summary = "Crear producto",
        description = "Permite a un administrador crear un nuevo producto"
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

    @PostMapping("/crear")
    public ResponseEntity<ApiResponsee<ProductoResponse>> crear(@Valid @RequestBody ProductoRequest productoRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponsee<>(true, "Producto creado correctamente", productoService.crear(productoRequest)));
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

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id){
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
