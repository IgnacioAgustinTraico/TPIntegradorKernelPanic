package com.tuti.grupo.kernelpanic.trabajo.controllers;

import com.tuti.grupo.kernelpanic.trabajo.entities.EstadoPropiedad;
import com.tuti.grupo.kernelpanic.trabajo.entities.Persona;
import com.tuti.grupo.kernelpanic.trabajo.entities.Propiedad;
import com.tuti.grupo.kernelpanic.trabajo.entities.TipoPropiedad;
import com.tuti.grupo.kernelpanic.trabajo.repositories.CiudadRepository;
import com.tuti.grupo.kernelpanic.trabajo.repositories.PersonaRepository;
import com.tuti.grupo.kernelpanic.trabajo.services.PropiedadService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/propiedades")
public class PropiedadController {

    private final PropiedadService propiedadService;
    private final PersonaRepository personaRepository;
    private final CiudadRepository ciudadRepository;

    public PropiedadController(PropiedadService propiedadService,
                               PersonaRepository personaRepository,
                               CiudadRepository ciudadRepository) {
        this.propiedadService = propiedadService;
        this.personaRepository = personaRepository;
        this.ciudadRepository = ciudadRepository;
    }

    @GetMapping
    public String listarPropiedades(
            @RequestParam(required = false) String direccion,
            @RequestParam(required = false) String ciudad,
            @RequestParam(required = false) TipoPropiedad tipo,
            @RequestParam(required = false) EstadoPropiedad estado,
            Model model) {

        model.addAttribute("propiedades", propiedadService.filtrar(direccion, ciudad, tipo, estado));
        model.addAttribute("tipos", TipoPropiedad.values());
        model.addAttribute("estados", EstadoPropiedad.values());
        model.addAttribute("direccion", direccion);
        model.addAttribute("ciudad", ciudad);
        model.addAttribute("tipoSeleccionado", tipo);
        model.addAttribute("estadoSeleccionado", estado);
        return "propiedades-lista";
    }

    @GetMapping("/nueva")
    public String mostrarFormularioNueva(Model model) {
        Propiedad propiedad = new Propiedad();
        propiedad.setEstadoDisponibilidad(EstadoPropiedad.DISPONIBLE);
        model.addAttribute("propiedad", propiedad);
        agregarModelosBase(model);
        return "propiedad-form";
    }

    @PostMapping("/guardar")
    public String guardarPropiedad(@ModelAttribute Propiedad propiedad,
                                   @RequestParam(required = false) Long propietarioId,
                                   @RequestParam(name = "ciudadNombre", required = false) String ciudadNombre,
                                   Model model) {
        try {
            if (propietarioId != null) {
                Persona propietario = personaRepository.findById(propietarioId)
                        .orElseThrow(() -> new IllegalArgumentException("El propietario seleccionado no existe."));
                propiedad.setPropietario(propietario);
            }

            propiedadService.guardarPropiedad(propiedad, ciudadNombre);
            return "redirect:/propiedades";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("propiedad", propiedad);
            agregarModelosBase(model);
            return "propiedad-form";
        } catch (DataIntegrityViolationException e) {
            model.addAttribute("error", "No se pudo guardar la propiedad debido a un problema de datos");
            model.addAttribute("propiedad", propiedad);
            agregarModelosBase(model);
            return "propiedad-form";
        }
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        Propiedad propiedad = propiedadService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Propiedad no encontrada"));
        model.addAttribute("propiedad", propiedad);
        agregarModelosBase(model);
        return "propiedad-form";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarPropiedad(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            propiedadService.eliminarPropiedad(id);
            redirectAttributes.addFlashAttribute("success", "La propiedad fue eliminada correctamente.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("error", "No se pudo eliminar la propiedad debido a un problema de integridad en la base de datos.");
        }
        return "redirect:/propiedades";
    }

    private void agregarModelosBase(Model model) {
        model.addAttribute("propietarios", personaRepository.findAllByEliminadaFalseOrderByNombreAsc());
        model.addAttribute("ciudades", ciudadRepository.findAll());
        model.addAttribute("tipos", TipoPropiedad.values());
        model.addAttribute("estados", EstadoPropiedad.values());
    }
}
