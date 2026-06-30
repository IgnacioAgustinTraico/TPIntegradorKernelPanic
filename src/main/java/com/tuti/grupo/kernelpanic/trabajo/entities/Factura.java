package com.tuti.grupo.kernelpanic.trabajo.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "factura")
public class Factura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer nroFactura;

    @Column(nullable = false)
    private LocalDate fechaEmision;

    @Column(nullable = false)
    private LocalDate fechaVencimiento;

    @Column(name = "importe", nullable = false) 
    private BigDecimal importe;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoFactura estado = EstadoFactura.PENDIENTE;

    @Column(name = "concepto_facturado", nullable = false)
    private String conceptoFacturado;
    
    private LocalDate fechaPago;

    @Enumerated(EnumType.STRING)
    private MedioPago medioPago;

    private BigDecimal importePagado;
    private BigDecimal interesImporte;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contrato_id", nullable = false)
    private Contrato contrato;

    private boolean eliminado = false;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "factura_historial_estados", joinColumns = @JoinColumn(name = "factura_id"))
    private List<RegistroCambioEstado> historialEstados = new ArrayList<>();

    @Embeddable
    public static class RegistroCambioEstado {
        @Enumerated(EnumType.STRING)
        private EstadoFactura estado;
        
        private LocalDateTime fechaCambio;

        public RegistroCambioEstado() {}

        public RegistroCambioEstado(EstadoFactura estado) {
            this.estado = estado;
            this.fechaCambio = LocalDateTime.now(); 
        }

        public EstadoFactura getEstado() { return estado; }
        public void setEstado(EstadoFactura estado) { this.estado = estado; }
        public LocalDateTime getFechaCambio() { return fechaCambio; }
        public void setFechaCambio(LocalDateTime fechaCambio) { this.fechaCambio = fechaCambio; }
    }

    public Factura() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getConceptoFacturado() { return conceptoFacturado; }
    public void setConceptoFacturado(String conceptoFacturado) { this.conceptoFacturado = conceptoFacturado; }
    public Integer getNroFactura() { return nroFactura; }
    public void setNroFactura(Integer nroFactura) { this.nroFactura = nroFactura; }
    public LocalDate getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDate fechaEmision) { this.fechaEmision = fechaEmision; }
    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }
    public BigDecimal getImporte() { return importe; }
    public void setImporte(BigDecimal importe) { this.importe = importe; }
    public EstadoFactura getEstado() { return estado; }
    public void setEstado(EstadoFactura estado) { this.estado = estado; }
    public LocalDate getFechaPago() { return fechaPago; }
    public void setFechaPago(LocalDate fechaPago) { this.fechaPago = fechaPago; }
    public MedioPago getMedioPago() { return medioPago; }
    public void setMedioPago(MedioPago medioPago) { this.medioPago = medioPago; }
    public BigDecimal getImportePagado() { return importePagado; }
    public void setImportePagado(BigDecimal importePagado) { this.importePagado = importePagado; }
    public BigDecimal getInteresImporte() { return interesImporte; }
    public void setInteresImporte(BigDecimal interesImporte) { this.interesImporte = interesImporte; }
    public Contrato getContrato() { return contrato; }
    public void setContrato(Contrato contrato) { this.contrato = contrato; }
    public boolean isEliminado() { return eliminado; }
    public void setEliminado(boolean eliminado) { this.eliminado = eliminado; }

    public List<RegistroCambioEstado> getHistorialEstados() { return historialEstados; }
    public void setHistorialEstados(List<RegistroCambioEstado> historialEstados) { this.historialEstados = historialEstados; }
}