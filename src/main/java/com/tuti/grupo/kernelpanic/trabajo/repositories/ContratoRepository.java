package com.tuti.grupo.kernelpanic.trabajo.repositories;

import com.tuti.grupo.kernelpanic.trabajo.entities.Contrato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ContratoRepository extends JpaRepository<Contrato, Long> {
    
    List<Contrato> findByEliminadoFalse();
}