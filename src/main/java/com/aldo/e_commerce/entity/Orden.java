package com.aldo.e_commerce.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.aldo.e_commerce.enums.EstadoOrden;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ordenes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Orden {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    private Double total;

    @Enumerated(EnumType.STRING)
    private EstadoOrden estado;

    private LocalDateTime fecha;

    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL)
    private List<OrdenDetalle> detalles = new ArrayList<>();

    @PrePersist
    public void prePersist() {

        LocalDateTime ahora = LocalDateTime.now();
        this.fecha = ahora;
    }
}
