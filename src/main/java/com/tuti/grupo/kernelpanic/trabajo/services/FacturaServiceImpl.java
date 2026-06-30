package com.tuti.grupo.kernelpanic.trabajo.services;

import com.tuti.grupo.kernelpanic.trabajo.entities.Factura;
import com.tuti.grupo.kernelpanic.trabajo.entities.EstadoFactura;
import com.tuti.grupo.kernelpanic.trabajo.entities.Contrato;        
import com.tuti.grupo.kernelpanic.trabajo.entities.EstadoContrato;  
import com.tuti.grupo.kernelpanic.trabajo.repositories.FacturaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.tuti.grupo.kernelpanic.trabajo.repositories.ContratoRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class FacturaServiceImpl implements FacturaService {

    @Autowired
    private FacturaRepository facturaRepository;

    @Autowired
    private ContratoRepository contratoRepository;
    
    @Override
    @Transactional(readOnly = true)
    public List<Factura> listarTodas() {
        return facturaRepository.findByEliminadoFalse();
    }

    @Override
    @Transactional(readOnly = true)
    public Factura obtenerPorId(Long id) {
        return facturaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la factura con ID: " + id));
    }

    @Override
    @Transactional
    public Factura guardarManual(Factura factura) {
        if (factura.getContrato() == null || factura.getContrato().getId() == null) {
            throw new IllegalArgumentException("La factura debe estar asociada a un contrato.");
        }
        if (factura.getConceptoFacturado() == null || factura.getConceptoFacturado().trim().isEmpty()) {
            throw new IllegalArgumentException("El concepto de la factura es obligatorio.");
        }
        
        Contrato contratoReal = contratoRepository.findById(factura.getContrato().getId())
                .orElseThrow(() -> new IllegalArgumentException("El contrato solicitado no existe."));
        
        EstadoContrato estadoContrato = contratoReal.getEstado();
        
        if (estadoContrato != EstadoContrato.ACTIVO || contratoReal.isEliminado()) {
            throw new IllegalArgumentException("No se puede crear una factura para un contrato que no esté en estado ACTIVO o que haya sido eliminado.");
        }

        factura.setContrato(contratoReal);

        if (factura.getFechaVencimiento() == null) {
            throw new IllegalArgumentException("La fecha de vencimiento es obligatoria.");
        }
        if (factura.getFechaVencimiento().isBefore(factura.getFechaEmision())) {
            throw new IllegalArgumentException("La fecha de vencimiento no puede ser anterior a la fecha de emisión.");
        }
        if (factura.getImporte() == null || factura.getImporte().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El importe facturado debe ser un número positivo mayor a cero.");
        }

        if (factura.getImportePagado() != null && factura.getImportePagado().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El importe pagado no puede ser un número negativo.");
        }
        if (factura.getInteresImporte() != null && factura.getInteresImporte().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El importe de intereses no puede ser un número negativo.");
        }

        if (factura.getNroFactura() == null) {
            factura.setNroFactura((int) (Math.random() * 100000)); 
        }

        factura.setEstado(EstadoFactura.PENDIENTE);
        factura.setEliminado(false);

        factura.getHistorialEstados().add(new Factura.RegistroCambioEstado(EstadoFactura.PENDIENTE));

        return facturaRepository.save(factura);
    }

    @Override
    @Transactional
    public Factura modificar(Long id, Factura facturaActualizada) {
        Factura facturaExistente = obtenerPorId(id);

        EstadoFactura estadoAnterior = facturaExistente.getEstado();
        EstadoFactura estadoNuevo = facturaActualizada.getEstado();

        if (estadoAnterior == EstadoFactura.ANULADA) {
            throw new IllegalStateException("No se puede modificar una factura que ya está ANULADA.");
        }
        if (estadoAnterior == EstadoFactura.PAGADA) {
            throw new IllegalStateException("No se puede modificar una factura que ya está PAGADA.");
        }

        if (facturaActualizada.getFechaVencimiento().isBefore(facturaExistente.getFechaEmision())) {
            throw new IllegalArgumentException("La fecha de vencimiento no puede ser anterior a la de emisión.");
        }
        if (facturaActualizada.getImporte().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El importe debe ser un número positivo.");
        }

        if (estadoNuevo == EstadoFactura.PAGADA) {
            if (facturaActualizada.getFechaPago() == null || 
                facturaActualizada.getMedioPago() == null || 
                facturaActualizada.getImportePagado() == null) {
                throw new IllegalArgumentException("Si la factura pasa a estado PAGADA, se deben completar todos los datos del pago.");
            }
            if (facturaActualizada.getImportePagado().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("El importe pagado debe ser un número positivo.");
            }
            
            facturaExistente.setFechaPago(facturaActualizada.getFechaPago());
            facturaExistente.setMedioPago(facturaActualizada.getMedioPago());
            facturaExistente.setImportePagado(facturaActualizada.getImportePagado());
            
            if (facturaActualizada.getInteresImporte() != null) {
                facturaExistente.setInteresImporte(facturaActualizada.getInteresImporte());
            } else {
                facturaExistente.setInteresImporte(BigDecimal.ZERO);
            }
            
        } else {
            facturaExistente.setFechaPago(null);
            facturaExistente.setMedioPago(null);
            facturaExistente.setImportePagado(null);
            facturaExistente.setInteresImporte(null);
        }

        if (estadoAnterior != estadoNuevo) {
            facturaExistente.getHistorialEstados().add(new Factura.RegistroCambioEstado(estadoNuevo));
        }

        facturaExistente.setEstado(estadoNuevo);
        facturaExistente.setImporte(facturaActualizada.getImporte());
        facturaExistente.setFechaVencimiento(facturaActualizada.getFechaVencimiento());

        return facturaRepository.save(facturaExistente);
    }
    
    @Override
    @Transactional
    public void eliminarLogico(Long id) {
        Factura factura = obtenerPorId(id);

        if (factura.getEstado() == EstadoFactura.PAGADA) {
            throw new IllegalStateException("No se puede eliminar una factura que se encuentra en estado PAGADA.");
        }
        if (factura.getEstado() != EstadoFactura.ANULADA) {
            factura.getHistorialEstados().add(new Factura.RegistroCambioEstado(EstadoFactura.ANULADA));
            factura.setEstado(EstadoFactura.ANULADA);
        }

        factura.setEliminado(true);
        facturaRepository.save(factura);
    }
}