package com.tuti.grupo.kernelpanic.trabajo.services;

import com.tuti.grupo.kernelpanic.trabajo.entities.Ciudad;
import com.tuti.grupo.kernelpanic.trabajo.entities.EstadoPropiedad;
import com.tuti.grupo.kernelpanic.trabajo.entities.HistorialEstado;
import com.tuti.grupo.kernelpanic.trabajo.entities.Propiedad;
import com.tuti.grupo.kernelpanic.trabajo.entities.TipoPropiedad;
import com.tuti.grupo.kernelpanic.trabajo.repositories.CiudadRepository;
import com.tuti.grupo.kernelpanic.trabajo.repositories.PropiedadRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PropiedadService {

    private final PropiedadRepository propiedadRepository;
    private final CiudadRepository ciudadRepository;

    public PropiedadService(PropiedadRepository propiedadRepository, CiudadRepository ciudadRepository) {
        this.propiedadRepository = propiedadRepository;
        this.ciudadRepository = ciudadRepository;
    }

    public List<Propiedad> obtenerTodasActivas() {
        return propiedadRepository.filtrarPropiedadesActivas(null, null, null, null);
    }

    public List<Propiedad> filtrar(String direccion, String ciudad, TipoPropiedad tipo, EstadoPropiedad estado) {
        return propiedadRepository.filtrarPropiedadesActivas(direccion, ciudad, tipo, estado);
    }

    public Optional<Propiedad> buscarPorId(Long id) {
        return propiedadRepository.findById(id).filter(propiedad -> !Boolean.TRUE.equals(propiedad.getEliminada()));
    }

    public Propiedad guardarPropiedad(Propiedad propiedad, String ciudadNombre) {
        validarCamposObligatorios(propiedad, ciudadNombre);

        if (propiedad.getEstadoDisponibilidad() == null) {
            propiedad.setEstadoDisponibilidad(EstadoPropiedad.DISPONIBLE);
        }

        if (propiedad.getEliminada() == null) {
            propiedad.setEliminada(false);
        }

        if (propiedad.getContratoActivo() == null) {
            propiedad.setContratoActivo(Boolean.FALSE);
        }

        Ciudad ciudad = obtenerOCrearCiudad(ciudadNombre);
        propiedad.setCiudad(ciudad);

        if (propiedad.getEstadoDisponibilidad() == EstadoPropiedad.ALQUILADA) {
            propiedad.setContratoActivo(true);
        }

        if (propiedad.getId() == null) {
            Long idEvaluar = -1L;
            if (propiedadRepository.existsByDireccionAndCiudadAndEliminadaFalseAndIdNot(
                    propiedad.getDireccion().trim(), ciudad, idEvaluar)) {
                throw new IllegalArgumentException("Ya existe una propiedad activa con esa dirección y ciudad.");
            }

            if (propiedad.getHistorial() == null) {
                propiedad.setHistorial(new java.util.ArrayList<>());
            }
            agregarHistorial(propiedad, propiedad.getEstadoDisponibilidad());
            return propiedadRepository.save(propiedad);
        } else {
            Propiedad propiedadAnterior = buscarPorId(propiedad.getId()).orElse(null);
            if (propiedadAnterior != null) {
                Long idEvaluar = propiedad.getId();
                if (propiedadRepository.existsByDireccionAndCiudadAndEliminadaFalseAndIdNot(
                        propiedad.getDireccion().trim(), ciudad, idEvaluar)) {
                    throw new IllegalArgumentException("Ya existe una propiedad activa con esa dirección y ciudad.");
                }

                if (Boolean.TRUE.equals(propiedadAnterior.getContratoActivo())
                        && (propiedad.getEstadoDisponibilidad() == EstadoPropiedad.DISPONIBLE
                        || propiedad.getEstadoDisponibilidad() == EstadoPropiedad.INACTIVA)
                        && !propiedadAnterior.getEstadoDisponibilidad().equals(propiedad.getEstadoDisponibilidad())) {
                    throw new IllegalArgumentException("No se puede cambiar el estado a Disponible o Inactiva mientras hay un contrato activo vigente.");
                }

                Propiedad propiedadPersistida = propiedadAnterior;
                propiedadPersistida.setDireccion(propiedad.getDireccion());
                propiedadPersistida.setCiudad(ciudad);
                propiedadPersistida.setPropietario(propiedad.getPropietario());
                propiedadPersistida.setTipo(propiedad.getTipo());
                propiedadPersistida.setCantidadAmbientes(propiedad.getCantidadAmbientes());
                propiedadPersistida.setMetrosCuadrados(propiedad.getMetrosCuadrados());
                propiedadPersistida.setDescripcion(propiedad.getDescripcion());
                propiedadPersistida.setComodidades(propiedad.getComodidades());
                propiedadPersistida.setEliminada(propiedad.getEliminada());
                propiedadPersistida.setContratoActivo(propiedad.getContratoActivo());
                propiedadPersistida.setEstadoDisponibilidad(propiedad.getEstadoDisponibilidad());

                if (propiedadPersistida.getHistorial() == null) {
                    propiedadPersistida.setHistorial(new java.util.ArrayList<>());
                }

                if (!propiedadAnterior.getEstadoDisponibilidad().equals(propiedad.getEstadoDisponibilidad())) {
                    agregarHistorial(propiedadPersistida, propiedad.getEstadoDisponibilidad());
                }

                return propiedadRepository.save(propiedadPersistida);
            }
        }

        return propiedadRepository.save(propiedad);
    }

    public Propiedad eliminarPropiedad(Long id) {
        Propiedad propiedad = buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Propiedad no encontrada."));

        if (Boolean.TRUE.equals(propiedad.getContratoActivo())) {
            throw new IllegalArgumentException("No se puede eliminar una propiedad con un contrato activo vigente.");
        }

        propiedad.setEliminada(true);
        return propiedadRepository.save(propiedad);
    }

    private void validarCamposObligatorios(Propiedad propiedad, String ciudadNombre) {
        if (propiedad == null) {
            throw new IllegalArgumentException("La propiedad es obligatoria.");
        }
        if (!StringUtils.hasText(propiedad.getDireccion())) {
            throw new IllegalArgumentException("La dirección es obligatoria.");
        }
        if (!StringUtils.hasText(ciudadNombre)) {
            throw new IllegalArgumentException("La ciudad es obligatoria.");
        }
        if (propiedad.getTipo() == null) {
            throw new IllegalArgumentException("El tipo de propiedad es obligatorio.");
        }
        if (propiedad.getCantidadAmbientes() == null || propiedad.getCantidadAmbientes() <= 0) {
            throw new IllegalArgumentException("La cantidad de ambientes debe ser un número positivo.");
        }
        if (propiedad.getMetrosCuadrados() == null || propiedad.getMetrosCuadrados() <= 0) {
            throw new IllegalArgumentException("Los metros cuadrados deben ser un número positivo.");
        }
        if (!StringUtils.hasText(propiedad.getDescripcion())) {
            throw new IllegalArgumentException("La descripción es obligatoria.");
        }
        if (propiedad.getEstadoDisponibilidad() == null) {
            throw new IllegalArgumentException("El estado de disponibilidad es obligatorio.");
        }
        if (propiedad.getPropietario() == null || propiedad.getPropietario().getId() == null) {
            throw new IllegalArgumentException("Debe seleccionar un propietario válido.");
        }
    }

    private Ciudad obtenerOCrearCiudad(String nombre) {
        String nombreNormalizado = nombre == null ? null : nombre.trim();
        if (!StringUtils.hasText(nombreNormalizado)) {
            throw new IllegalArgumentException("La ciudad es obligatoria.");
        }
        return ciudadRepository.findFirstByNombreIgnoreCase(nombreNormalizado)
                .orElseGet(() -> ciudadRepository.save(new Ciudad(nombreNormalizado)));
    }

    private void agregarHistorial(Propiedad propiedad, EstadoPropiedad estado) {
        HistorialEstado nuevoHistorial = new HistorialEstado(estado, LocalDateTime.now());
        nuevoHistorial.setPropiedad(propiedad);
        propiedad.getHistorial().add(nuevoHistorial);
    }
}
