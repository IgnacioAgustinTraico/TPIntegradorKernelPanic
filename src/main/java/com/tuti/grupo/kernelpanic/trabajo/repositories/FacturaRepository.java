package com.tuti.grupo.kernelpanic.trabajo.repositories;

import com.tuti.grupo.kernelpanic.trabajo.entities.Factura;
import com.tuti.grupo.kernelpanic.trabajo.entities.EstadoFactura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, Long> {
    
    List<Factura> findByEliminadoFalse();

    @Query("SELECT f FROM Factura f WHERE f.eliminado = false " +
           "AND (:contratoId IS NULL OR f.contrato.id = :contratoId) " +
           "AND (:propiedadId IS NULL OR f.contrato.propiedad.id = :propiedadId) " +
           "AND (:inquilinoId IS NULL OR f.contrato.inquilino.id = :inquilinoId) " +
           "AND (:estado IS NULL OR f.estado = :estado) " +
           "AND (:fechaDesde IS NULL OR f.fechaVencimiento >= :fechaDesde) " +
           "AND (:fechaHasta IS NULL OR f.fechaVencimiento <= :fechaHasta)")
    List<Factura> buscarConFiltros(
            @Param("contratoId") Long contratoId,
            @Param("propiedadId") Long propiedadId,
            @Param("inquilinoId") Long inquilinoId,
            @Param("estado") EstadoFactura estado,
            @Param("fechaDesde") LocalDate fechaDesde,
            @Param("fechaHasta") LocalDate fechaHasta
    );
}