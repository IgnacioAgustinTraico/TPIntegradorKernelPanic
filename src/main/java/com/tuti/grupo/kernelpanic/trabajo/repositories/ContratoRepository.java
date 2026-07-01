package com.tuti.grupo.kernelpanic.trabajo.repositories;

import com.tuti.grupo.kernelpanic.trabajo.entities.Contrato;
import com.tuti.grupo.kernelpanic.trabajo.entities.EstadoContrato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDate;

@Repository
public interface ContratoRepository extends JpaRepository<Contrato, Long> {
    
        List<Contrato> findByEliminadoFalse();

    @Query("""
            select c from Contrato c
            where c.eliminado = false
              and (:direccion is null or :direccion = '' or lower(c.propiedad.direccion) like lower(concat('%', :direccion, '%')))
              and (:inquilinoNombre is null or :inquilinoNombre = '' or lower(c.inquilino.nombre) like lower(concat('%', :inquilinoNombre, '%')))
              and (:estado is null or c.estado = :estado)
              and (:fechaInicio is null or c.fechaInicio = :fechaInicio)
            order by c.id desc
            """)
    List<Contrato> buscarConFiltros(
            @Param("direccion") String direccion,
            @Param("inquilinoNombre") String inquilinoNombre,
            @Param("estado") EstadoContrato estado,
            @Param("fechaInicio") LocalDate fechaInicio
    );

        boolean existsByPropiedadIdAndEliminadoFalseAndEstadoAndIdNot(Long propiedadId, EstadoContrato estado, Long id);
}