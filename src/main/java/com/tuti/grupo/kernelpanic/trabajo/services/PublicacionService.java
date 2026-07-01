package com.tuti.grupo.kernelpanic.trabajo.services;

import com.tuti.grupo.kernelpanic.trabajo.entities.Publicacion;
import com.tuti.grupo.kernelpanic.trabajo.entities.EstadoPublicacion;
import com.tuti.grupo.kernelpanic.trabajo.entities.EstadoPropiedad;
import com.tuti.grupo.kernelpanic.trabajo.entities.HistorialEstadoPublicacion;
import com.tuti.grupo.kernelpanic.trabajo.repositories.PublicacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class PublicacionService {

    @Autowired
    private PublicacionRepository publicacionRepository;

    public List<Publicacion> obtenerTodasActivas() {
        return publicacionRepository.findByEliminadaFalse();
    }

    public List<Publicacion> filtrar(Long propiedadId, String direccion, String ciudad, EstadoPublicacion estado, BigDecimal precioMin, BigDecimal precioMax) {
        return publicacionRepository.buscarConFiltros(propiedadId, direccion, ciudad, estado, precioMin, precioMax);
    }

    public Publicacion buscarPorId(Long id) {
        return publicacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("La publicación no existe."));
    }

    public Publicacion guardarPublicacion(Publicacion publicacion) {
        if (publicacion.getPrecioMensual() == null) {
            throw new RuntimeException("El precio mensual es obligatorio.");
        }
        if (publicacion.getPropiedad() == null) {
            throw new RuntimeException("Debe seleccionar una propiedad.");
        }

        if (publicacion.getPrecioMensual().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("El precio mensual debe ser un valor positivo mayor a cero.");
        }

        if (publicacion.getPropiedad().getEstadoDisponibilidad() != EstadoPropiedad.DISPONIBLE) {
            throw new RuntimeException("No se puede publicar esta propiedad porque no está disponible (está reservada, alquilada o inactiva).");
        }

        if (publicacion.getId() == null) { // Si es una publicación nueva
            boolean yaExisteActiva = publicacionRepository.existsByPropiedadIdAndEstadoAndEliminadaFalse(
                    publicacion.getPropiedad().getId(), EstadoPublicacion.ACTIVA);
            if (yaExisteActiva) {
                throw new RuntimeException("Esta propiedad ya cuenta con una publicación activa.");
            }
            publicacion.setFechaPublicacion(LocalDate.now());
            if (publicacion.getEstado() == null) {
                publicacion.setEstado(EstadoPublicacion.ACTIVA);
            }
        } else {
            Publicacion publicacionOriginal = buscarPorId(publicacion.getId());
            publicacion.setPropiedad(publicacionOriginal.getPropiedad());
            if (publicacionOriginal.getEstado() == EstadoPublicacion.FINALIZADA && publicacion.getEstado() != EstadoPublicacion.FINALIZADA) {
                throw new RuntimeException("No se puede modificar una publicación FINALIZADA para volverla a un estado anterior.");
            }
            validarTransicionEstadoPublicacion(publicacionOriginal, publicacion);
            if (publicacionOriginal.getEstado() != publicacion.getEstado()) {
                publicacion.agregarHistorialEstado(publicacion.getEstado());
            }
        }

        return publicacionRepository.save(publicacion);
    }

    public void eliminarPublicacion(Long id) {
        Publicacion pub = buscarPorId(id);
        if (pub.getEstado() != EstadoPublicacion.ACTIVA) {
            throw new RuntimeException("Solo se pueden eliminar publicaciones en estado ACTIVA.");
        }
        pub.setEliminada(true);
        publicacionRepository.save(pub);
    }

    private void validarTransicionEstadoPublicacion(Publicacion publicacionOriginal, Publicacion publicacionActualizada) {
        EstadoPublicacion estadoAnterior = publicacionOriginal.getEstado();
        EstadoPublicacion estadoNuevo = publicacionActualizada.getEstado();

        if (estadoAnterior == EstadoPublicacion.FINALIZADA && estadoNuevo == EstadoPublicacion.ACTIVA) {
            throw new RuntimeException("No se puede volver una publicación FINALIZADA a ACTIVA.");
        }

        if (estadoNuevo == EstadoPublicacion.ACTIVA && publicacionActualizada.getPropiedad() != null && publicacionActualizada.getPropiedad().getId() != null) {
            boolean existeOtraActiva = publicacionRepository.existsByPropiedadIdAndEliminadaFalseAndEstadoAndIdNot(
                    publicacionActualizada.getPropiedad().getId(), EstadoPublicacion.ACTIVA, publicacionActualizada.getId());
            if (existeOtraActiva) {
                throw new RuntimeException("No se puede activar la publicación porque ya existe otra publicación activa para la misma propiedad.");
            }
        }
    }
}