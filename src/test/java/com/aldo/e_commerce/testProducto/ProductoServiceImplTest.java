package com.aldo.e_commerce.testProducto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.domain.Page;

import com.aldo.e_commerce.dto.producto.ProductoRequest;
import com.aldo.e_commerce.dto.producto.ProductoResponse;
import com.aldo.e_commerce.entity.Categoria;
import com.aldo.e_commerce.entity.Producto;
import com.aldo.e_commerce.entity.Usuario;
import com.aldo.e_commerce.enums.Role;
import com.aldo.e_commerce.exception.CampoDeOrdenamientoInvalidoException;
import com.aldo.e_commerce.exception.CategoriaNoEncontradaException;
import com.aldo.e_commerce.exception.ProductoNoEncontradoException;
import com.aldo.e_commerce.exception.ProductoYaExistenteException;
import com.aldo.e_commerce.exception.TamañoMaximoDePaginaException;
import com.aldo.e_commerce.repository.CategoriaRepository;
import com.aldo.e_commerce.repository.ProductoRepository;
import com.aldo.e_commerce.service.ProductoServiceImpl;

@ExtendWith(MockitoExtension.class) // Esto de dice JUnit que use Mockito
public class ProductoServiceImplTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private ProductoServiceImpl productoService;

    private Categoria categoria;

    private Producto producto;

    private ProductoRequest productoRequest;

    private Usuario usuario;

    @BeforeEach // Crea datos reutilizables antes de cada test
    void setUp() {

        categoria = Categoria.builder()
                .id(1L)
                .nombre("Tecnologia")
                .build();

        producto = Producto.builder()
                .id(1L)
                .nombre("Laptop")
                .descripcion("Laptop gamer")
                .precio(15000.0)
                .stock(10)
                .activo(true)
                .categoria(categoria)
                .build();

        productoRequest = new ProductoRequest();
        productoRequest.setNombre("Laptop");
        productoRequest.setDescripcion("Laptop gamer");
        productoRequest.setPrecio(15000.0);
        productoRequest.setStock(10);
        productoRequest.setCategoriaId(1L);


        // Esto se hace aqui solo por que el admin puede ver productos inactivos (esto lo agregue despues) y ya lo habia hecho en otros test
        usuario = Usuario.builder()
            .id(1L)
            .nombre("Admin")
            .email("admin@test.com")
            .password("admin123")
            .role(Role.ADMIN)
            .build();

        // --- Simular usuario autenticado en SecurityContext ---
        Authentication auth = new UsernamePasswordAuthenticationToken(
                usuario.getEmail(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);
    }


    // Tests donde todo sale bien (happy path -> caso feliz)
    @Test
    void deberiaCrearProductoCorrectamente() {

        when(categoriaRepository.findById(1L))
                .thenReturn(Optional.of(categoria));

        when(productoRepository.save(any(Producto.class)))
                .thenReturn(producto);

        ProductoResponse response = productoService.crear(productoRequest);

        assertNotNull(response);

        assertEquals("Laptop", response.getNombre());

        assertEquals(15000.0, response.getPrecio());

        verify(productoRepository).save(any(Producto.class));
    }
    
    // Este mismo test funciona para "direccion = "desc"
    @Test
    void deberiaListarProductosPaginados() {

        Page<Producto> page = new PageImpl<>(List.of(producto));

        when(productoRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);

        Page<ProductoResponse> response = productoService.listar(
                0,
                10,
                "nombre",
                "asc",
                null,
                null,
                null
        );

        assertEquals(1, response.getContent().size());

        assertEquals("Laptop", response.getContent().get(0).getNombre());
    }

    // Mismo test de arriba solo que este es para el admin que si ve los productos inactivos
    @Test
    void deberiaListarProductosIncluyendoInactivosSiEsAdmin() {

        Producto productoInactivo = new Producto(2L, "Mouse", "Mouse gamer", 1000.0, 10, categoria, false);

        Page<Producto> page = new PageImpl<>(List.of(producto, productoInactivo));

        when(productoRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);

        Page<ProductoResponse> response = productoService.listar(
                0,
                10,
                "nombre",
                "asc",
                null,
                null,
                null
        );

        assertEquals(2, response.getContent().size());
        assertEquals("Laptop", response.getContent().get(0).getNombre());
        assertEquals("Mouse", response.getContent().get(1).getNombre());
    }
    
    @Test
    void deberiaActualizarProductoCorrectamente() {

        ProductoRequest requestActualizado = new ProductoRequest();

        requestActualizado.setNombre("Laptop PRO");
        requestActualizado.setDescripcion("Nueva descripcion");
        requestActualizado.setPrecio(20000.0);
        requestActualizado.setStock(5);
        requestActualizado.setCategoriaId(1L);

        producto.setCategoria(new Categoria(2L, "Categoria vieja"));

        when(productoRepository.findById(1L))
                .thenReturn(Optional.of(producto));

        when(categoriaRepository.findById(1L))
                .thenReturn(Optional.of(categoria));

        when(productoRepository.save(any(Producto.class)))
                .thenReturn(producto);

        ProductoResponse response =
                productoService.actualizar(requestActualizado, 1L);

        assertEquals("Laptop PRO", response.getNombre());

        verify(productoRepository).save(any(Producto.class));
    }
    
    @Test
    void deberiaHacerSoftDelete() {

        when(productoRepository.findById(1L))
                .thenReturn(Optional.of(producto));

        when(productoRepository.save(any(Producto.class)))
                .thenReturn(producto);

        productoService.eliminar(1L);

        assertFalse(producto.getActivo());

        verify(productoRepository).save(producto);
    }


    // Tests donde se lanzan excpeciones (error path -> caso error)
    @Test
    void deberiaLanzarExcepcionSiCategoriaNoExiste() {

        when(categoriaRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                CategoriaNoEncontradaException.class,
                () -> productoService.crear(productoRequest)
        );

        verify(productoRepository, never()).save(any());
    }
    
    @Test
    void deberiaLanzarExcepcionAlActualizarProductoInexistente() {

        when(productoRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ProductoNoEncontradoException.class,
                () -> productoService.actualizar(productoRequest, 1L)
        );
    }

    @Test
    void deberiaLanzarExcepcionAlEliminarProductoInexistente() {

        when(productoRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ProductoNoEncontradoException.class,
                () -> productoService.eliminar(1L)
        );

        verify(productoRepository, never()).save(any());
    }

    // Creados por mi
    @Test
    void deberiaLanzarExcepcionAlIntentarCrearProductoYaExistente(){

        when(productoRepository.findByNombre(producto.getNombre()))
                .thenReturn(Optional.of(producto));

        assertThrows(ProductoYaExistenteException.class, () -> productoService.crear(productoRequest));

        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    void deberiaLanzarExcepcionCuandoCampoDeOrdenamientoInvalido(){

        String sortByInvalido = "fechaCreacion";

        assertThrows(CampoDeOrdenamientoInvalidoException.class, () -> 
                productoService.listar(
                        0,
                        5,
                        sortByInvalido,
                        "asc",
                        null,
                        null,
                        null)
        );

        verify(productoRepository, never()).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void deberiaLanzarExcepcionCuandoTamañoDePaginaEsMayorAlPermitido(){

        int tamañoInvalido = 11;

        assertThrows(TamañoMaximoDePaginaException.class, () -> 
                productoService.listar(
                        0,
                        tamañoInvalido,
                        "id",
                        "asc",
                        null,
                        null,
                        null)
        );

        verify(productoRepository, never()).findAll(any(Specification.class), any(Pageable.class));
    }
}
