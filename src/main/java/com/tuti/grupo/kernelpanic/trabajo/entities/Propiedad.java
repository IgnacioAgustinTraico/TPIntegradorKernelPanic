package com.tuti.grupo.kernelpanic.trabajo.entities;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "propiedad")
public class Propiedad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 200, nullable = false)
    private String direccion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ciudad_id", nullable = false)
    private Ciudad ciudad;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "propietario_id", nullable = false)
    private Persona propietario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoPropiedad tipo;

    @Column(name = "cantidad_ambientes", nullable = false)
    private Integer cantidadAmbientes;

    @Column(name = "metros_cuadrados", nullable = false)
    private Double metrosCuadrados;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(columnDefinition = "TEXT")
    private String comodidades;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_disponibilidad", nullable = false)
    private EstadoPropiedad estadoDisponibilidad = EstadoPropiedad.DISPONIBLE;

    @Column(nullable = false)
    private Boolean eliminada = false;

    @Column(name = "contrato_activo", nullable = false, columnDefinition = "BIT(1)")
    private Boolean contratoActivo = false;

    @OneToMany(mappedBy = "propiedad", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<HistorialEstado> historial = new ArrayList<>();

    public Propiedad() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public Ciudad getCiudad() {
        return ciudad;
    }

    public void setCiudad(Ciudad ciudad) {
        this.ciudad = ciudad;
    }

    public Persona getPropietario() {
        return propietario;
    }

    public void setPropietario(Persona propietario) {
        this.propietario = propietario;
    }

    public TipoPropiedad getTipo() {
        return tipo;
    }

    public void setTipo(TipoPropiedad tipo) {
        this.tipo = tipo;
    }

    public Integer getCantidadAmbientes() {
        return cantidadAmbientes;
    }

    public void setCantidadAmbientes(Integer cantidadAmbientes) {
        this.cantidadAmbientes = cantidadAmbientes;
    }

    public Double getMetrosCuadrados() {
        return metrosCuadrados;
    }

    public void setMetrosCuadrados(Double metrosCuadrados) {
        this.metrosCuadrados = metrosCuadrados;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getComodidades() {
        return comodidades;
    }

    public void setComodidades(String comodidades) {
        this.comodidades = comodidades;
    }

    public EstadoPropiedad getEstadoDisponibilidad() {
        return estadoDisponibilidad;
    }

    public void setEstadoDisponibilidad(EstadoPropiedad estadoDisponibilidad) {
        this.estadoDisponibilidad = estadoDisponibilidad;
    }

    public Boolean getEliminada() {
        return eliminada;
    }

    public void setEliminada(Boolean eliminada) {
        this.eliminada = eliminada;
    }

    public Boolean getContratoActivo() {
        return contratoActivo;
    }

    public void setContratoActivo(Boolean contratoActivo) {
        this.contratoActivo = contratoActivo;
    }

    public List<HistorialEstado> getHistorial() {
        return historial;
    }

    public void setHistorial(List<HistorialEstado> historial) {
        this.historial = new ArrayList<>();
        if (historial != null) {
            historial.forEach(this::agregarHistorial);
        }
    }

    public void agregarHistorial(HistorialEstado historialEstado) {
        if (historialEstado == null) {
            return;
        }
        historialEstado.setPropiedad(this);
        this.historial.add(historialEstado);
    }
}
