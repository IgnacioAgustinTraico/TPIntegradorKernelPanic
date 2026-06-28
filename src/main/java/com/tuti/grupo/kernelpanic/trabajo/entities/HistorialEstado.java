package com.tuti.grupo.kernelpanic.trabajo.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "historial_estado_propiedad")
public class HistorialEstado {

@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

@Enumerated(EnumType.STRING)
@Column(nullable = false)
private EstadoPropiedad estado;

@Column(name = "fecha_hora", nullable = false)
private LocalDateTime fechaHora;

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "propiedad_id", nullable = false)
private Propiedad propiedad;

public HistorialEstado() {
}

public HistorialEstado(EstadoPropiedad estado, LocalDateTime fechaHora) {
    this.estado = estado;
    this.fechaHora = fechaHora;
}

public Long getId() { return id; }
public void setId(Long id) { this.id = id; }
public EstadoPropiedad getEstado() { return estado; }
public void setEstado(EstadoPropiedad estado) { this.estado = estado; }
public LocalDateTime getFechaHora() { return fechaHora; }
public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
public Propiedad getPropiedad() { return propiedad; }
public void setPropiedad(Propiedad propiedad) { this.propiedad = propiedad; }
}