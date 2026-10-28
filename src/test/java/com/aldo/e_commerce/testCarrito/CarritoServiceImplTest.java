package com.aldo.e_commerce.testCarrito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

import com.aldo.e_commerce.dto.carrito.AddProductoRequest;
import com.aldo.e_commerce.dto.carrito.CarritoResponse;
import com.aldo.e_commerce.entity.Carrito;
import com.aldo.e_commerce.entity.CarritoItem;
import com.aldo.e_commerce.entity.Categoria;
import com.aldo.e_commerce.entity.Producto;
import com.aldo.e_commerce.entity.Role;
import com.aldo.e_commerce.entity.Usuario;
import com.aldo.e_commerce.exception.ProductoNoDisponibleException;
import com.aldo.e_commerce.exception.ProductoNoEncontradoException;
import com.aldo.e_commerce.exception.StockInsuficienteException;
import com.aldo.e_commerce.mapper.CarritoMapper;
import com.aldo.e_commerce.repository.CarritoRepository;
import com.aldo.e_commerce.repository.ProductoRepository;
import com.aldo.e_commerce.repository.UsuarioRepository;
import com.aldo.e_commerce.service.CarritoServieImpl;

@ExtendWith(MockitoExtension.class)
public class CarritoServiceImplTest {
    
    @Mock
    private CarritoRepository carritoRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private CarritoServieImpl carritoServieImpl;

    private Usuario usuario;
    private Categoria categoria;
    private Producto producto;
    private Carrito carrito;
    private CarritoItem carritoItem;
    private AddProductoRequest addProductoRequest;
    private CarritoResponse carritoResponse;

    @BeforeEach
    void setUp(){

        usuario = Usuario.builder()
                        .id(1L)
                        .nombre("Aldo")
                        .email("aldo@test.com")
                        .password("aldo123")
                        .role(Role.USER)
                        .build();

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

        carrito = Carrito.builder()
                .id(1L)
                .usuario(usuario)
                .items(new ArrayList<>())
                .build();

        carritoItem = CarritoItem.builder()
                .id(1L)
                .carrito(carrito)
                .producto(producto)
                .cantidad(2)
                .build();

        carrito.getItems().add(carritoItem);

        addProductoRequest = new AddProductoRequest();
        addProductoRequest.setProductoId(producto.getId());
        addProductoRequest.setCantidad(3);

        carritoResponse = CarritoMapper.toResponse(carrito);

        // --- Simular usuario autenticado en SecurityContext ---
        Authentication auth = new UsernamePasswordAuthenticationToken(usuario.getEmail(), null, List.of());
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);

        // --- Mockear repositorio de usuario ---
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
    }

    // Tests conde todo sale bien (happy path -> camino feliz)
    @Test
    void deberiaObtenerCarrito(){

        when(usuarioRepository.findByEmail(usuario.getEmail()))
                                .thenReturn(Optional.of(usuario));

        when(carritoRepository.findByUsuarioId(usuario.getId()))
                                .thenReturn(Optional.of(carrito));

        CarritoResponse carritoResponse = carritoServieImpl.obtenerCarrito();

        assertNotNull(carritoResponse);

        assertEquals(carrito.getItems().size(), carritoResponse.getItems().size());
        assertEquals(carrito.getItems().get(0).getProducto().getNombre(), carritoResponse.getItems().get(0).getNombre()); // Aqui se obtiene el nombre directamente ya que en la Lista de items que esta dentro de CarritoResponse especificamente el metodo toResponse() ya se hace .getProducto().getNombre()

        verify(carritoRepository).findByUsuarioId(usuario.getId());
        
    }

    @Test
    void deberiaCrearUnCarritoSiNoExiste(){
        
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));

        // Simular que no existe carrito
        when(carritoRepository.findByUsuarioId(usuario.getId())).thenReturn(Optional.empty());

        // Simular guardado de nuevo carrito
        when(carritoRepository.save(any(Carrito.class))).thenReturn(carrito);

        CarritoResponse response = carritoServieImpl.obtenerCarrito();

        assertNotNull(response);
        verify(carritoRepository).save(any(Carrito.class));
    }

    @Test
    void deberiaAgregarProductoAlCarrito(){

        when(usuarioRepository.findByEmail(usuario.getEmail()))
                                .thenReturn(Optional.of(usuario));

        when(carritoRepository.findByUsuarioId(usuario.getId()))
                                .thenReturn(Optional.of(carrito));

        when(productoRepository.findById(addProductoRequest.getProductoId()))
                                .thenReturn(Optional.of(producto));
        
        when(carritoRepository.save(any(Carrito.class))).thenReturn(carrito);

        CarritoResponse carritoResponse = carritoServieImpl.agregarProducto(addProductoRequest);

        assertNotNull(carritoResponse);
        assertEquals(1, carritoResponse.getItems().size());
        assertEquals(producto.getNombre(), carritoResponse.getItems().get(0).getNombre());
        assertEquals(5, carritoResponse.getItems().get(0).getCantidad());

        verify(carritoRepository).save(any(Carrito.class));
    }

    @Test
    void deberiaEliminarProducto(){

        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(carritoRepository.findByUsuarioId(usuario.getId())).thenReturn(Optional.of(carrito));

        when(carritoRepository.save(any(Carrito.class))).thenReturn(carrito);

        CarritoResponse carritoResponse = carritoServieImpl.eliminarProducto(producto.getId());

        assertNotNull(carritoResponse);
        assertTrue(carritoResponse.getItems().isEmpty());
        assertEquals(0.0, carritoResponse.getTotal());

        verify(carritoRepository).save(any(Carrito.class));
    }


    // Tests donde todo sale mal y se lanzan las excepciones (error Path -> saso error)
    @Test
    void deberiaLanzarExcepcionSiNoSeEncuentraElProducto(){

        when(usuarioRepository.findByEmail(usuario.getEmail()))
                                .thenReturn(Optional.of(usuario));

        when(carritoRepository.findByUsuarioId(usuario.getId()))
                                .thenReturn(Optional.of(carrito));

        when(productoRepository.findById(addProductoRequest.getProductoId()))
                                .thenReturn(Optional.empty());
        
        assertThrows(ProductoNoEncontradoException.class, () -> carritoServieImpl.agregarProducto(addProductoRequest));

    }

    @Test
    void deberiaLanzarExcpecionSiElProductoEstaInactivo(){

        when(usuarioRepository.findByEmail(usuario.getEmail()))
                                .thenReturn(Optional.of(usuario));

        when(carritoRepository.findByUsuarioId(usuario.getId()))
                                .thenReturn(Optional.of(carrito));

        producto.setActivo(false);
        when(productoRepository.findById(addProductoRequest.getProductoId())).thenReturn(Optional.of(producto));
        
        assertThrows(ProductoNoDisponibleException.class, () -> carritoServieImpl.agregarProducto(addProductoRequest));
    }

    @Test
    void deberiaLanzarExcepcionCuandoNoHayStockSuficiente(){
        
        when(usuarioRepository.findByEmail(usuario.getEmail()))
                                .thenReturn(Optional.of(usuario));

        when(carritoRepository.findByUsuarioId(usuario.getId()))
                                .thenReturn(Optional.of(carrito));

        producto.setStock(5);
        when(productoRepository.findById(addProductoRequest.getProductoId())).thenReturn(Optional.of(producto));

        addProductoRequest.setCantidad(10);

        assertThrows(StockInsuficienteException.class, () -> carritoServieImpl.agregarProducto(addProductoRequest));
    }


    // Test donde hay errores poco comunes (edge cases -> casos limite)

}
