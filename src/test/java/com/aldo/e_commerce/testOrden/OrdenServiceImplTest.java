package com.aldo.e_commerce.testOrden;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.aldo.e_commerce.dto.orden.OrdenResponse;
import com.aldo.e_commerce.entity.Carrito;
import com.aldo.e_commerce.entity.CarritoItem;
import com.aldo.e_commerce.entity.Categoria;
import com.aldo.e_commerce.entity.Orden;
import com.aldo.e_commerce.entity.Producto;
import com.aldo.e_commerce.entity.Usuario;
import com.aldo.e_commerce.enums.EstadoOrden;
import com.aldo.e_commerce.enums.Role;
import com.aldo.e_commerce.exception.CarritoInexistenteException;
import com.aldo.e_commerce.exception.CarritoVacioException;
import com.aldo.e_commerce.exception.ProductoNoDisponibleException;
import com.aldo.e_commerce.exception.StockInsuficienteException;
import com.aldo.e_commerce.repository.CarritoItemRepository;
import com.aldo.e_commerce.repository.CarritoRepository;
import com.aldo.e_commerce.repository.OrdenRepository;
import com.aldo.e_commerce.repository.UsuarioRepository;
import com.aldo.e_commerce.service.OrdenServiceImpl;

@ExtendWith(MockitoExtension.class)
public class OrdenServiceImplTest {

    @Mock
    private OrdenRepository ordenRepository;

    @Mock
    private CarritoRepository carritoRepository;

    @Mock
    private CarritoItemRepository carritoItemRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private OrdenServiceImpl ordenServiceImpl;

    private Usuario usuario;
    private Categoria categoria;
    private Producto producto;
    private Carrito carrito;
    private CarritoItem carritoItem;
    private Orden orden;
    
    @BeforeEach
    void setUp() {
        // Usuario simulado
        usuario = Usuario.builder()
                .id(1L)
                .nombre("Aldo")
                .email("aldo@test.com")
                .password("123456")
                .role(Role.USER)
                .build();

        categoria = Categoria.builder()
                .id(1L)
                .nombre("Tecnologia")
                .build();

        // Producto simulado
        producto = Producto.builder()
                .id(1L)
                .nombre("Laptop")
                .descripcion("Laptop Gamer")
                .precio(15000.0)
                .stock(10)
                .categoria(categoria)
                .activo(true)
                .build();

        // Carrito con un item
        carritoItem = CarritoItem.builder()
                .id(1L)
                .carrito(carrito)
                .producto(producto)
                .cantidad(2)
                .build();

        carrito = Carrito.builder()
                .id(1L)
                .usuario(usuario)
                .items(new ArrayList<>(List.of(carritoItem)))
                .build();

        // Orden simulada
        orden = Orden.builder()
                .id(1L)
                .usuario(usuario)
                .total(30000.0)
                .estado(EstadoOrden.PENDIENTE)
                .fecha(LocalDateTime.now())
                .detalles(new ArrayList<>())
                .build();

        // Simular usuario autenticado en SecurityContext
        Authentication auth = new UsernamePasswordAuthenticationToken(usuario.getEmail(), null, List.of());
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);

        // Mock básico de repositorios
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        
    }

    // Happy path
    @Test
    void deberiaHacerCheckout(){

        when(carritoRepository.findByUsuarioId(usuario.getId())).thenReturn(Optional.of(carrito));
        when(ordenRepository.save(any(Orden.class))).thenReturn(orden);

        // Ejecutar método
        OrdenResponse response = ordenServiceImpl.checkout();

        // Validaciones
        assertNotNull(response);
        assertEquals(30000.0, response.getTotal()); // 15000 * 2
        assertTrue(carrito.getItems().isEmpty());   // carrito se vacía

        // Verificar interacción con repositorios
        verify(ordenRepository).save(any(Orden.class));
    }

    @Test
    void deberiaMostrarElHistorial(){

        when(ordenRepository.findByUsuarioId(usuario.getId())).thenReturn(List.of(orden));

        List<OrdenResponse> ordenResponse = ordenServiceImpl.historial();

        assertNotNull(ordenResponse);
        assertEquals(1, ordenResponse.size());
        assertEquals(30000.0, ordenResponse.get(0).getTotal());
        assertEquals("PENDIENTE", ordenResponse.get(0).getEstado());
    }


    // Error path
    @Test
    void deberiaLanzarExcepcionAlIntentarHacerCheckoutSinCarrito(){

        when(carritoRepository.findByUsuarioId(usuario.getId())).thenReturn(Optional.empty());

        assertThrows(
                CarritoInexistenteException.class,
                () -> ordenServiceImpl.checkout());
    }

    @Test
    void deberiaLanzarExcepcionAlEstarVacioElCarrito(){

        carrito.setItems(new ArrayList<>());
        when(carritoRepository.findByUsuarioId(usuario.getId())).thenReturn(Optional.of(carrito));

        assertThrows(
                CarritoVacioException.class,
                () -> ordenServiceImpl.checkout());
    }

    @Test
    void deberiaLanzarExcepcionSiElProductoNoEstaActivo(){

        when(carritoRepository.findByUsuarioId(usuario.getId())).thenReturn(Optional.of(carrito));

        producto.setActivo(false);

        assertThrows(
                ProductoNoDisponibleException.class,
                () -> ordenServiceImpl.checkout());
    }

    @Test
    void deberiaLanzarExcepcionSiNoHayStockSuficiente(){

        when(carritoRepository.findByUsuarioId(usuario.getId())).thenReturn(Optional.of(carrito));

        producto.setStock(5); 
        carritoItem.setCantidad(10); // Quiero 10 y solo hay 5

        assertThrows(
                StockInsuficienteException.class,
                () -> ordenServiceImpl.checkout());
    }
}
