package com.aldo.e_commerce.service.specifications;

import org.springframework.data.jpa.domain.Specification;

import com.aldo.e_commerce.entity.Producto;

public class ProductoSpecification {

    public static Specification<Producto> tieneCategoria(String categoria) {

        return (root, query, criteriaBuilder) -> {

            if (categoria == null || categoria.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("categoria").get("nombre"),
                    categoria
            );
        };
    }

    public static Specification<Producto> precioMayorOIgual(Double minPrecio) {

        return (root, query, criteriaBuilder) -> {

            if (minPrecio == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.greaterThanOrEqualTo(
                    root.get("precio"),
                    minPrecio
            );
        };
    }

    public static Specification<Producto> precioMenorOIgual(Double maxPrecio) {

        return (root, query, criteriaBuilder) -> {

            if (maxPrecio == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.lessThanOrEqualTo(
                    root.get("precio"),
                    maxPrecio
            );
        };
    }

    public static Specification<Producto> estaActivo() {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.isTrue(root.get("activo"));
    }
}
