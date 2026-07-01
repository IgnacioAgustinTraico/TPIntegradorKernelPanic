package com.tuti.grupo.kernelpanic.trabajo.services;

import com.tuti.grupo.kernelpanic.trabajo.entities.Contrato;
import com.tuti.grupo.kernelpanic.trabajo.entities.EstadoContrato;
import com.tuti.grupo.kernelpanic.trabajo.entities.EstadoPropiedad;
import com.tuti.grupo.kernelpanic.trabajo.entities.Propiedad;
import com.tuti.grupo.kernelpanic.trabajo.repositories.ContratoRepository;
import com.tuti.grupo.kernelpanic.trabajo.repositories.PropiedadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class ContratoService {

    @Autowired
    private ContratoRepository contratoRepository;

    @Autowired
    private PropiedadRepository propiedadRepository;

    public List<Contrato> obtenerTodosActivos() {
        return contratoRepository.findByEliminadoFalse();
    }

    public List<Contrato> filtrar(String direccion, String inquilinoNombre, EstadoContrato estado, LocalDate fechaInicio) {
        return contratoRepository.buscarConFiltros(direccion, inquilinoNombre, estado, fechaInicio);
    }

    public Contrato buscarPorId(Long id) {
        return contratoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("El contrato solicitado no existe."));
    }

    @Transactional
    public Contrato guardarContrato(Contrato contrato) {
        if (contrato.getFechaInicio() == null) {
            throw new RuntimeException("La fecha de inicio es obligatoria.");
        }
        if (contrato.getDuracionMeses() == null || contrato.getDuracionMeses() <= 0) {
            throw new RuntimeException("La duración en meses debe ser un número positivo mayor a cero.");
        }
        if (contrato.getImporteMensual() == null || contrato.getImporteMensual().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("El importe mensual debe ser un valor positivo mayor a cero.");
        }
        if (contrato.getDiaVencimientoMensual() == null || contrato.getDiaVencimientoMensual() < 1 || contrato.getDiaVencimientoMensual() > 31) {
            throw new RuntimeException("El día de vencimiento debe ser un día válido entre 1 y 31.");
        }
        if (contrato.getPropiedad() == null) {
            throw new RuntimeException("Debe seleccionar una propiedad para el contrato.");
        }
        if (contrato.getInquilino() == null) {
            throw new RuntimeException("Debe asignar un inquilino al contrato.");
        }

        Propiedad propiedad = propiedadRepository.findById(contrato.getPropiedad().getId())
                .orElseThrow(() -> new RuntimeException("La propiedad seleccionada no existe."));

        Contrato contratoOriginal = null;
        if (contrato.getId() != null) {
            contratoOriginal = buscarPorId(contrato.getId());
            if (contratoOriginal.getEstado() == EstadoContrato.ACTIVO
                    && contratoOriginal.getPropiedad() != null
                    && contrato.getPropiedad() != null
                    && !contratoOriginal.getPropiedad().getId().equals(contrato.getPropiedad().getId())) {
                throw new RuntimeException("No se puede cambiar la propiedad de un contrato ACTIVO.");
            }
        }

        if (contrato.getId() == null) {
            if (contrato.getEstado() == EstadoContrato.ACTIVO && propiedad.getEstadoDisponibilidad() != EstadoPropiedad.DISPONIBLE) {
                throw new RuntimeException("No se puede activar el contrato porque la propiedad seleccionada no está DISPONIBLE.");
            }
            if (contrato.getEstado() == null) {
                contrato.setEstado(EstadoContrato.BORRADOR);
            } else if (contrato.getEstado() == EstadoContrato.ACTIVO) {
                propiedad.setEstadoDisponibilidad(EstadoPropiedad.ALQUILADA);
                propiedad.setContratoActivo(true);
                propiedadRepository.save(propiedad);
            }
        } else {
            validarTransicionEstadoContrato(contratoOriginal.getEstado(), contrato.getEstado());

            if (contratoOriginal.getEstado() != EstadoContrato.ACTIVO && contrato.getEstado() == EstadoContrato.ACTIVO) {
                if (propiedad.getEstadoDisponibilidad() != EstadoPropiedad.DISPONIBLE) {
                    throw new RuntimeException("No se puede activar el contrato porque la propiedad ya no está disponible.");
                }
            }

            if (contratoOriginal.getEstado() == EstadoContrato.ACTIVO
                    && (contrato.getEstado() == EstadoContrato.FINALIZADO || contrato.getEstado() == EstadoContrato.RESCINDIDO)) {
                propiedad.setEstadoDisponibilidad(EstadoPropiedad.DISPONIBLE);
                propiedad.setContratoActivo(false);
                propiedadRepository.save(propiedad);
            }

            if (contratoOriginal.getEstado() != EstadoContrato.ACTIVO && contrato.getEstado() == EstadoContrato.ACTIVO) {
                propiedad.setEstadoDisponibilidad(EstadoPropiedad.ALQUILADA);
                propiedad.setContratoActivo(true);
                propiedadRepository.save(propiedad);
            }
        }

        if (contrato.getId() == null) {
            contrato.agregarHistorialEstado(contrato.getEstado());
        } else if (contratoOriginal != null && contratoOriginal.getEstado() != contrato.getEstado()) {
            contratoOriginal.agregarHistorialEstado(contrato.getEstado());
            contratoOriginal.setFechaInicio(contrato.getFechaInicio());
            contratoOriginal.setDuracionMeses(contrato.getDuracionMeses());
            contratoOriginal.setImporteMensual(contrato.getImporteMensual());
            contratoOriginal.setDiaVencimientoMensual(contrato.getDiaVencimientoMensual());
            contratoOriginal.setDescripcion(contrato.getDescripcion());
            contratoOriginal.setEstado(contrato.getEstado());
            contratoOriginal.setInquilino(contrato.getInquilino());
            contratoOriginal.setPropiedad(propiedad);
            return contratoRepository.save(contratoOriginal);
        }

        return contratoRepository.save(contrato);
    }

    @Transactional
    public void eliminarContrato(Long id) {
        Contrato contrato = buscarPorId(id);

        if (contrato.getEstado() != EstadoContrato.BORRADOR) {
            throw new RuntimeException("No se puede eliminar este contrato porque ya fue procesado (activo/finalizado). Solo se permite eliminar contratos en estado BORRADOR.");
        }

        contrato.setEliminado(true);
        contratoRepository.save(contrato);
    }

    private void validarTransicionEstadoContrato(EstadoContrato estadoAnterior, EstadoContrato estadoNuevo) {
        if (estadoAnterior == EstadoContrato.FINALIZADO || estadoAnterior == EstadoContrato.RESCINDIDO) {
            if (estadoNuevo == EstadoContrato.ACTIVO) {
                throw new RuntimeException("No se puede volver a estado ACTIVO desde FINALIZADO o RESCINDIDO.");
            }
        }

        if (estadoAnterior == EstadoContrato.ACTIVO && estadoNuevo == EstadoContrato.BORRADOR) {
            throw new RuntimeException("No se puede volver un contrato ACTIVO a BORRADOR.");
        }
    }

}