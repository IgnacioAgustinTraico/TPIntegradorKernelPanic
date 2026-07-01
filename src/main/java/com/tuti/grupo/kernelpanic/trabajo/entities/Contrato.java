package com.tuti.grupo.kernelpanic.trabajo.entities;

import jakarta.persistence.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "contrato")
public class Contrato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaInicio;

    @Column(nullable = false)
    private Integer duracionMeses;

    @Column(nullable = false)
    private BigDecimal importeMensual;

    @Column(nullable = false)
    private Integer diaVencimientoMensual;

    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoContrato estado = EstadoContrato.BORRADOR; // Estado inicial por defecto

    private boolean eliminado = false;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "propiedad_id", nullable = false)
    private Propiedad propiedad;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inquilino_id", nullable = false)
    private Persona inquilino;

    @OneToMany(mappedBy = "contrato", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HistorialEstadoContrato> historialEstados = new ArrayList<>();


    public Contrato() {}


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
    public Integer getDuracionMeses() { return duracionMeses; }
    public void setDuracionMeses(Integer duracionMeses) { this.duracionMeses = duracionMeses; }
    public BigDecimal getImporteMensual() { return importeMensual; }
    public void setImporteMensual(BigDecimal importeMensual) { this.importeMensual = importeMensual; }
    public Integer getDiaVencimientoMensual() { return diaVencimientoMensual; }
    public void setDiaVencimientoMensual(Integer diaVencimientoMensual) { this.diaVencimientoMensual = diaVencimientoMensual; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public EstadoContrato getEstado() { return estado; }
    public void setEstado(EstadoContrato estado) { this.estado = estado; }
    public boolean isEliminado() { return eliminado; }
    public void setEliminado(boolean eliminado) { this.eliminado = eliminado; }
    public Propiedad getPropiedad() { return propiedad; }
    public void setPropiedad(Propiedad propiedad) { this.propiedad = propiedad; }
    public Persona getInquilino() { return inquilino; }
    public void setInquilino(Persona inquilino) { this.inquilino = inquilino; }
    public List<HistorialEstadoContrato> getHistorialEstados() { return historialEstados; }
    public void setHistorialEstados(List<HistorialEstadoContrato> historialEstados) { this.historialEstados = historialEstados; }

    public void agregarHistorialEstado(EstadoContrato estado) {
        HistorialEstadoContrato historial = new HistorialEstadoContrato();
        historial.setEstado(estado);
        historial.setFechaHora(LocalDateTime.now());
        historial.setContrato(this);
        this.historialEstados.add(historial);
    }
}