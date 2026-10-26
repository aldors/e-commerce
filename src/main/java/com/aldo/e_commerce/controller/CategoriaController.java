package com.aldo.e_commerce.controller;

import java.util.List;

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
import org.springframework.web.bind.annotation.RestController;

import com.aldo.e_commerce.dto.apiResponse.ApiResponsee;
import com.aldo.e_commerce.dto.categoria.CategoriaRequest;
import com.aldo.e_commerce.dto.categoria.CategoriaResponse;
import com.aldo.e_commerce.service.interfaces.CategoriaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(
    name = "Categoria",
    description = "Administración de categorias"
)

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/categorias")
@RequiredArgsConstructor
public class CategoriaController {
    
    private final CategoriaService categoriaService;

@Operation(
        summary = "Crear categoria",
        description = "Permite a administradores crear categorias"
)
@ApiResponses({
        @ApiResponse(
                responseCode = "201",
                description = "Categoria creada correctamente"
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
    public ResponseEntity<ApiResponsee<CategoriaResponse>> crear(@Valid @RequestBody CategoriaRequest categoriaRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponsee<>(true, "Categoria creada correctamente", categoriaService.crear(categoriaRequest)));
    }


@Operation(
        summary = "Listar categorias",
        description = "Permite a usuarios y administradores listar las categorias"
)
@ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Categorias listadas correctamente"
        ),
        @ApiResponse(
                responseCode = "401",
                description = "No autenticado"
        )
})

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/listar")
    public ResponseEntity<ApiResponsee<List<CategoriaResponse>>> listar(){
        return ResponseEntity.ok(new ApiResponsee<>(true, "Categorias listadas", categoriaService.listar()));
    }


@Operation(
        summary = "Actualizar categoria",
        description = "Permite a administradores actualizar las categorias"
)
@ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Categoria actualizada correctamente"
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
    public ResponseEntity<ApiResponsee<CategoriaResponse>> actualizar(@Valid @RequestBody CategoriaRequest categoriaRequest, @PathVariable Long id){
        return ResponseEntity.ok(new ApiResponsee<>(true, "Categoria actualizada correctamente", categoriaService.actualizar(categoriaRequest, id)));
    }


@Operation(
        summary = "Eliminar categoria",
        description = "Permite a administradores eliminar categorias"
)
@ApiResponses({
        @ApiResponse(
                responseCode = "204",
                description = "Categoria eliminada correctamente"
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
        categoriaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
