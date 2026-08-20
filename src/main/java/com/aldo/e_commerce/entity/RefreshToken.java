package com.aldo.e_commerce.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefreshToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;
    
    //private String nombre; Ya no se necesita el nombre del usuario aquí, ya que se puede obtener a través de la relación con el Usuario.

    /*
    Esto es opcional, dependiendo de cómo quieras manejar la relación entre el RefreshToken y el Usuario.
    Si decides mantener una referencia al Usuario, puedes descomentar este código y agregar la anotación @ManyToOne
    para establecer la relación en la base de datos.
    */
    
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    private LocalDateTime expirationDate;
}
