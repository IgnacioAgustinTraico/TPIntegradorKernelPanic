package com.tuti.grupo.kernelpanic.trabajo.controllers;

import com.tuti.grupo.kernelpanic.trabajo.entities.Factura;
import com.tuti.grupo.kernelpanic.trabajo.entities.Contrato;
import com.tuti.grupo.kernelpanic.trabajo.entities.EstadoContrato;
import com.tuti.grupo.kernelpanic.trabajo.entities.EstadoFactura;
import com.tuti.grupo.kernelpanic.trabajo.entities.MedioPago;
import com.tuti.grupo.kernelpanic.trabajo.services.FacturaService;
import com.tuti.grupo.kernelpanic.trabajo.repositories.ContratoRepository;
import com.tuti.grupo.kernelpanic.trabajo.repositories.FacturaRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/facturas")
public class FacturaController {

    @Autowired
    private FacturaService facturaService;

    @Autowired
    private FacturaRepository facturaRepository;
    
    @Autowired
    private ContratoRepository contratoRepository;

    @GetMapping
    public String listar(
            @RequestParam(value = "contratoId", required = false) Long contratoId,
            @RequestParam(value = "propiedadId", required = false) Long propiedadId,
            @RequestParam(value = "inquilinoId", required = false) Long inquilinoId,
            @RequestParam(value = "estado", required = false) EstadoFactura estado,
            @RequestParam(value = "fechaDesde", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(value = "fechaHasta", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            Model model) {
        
        List<Factura> facturasFiltradas = facturaRepository.buscarConFiltros(contratoId, propiedadId, inquilinoId, estado, fechaDesde, fechaHasta);

        model.addAttribute("facturas", facturasFiltradas);
        model.addAttribute("estados", EstadoFactura.values());

        model.addAttribute("contratoId", contratoId);
        model.addAttribute("propiedadId", propiedadId);
        model.addAttribute("inquilinoId", inquilinoId);
        model.addAttribute("estadoSeleccionado", estado);
        model.addAttribute("fechaDesde", fechaDesde);
        model.addAttribute("fechaHasta", fechaHasta);

        return "facturas-lista";
    }

    @GetMapping("/nueva")
    public String mostrarFormularioNuevo(Model model) {
        Factura factura = new Factura();
        factura.setFechaEmision(LocalDate.now());
        factura.setEstado(EstadoFactura.PENDIENTE); 
        factura.setConceptoFacturado(""); 

        List<Contrato> contratosActivos = contratoRepository.findByEliminadoFalse().stream()
                .filter(c -> c.getEstado() == EstadoContrato.ACTIVO)
                .toList();

        model.addAttribute("factura", factura);
        model.addAttribute("contratos", contratosActivos);
        model.addAttribute("mediosPago", Arrays.asList(MedioPago.values())); 
        model.addAttribute("esEdicion", false);
        
        return "factura-form";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable("id") Long id, Model model) {
        Factura factura = facturaService.obtenerPorId(id);
        
        if (factura.getEstado() == EstadoFactura.PAGADA || factura.getEstado() == EstadoFactura.ANULADA) {
            model.addAttribute("error", "No se puede editar una factura en estado " + factura.getEstado());
            model.addAttribute("facturas", facturaRepository.findByEliminadoFalse());
            model.addAttribute("estados", EstadoFactura.values());
            return "facturas-lista"; 
        }
        
        model.addAttribute("factura", factura);
        model.addAttribute("mediosPago", Arrays.asList(MedioPago.values()));
        
        if (factura.getEstado() == EstadoFactura.VENCIDA) {
            model.addAttribute("estadosPermitidos", Arrays.asList(EstadoFactura.VENCIDA, EstadoFactura.PAGADA));
        } else {
            model.addAttribute("estadosPermitidos", Arrays.asList(EstadoFactura.PENDIENTE, EstadoFactura.PAGADA, EstadoFactura.VENCIDA, EstadoFactura.ANULADA));
        }
        
        return "factura-modificar"; 
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute("factura") Factura factura, Model model) {
        try {
            if (factura.getEstado() == EstadoFactura.PAGADA) {
                if (factura.getFechaPago() == null || factura.getMedioPago() == null || factura.getImportePagado() == null) {
                    throw new IllegalArgumentException("Para marcar la factura como PAGADA debe completar la Fecha, el Medio de Pago y el Importe Pagado Real.");
                }

                if (factura.getImporte() != null && factura.getImportePagado().compareTo(factura.getImporte()) < 0) {
                    throw new IllegalArgumentException("El Importe Pagado Real ($" + factura.getImportePagado() + ") no puede ser menor al Importe Base ($" + factura.getImporte() + ").");
                }
                
                if (factura.getInteresImporte() == null) {
                    factura.setInteresImporte(java.math.BigDecimal.ZERO);
                }
            } else {
                factura.setFechaPago(null);
                factura.setMedioPago(null);
                factura.setImportePagado(null);
                factura.setInteresImporte(null);
            }

            if (factura.getId() == null) {
                facturaService.guardarManual(factura);
            } else {
                facturaService.modificar(factura.getId(), factura);
            }
            return "redirect:/facturas";

        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("mediosPago", Arrays.asList(MedioPago.values()));
            
            if (factura.getContrato() != null && factura.getContrato().getId() != null) {
                Contrato contratoCompleto = contratoRepository.findById(factura.getContrato().getId()).orElse(null);
                factura.setContrato(contratoCompleto);
            }

            model.addAttribute("factura", factura); 

            if (factura.getId() != null) {
                Factura facturaOriginal = facturaService.obtenerPorId(factura.getId());
                if (facturaOriginal.getEstado() == EstadoFactura.VENCIDA) {
                    model.addAttribute("estadosPermitidos", Arrays.asList(EstadoFactura.VENCIDA, EstadoFactura.PAGADA));
                } else {
                    model.addAttribute("estadosPermitidos", Arrays.asList(EstadoFactura.PENDIENTE, EstadoFactura.PAGADA, EstadoFactura.VENCIDA, EstadoFactura.ANULADA));
                }
                return "factura-modificar"; 
            } else {
                List<Contrato> contratosActivos = contratoRepository.findByEliminadoFalse().stream()
                        .filter(c -> c.getEstado() == EstadoContrato.ACTIVO)
                        .toList();
                model.addAttribute("contratos", contratosActivos);
                return "factura-form"; 
            }
        }
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable("id") Long id, Model model) {
        try {
            facturaService.eliminarLogico(id);
            return "redirect:/facturas";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("facturas", facturaRepository.findByEliminadoFalse());
            model.addAttribute("estados", EstadoFactura.values());
            return "facturas-lista";
        }
    }
}