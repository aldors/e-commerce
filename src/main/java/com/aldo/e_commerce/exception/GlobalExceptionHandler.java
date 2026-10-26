package com.aldo.e_commerce.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Validaciones @valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationErrors(MethodArgumentNotValidException ex) {

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.toList());

        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Error de validación",
                errors
        );

        return ResponseEntity.badRequest().body(apiError);
    }

    
    // Para errores de autenticación, como credenciales incorrectas
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException ex) {

        ApiError apiError = new ApiError(
                HttpStatus.UNAUTHORIZED.value(),
                "Credenciales incorrectas",
                List.of(ex.getMessage())
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiError);
    }

    // Cuenta desactivada
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiError> handleDisabled(DisabledException ex) {

        ApiError apiError = new ApiError(
                HttpStatus.FORBIDDEN.value(),
                "Cuenta desactivada",
                List.of(ex.getMessage())
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(apiError);
    }

    // Cuenta bloqueada
    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ApiError> handleLocked(LockedException ex) {

        ApiError apiError = new ApiError(
                HttpStatus.FORBIDDEN.value(),
                "Cuenta bloqueada",
                List.of(ex.getMessage())
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(apiError);
    }

    // Manejo general de excepciones no controladas
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneral(Exception ex) {

        ApiError apiError = new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Error interno del servidor",
                List.of(ex.getMessage())
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiError);
    }


    /* Aqui se pueden agregar mas handlers para otras excepciones personalizadas, por ejemplo:
    
    @ExceptionHandler(UsuarioYaExistenteException.class)
    public ResponseEntity<ApiError> handleUsuarioExistente(UsuarioYaExistenteException ex) {

        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Usuario ya existe",
                List.of(ex.getMessage())
        );

        return ResponseEntity.badRequest().body(apiError);
    }
    */

    @ExceptionHandler(EmailYaExistenteException.class)
    public ResponseEntity<ApiError> handleEmailExistente(EmailYaExistenteException ex) {

        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Email ya registrado",
                List.of(ex.getMessage())
        );

        return ResponseEntity.badRequest().body(apiError);
    }

    @ExceptionHandler(RefreshTokenNoEncontradoException.class)
    public ResponseEntity<ApiError> handleRefreshTokenNoEncontrado(RefreshTokenNoEncontradoException ex) {

        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Refresh token no encontrado",
                List.of(ex.getMessage())
        );

        return ResponseEntity.badRequest().body(apiError);
    }

    @ExceptionHandler(RefreshTokenExpiradoException.class)
    public ResponseEntity<ApiError> handleRefreshTokenExpirado(RefreshTokenExpiradoException ex) {

        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Refresh token expirado",
                List.of(ex.getMessage())
        );

        return ResponseEntity.badRequest().body(apiError);
    }

    @ExceptionHandler(CategoriaNoEncontradaException.class)
    public ResponseEntity<ApiError> handleCategoriaNoEncontrada(CategoriaNoEncontradaException ex) {

        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Categoria no encontrada",
                List.of(ex.getMessage())
        );

        return ResponseEntity.badRequest().body(apiError);
    }

    @ExceptionHandler(CategoriaYaExistenteException.class)
    public ResponseEntity<ApiError> handleCategoriaYaExistente(CategoriaYaExistenteException ex) {

        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Categoria ya existente",
                List.of(ex.getMessage())
        );

        return ResponseEntity.badRequest().body(apiError);
    }

    @ExceptionHandler(ProductoNoEncontradoException.class)
    public ResponseEntity<ApiError> handleProductoNoEncontrado(ProductoNoEncontradoException ex) {

        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Producto no encontrado",
                List.of(ex.getMessage())
        );

        return ResponseEntity.badRequest().body(apiError);
    }

    @ExceptionHandler(ProductoYaExistenteException.class)
    public ResponseEntity<ApiError> handleProductoYaExistente(ProductoYaExistenteException ex) {

        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Producto ya existente",
                List.of(ex.getMessage())
        );

        return ResponseEntity.badRequest().body(apiError);
    }

    @ExceptionHandler(UsuarioNoEncontradoException.class)
    public ResponseEntity<ApiError> handleUsuarioNoEncontrado(UsuarioNoEncontradoException ex) {

        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Usuario no encontrado",
                List.of(ex.getMessage())
        );

        return ResponseEntity.badRequest().body(apiError);
    }

    @ExceptionHandler(CarritoVacioException.class)
    public ResponseEntity<ApiError> handleCarritoVacio(CarritoVacioException ex) {

        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "El carrito está vacio",
                List.of(ex.getMessage())
        );

        return ResponseEntity.badRequest().body(apiError);
    }

    @ExceptionHandler(StockInsuficienteException.class)
    public ResponseEntity<ApiError> handleStockInsuficiente(StockInsuficienteException ex) {

        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Stock insuficiente",
                List.of(ex.getMessage())
        );

        return ResponseEntity.badRequest().body(apiError);
    }

    @ExceptionHandler(ProductoNoDisponibleException.class)
    public ResponseEntity<ApiError> handleProductoNoDisponible(ProductoNoDisponibleException ex) {

        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Producto no dispinible",
                List.of(ex.getMessage())
        );

        return ResponseEntity.badRequest().body(apiError);
    }

    @ExceptionHandler(CampoDeOrdenamientoInvalidoException.class)
    public ResponseEntity<ApiError> handleCampoDeOrdenamientoInvalido(CampoDeOrdenamientoInvalidoException ex) {

        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Campo de ordenamiento inválido",
                List.of(ex.getMessage())
        );

        return ResponseEntity.badRequest().body(apiError);
    }
    
    @ExceptionHandler(TamañoMaximoDePaginaException.class)
    public ResponseEntity<ApiError> handleTamañoMaximoDePagina(TamañoMaximoDePaginaException ex) {

        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Tamaño máximo de pagina inválido",
                List.of(ex.getMessage())
        );

        return ResponseEntity.badRequest().body(apiError);
    }

    @ExceptionHandler(CarritoInexistenteException.class)
    public ResponseEntity<ApiError> handleCarritoInexistente(CarritoInexistenteException ex) {

        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Carrito inexistente",
                List.of(ex.getMessage())
        );

        return ResponseEntity.badRequest().body(apiError);
    }

    @ExceptionHandler(NuevoNombreDiferenteException.class)
    public ResponseEntity<ApiError> handleNuevoNombreDiferente(NuevoNombreDiferenteException ex) {

        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Nombre invalido",
                List.of(ex.getMessage())
        );

        return ResponseEntity.badRequest().body(apiError);
    }

    @ExceptionHandler(NuevoEmailDiferenteException.class)
    public ResponseEntity<ApiError> handleNuevoEmailDiferente(NuevoEmailDiferenteException ex) {

        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Email invalido",
                List.of(ex.getMessage())
        );

        return ResponseEntity.badRequest().body(apiError);
    }

    @ExceptionHandler(NuevaContraseñaDiferenteException.class)
    public ResponseEntity<ApiError> handleNuevaConstraseñaDiferente(NuevaContraseñaDiferenteException ex) {

        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Contraseña invalida",
                List.of(ex.getMessage())
        );

        return ResponseEntity.badRequest().body(apiError);
    }

    @ExceptionHandler(ContraseñaActualIncorrecta.class)
    public ResponseEntity<ApiError> handleContraseñaActualIncorrecta(ContraseñaActualIncorrecta ex) {

        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Contraseña incorrecta",
                List.of(ex.getMessage())
        );

        return ResponseEntity.badRequest().body(apiError);
    }
}
