package com.aldo.e_commerce.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.aldo.e_commerce.dto.orden.OrdenResponse;
import com.aldo.e_commerce.entity.Carrito;
import com.aldo.e_commerce.entity.CarritoItem;
import com.aldo.e_commerce.entity.EstadoOrden;
import com.aldo.e_commerce.entity.Orden;
import com.aldo.e_commerce.entity.OrdenDetalle;
import com.aldo.e_commerce.entity.Producto;
import com.aldo.e_commerce.entity.Usuario;
import com.aldo.e_commerce.exception.CarritoInexistenteException;
import com.aldo.e_commerce.exception.CarritoVacioException;
import com.aldo.e_commerce.exception.ProductoNoDisponibleException;
import com.aldo.e_commerce.exception.StockInsuficienteException;
import com.aldo.e_commerce.exception.UsuarioNoEncontradoException;
import com.aldo.e_commerce.mapper.OrdenMapper;
import com.aldo.e_commerce.repository.CarritoItemRepository;
import com.aldo.e_commerce.repository.CarritoRepository;
import com.aldo.e_commerce.repository.OrdenRepository;
import com.aldo.e_commerce.repository.UsuarioRepository;
import com.aldo.e_commerce.service.interfaces.OrdenService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class OrdenServiceImpl implements OrdenService{

    private final OrdenRepository ordenRepository;
    private final CarritoRepository carritoRepository;
    private final CarritoItemRepository carritoItemRepository;
    private final UsuarioRepository usuarioRepository;

    private Usuario getUsuarioActual() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsuarioNoEncontradoException());
    }

    @Override
    public OrdenResponse checkout() {
        Usuario usuario = getUsuarioActual();

        //Aqui se intenta obtener el carrito del usuarios, no se usa getOrCreateCarrito (no se por que)
        Carrito carrito = carritoRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new CarritoInexistenteException(usuario.getNombre()));

        if (carrito.getItems().isEmpty()) {
            throw new CarritoVacioException();
        }

        Orden orden = Orden.builder()
                .usuario(usuario)
                .estado(EstadoOrden.PENDIENTE)
                .build();

        List<OrdenDetalle> detalles = new ArrayList<>();

        double total = 0;

        for (CarritoItem item : carrito.getItems()) {

            Producto producto = item.getProducto();

            // Validar la disponibilidad del producto (NO SE SI ESTE CORRECTO AÚN)
            if(!producto.getActivo()){
                throw new ProductoNoDisponibleException(producto.getNombre());
            }

            // Validar stock
            if (producto.getStock() < item.getCantidad()) {
                throw new StockInsuficienteException(producto.getNombre(), producto.getStock());
            }

            // Descontar stock
            producto.setStock(
                    producto.getStock() - item.getCantidad()
            );

            OrdenDetalle detalle = OrdenDetalle.builder()
                    .orden(orden)
                    .producto(producto)
                    .cantidad(item.getCantidad())
                    .precioUnitario(producto.getPrecio())
                    .build();

            detalles.add(detalle);

            total += producto.getPrecio() * item.getCantidad();
        }

        orden.setDetalles(detalles);
        orden.setTotal(total);

        Orden ordenGuardada = ordenRepository.save(orden);

        // Vaciar carrito
        carrito.getItems().clear();

        return OrdenMapper.toResponse(ordenGuardada);
    }

    @Override
    public List<OrdenResponse> historial() {
        Usuario usuario = getUsuarioActual();

        return ordenRepository.findByUsuarioId(usuario.getId())
                .stream()
                .map(OrdenMapper::toResponse)
                .toList();
    }
    
}
