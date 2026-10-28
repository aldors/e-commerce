package com.aldo.e_commerce.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aldo.e_commerce.dto.addProductoAlCarrito.AddProductoRequest;
import com.aldo.e_commerce.dto.apiResponse.ApiResponsee;
import com.aldo.e_commerce.dto.carrito.CarritoResponse;
import com.aldo.e_commerce.service.interfaces.CarritoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(
    name = "Carrito",
    description = "Administración del carrito"
)

@RestController
@RequestMapping("/carrito")
@RequiredArgsConstructor
public class CarritoController {

    private final CarritoService carritoService;
    
@Operation(
        summary = "Obtener carrito",
        description = "Permite a usuarios ver el contenido de sus carritos"
)
@ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Carrito obtenido"
        ),
        @ApiResponse(
                responseCode = "401",
                description = "No autenticado"
        )
})

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<ApiResponsee<CarritoResponse>> obtenerCarrito(){
        return ResponseEntity.ok().body(new ApiResponsee<>(true, "Carrito obtenido con éxito", carritoService.obtenerCarrito()));
    }


@Operation(
        summary = "Agregar producto",
        description = "Permite a usuarios agregar productos a su carrito"
)
@ApiResponses({
        @ApiResponse(
                responseCode = "201",
                description = "Producto agregado correctamente"
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
    @PostMapping
    public ResponseEntity<ApiResponsee<CarritoResponse>> agregarProducto(@Valid @RequestBody AddProductoRequest addProductoRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponsee<>(true, "Producto agregado al carrito correctamente", carritoService.agregarProducto(addProductoRequest)));
    }


@Operation(
        summary = "Eliminar producto",
        description = "Permite a usuarios eliminar productos de su carrito"
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
        )
})
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{productoId}")
    public ResponseEntity<CarritoResponse> eliminarProucto(@PathVariable Long productoId){
        carritoService.eliminarProducto(productoId);
        return ResponseEntity.noContent().build();
    }
}
