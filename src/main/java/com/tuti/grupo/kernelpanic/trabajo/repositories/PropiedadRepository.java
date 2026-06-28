package com.tuti.grupo.kernelpanic.trabajo.repositories;

import com.tuti.grupo.kernelpanic.trabajo.entities.Ciudad;
import com.tuti.grupo.kernelpanic.trabajo.entities.EstadoPropiedad;
import com.tuti.grupo.kernelpanic.trabajo.entities.Propiedad;
import com.tuti.grupo.kernelpanic.trabajo.entities.TipoPropiedad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropiedadRepository extends JpaRepository<Propiedad, Long> {

    @Query("""
            select p from Propiedad p
            where p.eliminada = false
              and (:direccion is null or :direccion = '' or lower(p.direccion) like lower(concat('%', :direccion, '%')))
              and (:ciudad is null or :ciudad = '' or lower(p.ciudad.nombre) like lower(concat('%', :ciudad, '%')))
              and (:tipo is null or p.tipo = :tipo)
              and (:estado is null or p.estadoDisponibilidad = :estado)
            order by p.id desc
            """)
    List<Propiedad> filtrarPropiedadesActivas(
            @Param("direccion") String direccion,
            @Param("ciudad") String ciudad,
            @Param("tipo") TipoPropiedad tipo,
            @Param("estado") EstadoPropiedad estado
    );

    boolean existsByDireccionAndCiudadAndEliminadaFalseAndIdNot(String direccion, Ciudad ciudad, Long id);
}
