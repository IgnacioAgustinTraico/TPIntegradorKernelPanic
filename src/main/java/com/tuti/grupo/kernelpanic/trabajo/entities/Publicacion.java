package com.tuti.grupo.kernelpanic.trabajo.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "publicacion")
public class Publicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal precioMensual;

    @Column(columnDefinition = "TEXT")
    private String condiciones;

    @Column(nullable = false)
    private LocalDate fechaPublicacion;

    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPublicacion estado = EstadoPublicacion.ACTIVA;

    private boolean eliminada = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "propiedad_id", nullable = false)
    private Propiedad propiedad;

    public Publicacion() {}

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public BigDecimal getPrecioMensual() { return precioMensual; }
    public void setPrecioMensual(BigDecimal precioMensual) { this.precioMensual = precioMensual; }
    public String getCondiciones() { return condiciones; }
    public void setCondiciones(String condiciones) { this.condiciones = condiciones; }
    public LocalDate getFechaPublicacion() { return fechaPublicacion; }
    public void setFechaPublicacion(LocalDate fechaPublicacion) { this.fechaPublicacion = fechaPublicacion; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public EstadoPublicacion getEstado() { return estado; }
    public void setEstado(EstadoPublicacion estado) { this.estado = estado; }
    public boolean isEliminada() { return eliminada; }
    public void setEliminada(boolean eliminada) { this.eliminada = eliminada; }
    public Propiedad getPropiedad() { return propiedad; }
    public void setPropiedad(Propiedad propiedad) { this.propiedad = propiedad; }
}