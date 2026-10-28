package com.aldo.e_commerce.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aldo.e_commerce.dto.producto.ProductoRequest;
import com.aldo.e_commerce.dto.producto.ProductoResponse;
import com.aldo.e_commerce.entity.Categoria;
import com.aldo.e_commerce.entity.Producto;
import com.aldo.e_commerce.exception.CampoDeOrdenamientoInvalidoException;
import com.aldo.e_commerce.exception.CategoriaNoEncontradaException;
import com.aldo.e_commerce.exception.ProductoNoEncontradoException;
import com.aldo.e_commerce.exception.ProductoYaExistenteException;
import com.aldo.e_commerce.exception.TamañoMaximoDePaginaException;
import com.aldo.e_commerce.mapper.ProductoMapper;
import com.aldo.e_commerce.repository.CategoriaRepository;
import com.aldo.e_commerce.repository.ProductoRepository;
import com.aldo.e_commerce.service.interfaces.ProductoService;
import com.aldo.e_commerce.service.specifications.ProductoSpecification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    @Override
    public ProductoResponse crear(ProductoRequest request) {

        if (productoRepository.findByNombre(request.getNombre()).isPresent()) {
            throw new ProductoYaExistenteException(request.getNombre());
        }

        Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new CategoriaNoEncontradaException(request.getCategoriaId()));

        Producto producto = ProductoMapper.toEntity(request, categoria);

        Producto productoGuardado = productoRepository.save(producto);

        return ProductoMapper.toResponse(productoGuardado);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductoResponse> listar(
            int page, // Numero de página
            int size, // Numero de elementos por página
            String sortBy,
            String direction, // Como se ordenan los resultados ya se asc o desc
            String categoria,
            Double minPrecio,
            Double maxPrecio) {

        List<String> camposPermitidos = List.of(
                "id",
                "nombre",
                "precio",
                "stock");

        if (!camposPermitidos.contains(sortBy)) {
            throw new CampoDeOrdenamientoInvalidoException(sortBy);
        }

        if (page < 0) {
            throw new IllegalArgumentException("El número de página no puede ser negativo");
        }

        if (size < 1) {
            throw new IllegalArgumentException("El tamaño de página debe ser mayor a 0");
        }

        if (size > 10) {
            throw new TamañoMaximoDePaginaException();
        }

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        // Esto lo agregue para que en el panel de admin solo el pueda ver los productos aunque esten inactivos
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean esAdmin = auth.getAuthorities().stream().anyMatch(r -> r.getAuthority().equals("ROLE_ADMIN"));

        Specification<Producto> spec = Specification
                // Si es admin puede ver inactivos si es usuario se hace el filtro normal y no puede verlos
                .where((root, query, cb) -> cb.conjunction());

                if(!esAdmin){
                    spec = spec.and(ProductoSpecification.estaActivo());
                }

                spec = spec.and(ProductoSpecification.tieneCategoria(categoria))
                .and(ProductoSpecification.precioMayorOIgual(minPrecio))
                .and(ProductoSpecification.precioMenorOIgual(maxPrecio));

        return productoRepository.findAll(spec, pageable)
                .map(ProductoMapper::toResponse);

        // return
        // productoRepository.findAll().stream().map(ProductoMapper::toResponse).toList();

    }

    @Override
    @Transactional
    public ProductoResponse actualizar(ProductoRequest request, Long id) {

        Producto producto = productoRepository.findById(id).orElseThrow(() -> new ProductoNoEncontradoException(id));

        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setPrecio(request.getPrecio());
        producto.setStock(request.getStock());
        
        if (request.getActivo() != null) {
            producto.setActivo(request.getActivo());
        }

        // Si la categoria del producto existente no coindice con la nueva, es decir que
        // se quiere actualizar la categoria.
        if (!producto.getCategoria().getId().equals(request.getCategoriaId())) {

            // Busco la nueva categoria, si no existe lanza una excepcion.
            Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                    .orElseThrow(() -> new CategoriaNoEncontradaException(request.getCategoriaId()));

            // Si se encontro la nueva categoria, se setea al producto.
            producto.setCategoria(categoria);
        }

        Producto productoActualizado = productoRepository.save(producto);

        return ProductoMapper.toResponse(productoActualizado);

    }

    @Override
    @Transactional
    public void eliminar(Long id) {

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ProductoNoEncontradoException(id));

        producto.setActivo(false);

        productoRepository.save(producto);

        // productoRepository.delete(producto);
    }
}
