package com.tuti.grupo.kernelpanic.trabajo.entities;

import jakarta.persistence.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(nullable = false)
    private LocalDate fechaPublicacion;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPublicacion estado = EstadoPublicacion.ACTIVA;

    private boolean eliminada = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "propiedad_id", nullable = false)
    private Propiedad propiedad;

    @OneToMany(mappedBy = "publicacion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HistorialEstadoPublicacion> historialEstados = new ArrayList<>();

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
    public List<HistorialEstadoPublicacion> getHistorialEstados() { return historialEstados; }
    public void setHistorialEstados(List<HistorialEstadoPublicacion> historialEstados) { this.historialEstados = historialEstados; }

    public void agregarHistorialEstado(EstadoPublicacion estado) {
        HistorialEstadoPublicacion historial = new HistorialEstadoPublicacion();
        historial.setEstado(estado);
        historial.setFechaHora(LocalDateTime.now());
        historial.setPublicacion(this);
        this.historialEstados.add(historial);
    }

    public void actualizarEstado(EstadoPublicacion nuevoEstado) {
        if (nuevoEstado != null && nuevoEstado != this.estado) {
            this.estado = nuevoEstado;
            agregarHistorialEstado(nuevoEstado);
        } else if (this.historialEstados.isEmpty() && this.estado != null) {
            agregarHistorialEstado(this.estado);
        }
    }
}