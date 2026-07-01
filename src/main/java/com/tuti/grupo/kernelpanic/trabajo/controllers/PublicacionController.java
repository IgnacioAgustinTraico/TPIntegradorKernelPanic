package com.tuti.grupo.kernelpanic.trabajo.controllers;

import com.tuti.grupo.kernelpanic.trabajo.entities.Publicacion;
import com.tuti.grupo.kernelpanic.trabajo.entities.EstadoPublicacion;
import com.tuti.grupo.kernelpanic.trabajo.services.PublicacionService;
import com.tuti.grupo.kernelpanic.trabajo.services.PropiedadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/publicaciones")
public class PublicacionController {

    @Autowired
    private PublicacionService publicacionService;

    @Autowired
    private PropiedadService propiedadService;

    @GetMapping
    public String listarPublicaciones(
            @RequestParam(required = false) Long propiedadId,
            @RequestParam(required = false) String direccion,
            @RequestParam(required = false) String ciudad,
            @RequestParam(required = false) EstadoPublicacion estado,
            @RequestParam(required = false) java.math.BigDecimal precioMin,
            @RequestParam(required = false) java.math.BigDecimal precioMax,
            Model model) {
        model.addAttribute("publicaciones", publicacionService.filtrar(propiedadId, direccion, ciudad, estado, precioMin, precioMax));
        model.addAttribute("propiedadId", propiedadId);
        model.addAttribute("direccion", direccion);
        model.addAttribute("ciudad", ciudad);
        model.addAttribute("estadoSeleccionado", estado);
        model.addAttribute("precioMin", precioMin);
        model.addAttribute("precioMax", precioMax);
        model.addAttribute("estados", EstadoPublicacion.values());
        return "publicaciones-lista";
    }

    @GetMapping("/nueva")
    public String mostrarFormularioNueva(Model model) {
        model.addAttribute("publicacion", new Publicacion());
        model.addAttribute("propiedades", propiedadService.obtenerTodasActivas());
        model.addAttribute("estados", EstadoPublicacion.values());
        return "publicacion-form";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable("id") Long id, Model model) {
        try {
            Publicacion publicacion = publicacionService.buscarPorId(id);
            model.addAttribute("publicacion", publicacion);
            model.addAttribute("propiedades", propiedadService.obtenerTodasActivas());
            model.addAttribute("estados", EstadoPublicacion.values());
            return "publicacion-form";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("publicaciones", publicacionService.obtenerTodasActivas());
            model.addAttribute("estados", EstadoPublicacion.values());
            return "publicaciones-lista";
        }
    }

    @PostMapping("/guardar")
    public String guardarPublicacion(@ModelAttribute("publicacion") Publicacion publicacion, Model model) {
        try {
            publicacionService.guardarPublicacion(publicacion);
            return "redirect:/publicaciones";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("propiedades", propiedadService.obtenerTodasActivas());
            model.addAttribute("estados", EstadoPublicacion.values());
            return "publicacion-form";
        }
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarPublicacion(@PathVariable("id") Long id, Model model) {
        try {
            publicacionService.eliminarPublicacion(id);
            return "redirect:/publicaciones";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("publicaciones", publicacionService.obtenerTodasActivas());
            model.addAttribute("estados", EstadoPublicacion.values());
            return "publicaciones-lista";
        }
    }
}