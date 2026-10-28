package com.aldo.e_commerce.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aldo.e_commerce.dto.auth.AuthResponse;
import com.aldo.e_commerce.dto.auth.ContraseñaUsuarioRequest;
import com.aldo.e_commerce.dto.auth.EmailUsuarioRequest;
import com.aldo.e_commerce.dto.auth.LoginRequest;
import com.aldo.e_commerce.dto.auth.LogoutRequest;
import com.aldo.e_commerce.dto.auth.MeResponse;
import com.aldo.e_commerce.dto.auth.NombreUsuarioRequest;
import com.aldo.e_commerce.dto.auth.RefreshTokenRequest;
import com.aldo.e_commerce.dto.auth.RegistroRequest;
import com.aldo.e_commerce.dto.auth.RegistroResponse;
import com.aldo.e_commerce.dto.response.ApiResponsee;
import com.aldo.e_commerce.service.interfaces.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(
    name = "Auth",
    description = "Registros | Inicios de sesión | Cierres de sesión | Refresh Access Tokens"
)

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
        
    private final AuthService authService;

@Operation(
        summary = "Registro de usuarios",
        description = "Permite a usuarios registrarse dentro del sistema"
)
@ApiResponses({
        @ApiResponse(
                responseCode = "201",
                description = "Usuario creado correctamente"
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Datos inválidos"
        )
})

    @PostMapping("/registro")
    public ResponseEntity<ApiResponsee<RegistroResponse>> registro(@Valid @RequestBody RegistroRequest registroRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponsee<>(true, "Registro exitoso", authService.registro(registroRequest)));
    }


@Operation(
        summary = "Inicio de sesión",
        description = "Permite a usuarios iniciar sesión en sus cuentas"
)
@ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Inicio de sesión correcto"
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

    @PostMapping("/login")
    public ResponseEntity<ApiResponsee<AuthResponse>> login(@Valid @RequestBody LoginRequest logingRequest) {
        return ResponseEntity.ok(new ApiResponsee<>(true, "Login exitoso", authService.login(logingRequest)));
    }


@Operation(
        summary = "Generar Access Tokens",
        description = "Permite a usuarios generar nuevos Access Tokens"
)
@ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Access Token generado correctamente"
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Datos inválidos"
        )
})

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponsee<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        return ResponseEntity.ok(new ApiResponsee<>(true, "Access token generado correctamente,", authService.refreshToken(refreshTokenRequest.getRefreshToken())));
    }


@Operation(
        summary = "Cerrar sesión",
        description = "Permite a usuarios cerrar sesión en sus cuentas"
)
@ApiResponses({
        @ApiResponse(
                responseCode = "204",
                description = "Sesión cerrada correctamente"
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Datos inválidos"
        )
})

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest refreshTokenRequest) {
        authService.logout(refreshTokenRequest.getRefreshToken());
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public ResponseEntity<MeResponse> me() {
        return ResponseEntity.ok(authService.me());
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/actualizar-nombre")
    public ResponseEntity<Void> actualizarNombre(@Valid @RequestBody NombreUsuarioRequest nombreUsuariorequest){

        authService.actualizarNombre(nombreUsuariorequest);
        
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/actualizar-email")
    public ResponseEntity<Void> actualizarEmail(@Valid @RequestBody EmailUsuarioRequest emailUsuariorequest){

        authService.actualizarEmail(emailUsuariorequest);
        
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/actualizar-contraseña")
    public ResponseEntity<Void> actualizarContraseña(@Valid @RequestBody ContraseñaUsuarioRequest contraseñalUsuariorequest){

        authService.actualizarContraseña(contraseñalUsuariorequest);
        
        return ResponseEntity.ok().build();
    }
    
}
