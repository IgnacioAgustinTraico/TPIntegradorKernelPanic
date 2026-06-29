package com.tuti.grupo.kernelpanic.trabajo.repositories;

import com.tuti.grupo.kernelpanic.trabajo.entities.Publicacion;
import com.tuti.grupo.kernelpanic.trabajo.entities.EstadoPublicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PublicacionRepository extends JpaRepository<Publicacion, Long> {
    
    // Trae solo las publicaciones activas o pausadas
    List<Publicacion> findByEliminadaFalse();

    // Regla de negocio: Sirve para chequear si una propiedad ya tiene una publicación activa
    boolean existsByPropiedadIdAndEstadoAndEliminadaFalse(Long propiedadId, EstadoPublicacion estado);
}