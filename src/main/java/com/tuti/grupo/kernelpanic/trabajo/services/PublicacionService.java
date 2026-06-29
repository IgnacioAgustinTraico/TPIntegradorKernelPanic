package com.tuti.grupo.kernelpanic.trabajo.services;

import com.tuti.grupo.kernelpanic.trabajo.entities.Publicacion;
import com.tuti.grupo.kernelpanic.trabajo.entities.EstadoPublicacion;
import com.tuti.grupo.kernelpanic.trabajo.entities.EstadoPropiedad;
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

    // Obtener las publicaciones para el listado de la pantalla
    public List<Publicacion> obtenerTodasActivas() {
        return publicacionRepository.findByEliminadaFalse();
    }

    public Publicacion buscarPorId(Long id) {
        return publicacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("La publicación no existe."));
    }

    // FLUJO ALTA (HU 2.1) con las validaciones del equipo
    public Publicacion guardarPublicacion(Publicacion publicacion) {
        
        // 1. Validación de campos obligatorios
        if (publicacion.getPrecioMensual() == null) {
            throw new RuntimeException("El precio mensual es obligatorio.");
        }
        if (publicacion.getPropiedad() == null) {
            throw new RuntimeException("Debe seleccionar una propiedad.");
        }

        // 2. Validación de números positivos
        if (publicacion.getPrecioMensual().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("El precio mensual debe ser un valor positivo mayor a cero.");
        }

        // 3. Regla de negocio: la propiedad debe estar DISPONIBLE
        if (publicacion.getPropiedad().getEstadoDisponibilidad() != EstadoPropiedad.DISPONIBLE) {
            throw new RuntimeException("No se puede publicar esta propiedad porque no está disponible (está reservada, alquilada o inactiva).");
        }

        // 4. Regla de negocio: no duplicar publicaciones activas para la misma propiedad
        if (publicacion.getId() == null) { // Si es una publicación nueva
            boolean yaExisteActiva = publicacionRepository.existsByPropiedadIdAndEstadoAndEliminadaFalse(
                    publicacion.getPropiedad().getId(), EstadoPublicacion.ACTIVA);
            if (yaExisteActiva) {
                throw new RuntimeException("Esta propiedad ya cuenta con una publicación activa.");
            }
            // Setea la fecha de publicación al día de hoy
            publicacion.setFechaPublicacion(LocalDate.now());
        }

        return publicacionRepository.save(publicacion);
    }

    // FLUJO ELIMINACIÓN LÓGICA (HU 2.2)
    public void eliminarPublicacion(Long id) {
        Publicacion pub = buscarPorId(id);
        
        // Regla de negocio: No se puede eliminar si ya está FINALIZADA
        if (pub.getEstado() == EstadoPublicacion.FINALIZADA) {
            throw new RuntimeException("No se puede eliminar una publicación que ya ha sido finalizada.");
        }
        
        // Marcamos borrado lógico
        pub.setEliminada(true);
        publicacionRepository.save(pub);
    }
}