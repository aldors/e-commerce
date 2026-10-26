package com.aldo.e_commerce.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aldo.e_commerce.dto.apiResponse.ApiResponsee;
import com.aldo.e_commerce.dto.orden.OrdenResponse;
import com.aldo.e_commerce.service.interfaces.OrdenService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(
    name = "Ordenes",
    description = "Administración de ordenes"
)

@RestController
@RequestMapping("/ordenes")
@RequiredArgsConstructor
public class OrdenController {
    
    private final OrdenService ordenService;

@Operation(
        summary = "Checkout",
        description = "Permite a usuarios finalizar la compra de sus carrito"
)
@ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Checkout realizado correctamente"
        ),
        @ApiResponse(
                responseCode = "401",
                description = "No autenticado"
        )
})

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/checkout")
    public ResponseEntity<ApiResponsee<OrdenResponse>> checkout(){
        return ResponseEntity.ok(new ApiResponsee<>(true, "Checkout realizado correctamente", ordenService.checkout()));
    }


@Operation(
        summary = "Historial de ordenes",
        description = "Permite a usuarios ver el historial de sus ordenes"
)
@ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Historial de ordenes"
        ),
        @ApiResponse(
                responseCode = "401",
                description = "No autenticado"
        )
})

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<ApiResponsee<List<OrdenResponse>>> historial(){
        return ResponseEntity.ok(new ApiResponsee<>(true, "Historial de ordenes", ordenService.historial()));
    }
}
