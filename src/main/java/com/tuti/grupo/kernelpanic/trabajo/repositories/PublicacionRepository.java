package com.tuti.grupo.kernelpanic.trabajo.repositories;

import com.tuti.grupo.kernelpanic.trabajo.entities.Publicacion;
import com.tuti.grupo.kernelpanic.trabajo.entities.EstadoPublicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.math.BigDecimal;

@Repository
public interface PublicacionRepository extends JpaRepository<Publicacion, Long> {

    List<Publicacion> findByEliminadaFalse();

        @Query("""
                        select p from Publicacion p
                        where p.eliminada = false
                            and (:propiedadId is null or p.propiedad.id = :propiedadId)
                            and (:direccion is null or :direccion = '' or lower(p.propiedad.direccion) like lower(concat('%', :direccion, '%')))
                            and (:ciudad is null or :ciudad = '' or lower(p.propiedad.ciudad.nombre) like lower(concat('%', :ciudad, '%')))
                            and (:estado is null or p.estado = :estado)
                            and (:precioMin is null or p.precioMensual >= :precioMin)
                            and (:precioMax is null or p.precioMensual <= :precioMax)
                        order by p.id desc
                        """)
        List<Publicacion> buscarConFiltros(
                        @Param("propiedadId") Long propiedadId,
                        @Param("direccion") String direccion,
                        @Param("ciudad") String ciudad,
                        @Param("estado") EstadoPublicacion estado,
                        @Param("precioMin") BigDecimal precioMin,
                        @Param("precioMax") BigDecimal precioMax
        );

    boolean existsByPropiedadIdAndEstadoAndEliminadaFalse(Long propiedadId, EstadoPublicacion estado);

    boolean existsByPropiedadIdAndEliminadaFalseAndEstadoAndIdNot(Long propiedadId, EstadoPublicacion estado, Long id);
}