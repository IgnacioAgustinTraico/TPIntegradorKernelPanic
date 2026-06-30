package com.tuti.grupo.kernelpanic.trabajo.services;

import com.tuti.grupo.kernelpanic.trabajo.entities.Factura;
import java.util.List;

public interface FacturaService {
    List<Factura> listarTodas();
    Factura obtenerPorId(Long id);
    Factura guardarManual(Factura factura);
    Factura modificar(Long id, Factura facturaActualizada);
    void eliminarLogico(Long id);
}