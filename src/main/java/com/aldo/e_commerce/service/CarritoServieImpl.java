package com.aldo.e_commerce.service;

import java.util.ArrayList;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aldo.e_commerce.dto.carrito.AddProductoRequest;
import com.aldo.e_commerce.dto.carrito.CarritoResponse;
import com.aldo.e_commerce.entity.Carrito;
import com.aldo.e_commerce.entity.CarritoItem;
import com.aldo.e_commerce.entity.Producto;
import com.aldo.e_commerce.entity.Usuario;
import com.aldo.e_commerce.exception.ProductoNoDisponibleException;
import com.aldo.e_commerce.exception.ProductoNoEncontradoException;
import com.aldo.e_commerce.exception.StockInsuficienteException;
import com.aldo.e_commerce.mapper.CarritoMapper;
import com.aldo.e_commerce.repository.CarritoRepository;
import com.aldo.e_commerce.repository.ProductoRepository;
import com.aldo.e_commerce.repository.UsuarioRepository;
import com.aldo.e_commerce.security.CurrentUserService;
import com.aldo.e_commerce.service.interfaces.CarritoService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CarritoServieImpl implements CarritoService{

    private final CarritoRepository carritoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CurrentUserService currentUserService;

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
    @Transactional
    public CarritoResponse obtenerCarrito() {
        Usuario usuario = currentUserService.obtenerUsuarioActual();
        Carrito carrito = getOrCreateCarrito(usuario);
        return CarritoMapper.toResponse(carrito);
    }

    @Override
    @Transactional
    public CarritoResponse agregarProducto(AddProductoRequest request) {
        Usuario usuario = currentUserService.obtenerUsuarioActual();
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

        int cantidadEnMiCarrito = itemExistente.map(CarritoItem::getCantidad).orElse(0);
        int cantidadSolicitada = request.getCantidad();
        int totalQueQuedaria = cantidadEnMiCarrito + cantidadSolicitada;

        if (totalQueQuedaria > producto.getStock()) {
            throw new StockInsuficienteException(producto.getNombre(), producto.getStock(), cantidadEnMiCarrito, cantidadSolicitada);
        }

        if (itemExistente.isPresent()) {
            itemExistente.get().setCantidad(totalQueQuedaria);
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
    @Transactional
    public CarritoResponse eliminarProducto(Long productoId) {
        Usuario usuario = currentUserService.obtenerUsuarioActual();
        Carrito carrito = getOrCreateCarrito(usuario);

        carrito.getItems().removeIf(i -> i.getProducto().getId().equals(productoId));
        carritoRepository.save(carrito);

        return CarritoMapper.toResponse(carrito);
    }
    
}
