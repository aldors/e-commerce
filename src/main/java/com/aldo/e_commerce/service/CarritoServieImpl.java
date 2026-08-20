package com.aldo.e_commerce.service;

import java.util.ArrayList;
import java.util.Optional;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.aldo.e_commerce.dto.addProductoAlCarrito.AddProductoRequest;
import com.aldo.e_commerce.dto.carrito.CarritoResponse;
import com.aldo.e_commerce.entity.Carrito;
import com.aldo.e_commerce.entity.CarritoItem;
import com.aldo.e_commerce.entity.Producto;
import com.aldo.e_commerce.entity.Usuario;
import com.aldo.e_commerce.exception.ProductoNoDisponibleException;
import com.aldo.e_commerce.exception.ProductoNoEncontradoException;
import com.aldo.e_commerce.exception.StockInsuficienteException;
import com.aldo.e_commerce.exception.UsuarioNoEncontradoException;
import com.aldo.e_commerce.mapper.CarritoMapper;
import com.aldo.e_commerce.repository.CarritoRepository;
import com.aldo.e_commerce.repository.ProductoRepository;
import com.aldo.e_commerce.repository.UsuarioRepository;
import com.aldo.e_commerce.service.interfaces.CarritoService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CarritoServieImpl implements CarritoService{

    private final CarritoRepository carritoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    private Usuario getUsuarioActual() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsuarioNoEncontradoException());
    }

    private Carrito getOrCreateCarrito(Usuario usuario) {
        return carritoRepository.findByUsuarioId(usuario.getId())
                .orElseGet(() -> {
                    Carrito nuevo = Carrito.builder()
                            .usuario(usuario)
                            .items(new ArrayList<>())
                            .build();
                    return carritoRepository.save(nuevo);
                });
    }

    @Override
    public CarritoResponse obtenerCarrito() {
        Usuario usuario = getUsuarioActual();
        Carrito carrito = getOrCreateCarrito(usuario);
        return CarritoMapper.toResponse(carrito);
    }

    @Override
    public CarritoResponse agregarProducto(AddProductoRequest request) {
        Usuario usuario = getUsuarioActual();
        Carrito carrito = getOrCreateCarrito(usuario);

        // Para saber si existe en la tabla productos el producto que se quiere agregar al carrito
        Producto producto = productoRepository.findById(request.getProductoId())
                .orElseThrow(() -> new ProductoNoEncontradoException(request.getProductoId()));

        // Si el producto si existe, entonces ahora se comprueba su disponibilidad
        if(!producto.getActivo()){
            throw new ProductoNoDisponibleException(producto.getNombre());
        }

        Optional<CarritoItem> itemExistente = carrito.getItems()
        .stream()
        .filter(i -> i.getProducto().getId().equals(producto.getId()))
        .findFirst();

        // El stock aqui no se descuenta, y eso genera problemas, por eso se hace esto.
        /*
        stock 10
        se agregan 4
        stock no se descuenta, sigue habiendo 10
        se agregan 10 al carrito
        ERROR: stock es 10 y se quieren agregar 14 unidades

        y eso se controla en la siguiente línea
        */
        int nuevaCantidad = itemExistente
            .map(i -> i.getCantidad() + request.getCantidad())
            .orElse(request.getCantidad());
        
        // No se podra agregar al carrito una cantidad de un producto mayor a la que hay en stock de ese producto
        if(nuevaCantidad > producto.getStock()){
            throw new StockInsuficienteException(producto.getNombre(), producto.getStock());
        }

        if (itemExistente.isPresent()) {
            itemExistente.get().setCantidad(
                    itemExistente.get().getCantidad() + request.getCantidad()
            );
        } else {
            CarritoItem nuevoItem = CarritoItem.builder()
                    .carrito(carrito)
                    .producto(producto)
                    .cantidad(request.getCantidad())
                    .build();

            carrito.getItems().add(nuevoItem);
        }

        carritoRepository.save(carrito);

        return CarritoMapper.toResponse(carrito);
    }

    @Override
    public CarritoResponse eliminarProducto(Long productoId) {
        Usuario usuario = getUsuarioActual();
        Carrito carrito = getOrCreateCarrito(usuario);

        carrito.getItems().removeIf(i -> i.getProducto().getId().equals(productoId));
        carritoRepository.save(carrito);

        return CarritoMapper.toResponse(carrito);
    }
    
}
