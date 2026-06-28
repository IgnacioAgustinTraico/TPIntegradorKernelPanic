package com.tuti.grupo.kernelpanic.trabajo.services;

import com.tuti.grupo.kernelpanic.trabajo.entities.Ciudad;
import com.tuti.grupo.kernelpanic.trabajo.entities.EstadoPropiedad;
import com.tuti.grupo.kernelpanic.trabajo.entities.Persona;
import com.tuti.grupo.kernelpanic.trabajo.entities.Propiedad;
import com.tuti.grupo.kernelpanic.trabajo.entities.TipoPropiedad;
import com.tuti.grupo.kernelpanic.trabajo.repositories.CiudadRepository;
import com.tuti.grupo.kernelpanic.trabajo.repositories.PropiedadRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PropiedadServiceTest {

    @Mock
    private PropiedadRepository propiedadRepository;

    @Mock
    private CiudadRepository ciudadRepository;

    @InjectMocks
    private PropiedadService propiedadService;

    @Test
    void guardarPropiedadDebeRechazarCamposObligatoriosVacios() {
        Propiedad propiedad = new Propiedad();
        propiedad.setDireccion("   ");
        propiedad.setTipo(TipoPropiedad.CASA);
        propiedad.setCantidadAmbientes(2);
        propiedad.setMetrosCuadrados(80.0);
        propiedad.setDescripcion("Descripción válida");
        propiedad.setEstadoDisponibilidad(EstadoPropiedad.DISPONIBLE);
        Persona persona = new Persona();
        persona.setNombre("Juan");
        propiedad.setPropietario(persona);

        assertThrows(IllegalArgumentException.class, () -> propiedadService.guardarPropiedad(propiedad, "Córdoba"));
    }
}
