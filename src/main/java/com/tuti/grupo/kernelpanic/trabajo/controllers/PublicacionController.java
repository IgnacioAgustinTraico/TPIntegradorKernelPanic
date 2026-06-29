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
    private PropiedadService propiedadService; // Lo necesitamos para listar propiedades en el formulario

    // 1. LISTADO DE PUBLICACIONES (HU 2.4)
    @GetMapping
    public String listarPublicaciones(Model model) {
        model.addAttribute("publicaciones", publicacionService.obtenerTodasActivas());
        return "publicaciones-lista";
    }

    // 2. MOSTRAR FORMULARIO DE ALTA (HU 2.1)
    @GetMapping("/nueva")
    public String mostrarFormularioNueva(Model model) {
        model.addAttribute("publicacion", new Publicacion());
        // Pasamos las propiedades activas para que el usuario elija cuál publicar
        model.addAttribute("propiedades", propiedadService.obtenerTodasActivas());
        model.addAttribute("estados", EstadoPublicacion.values());
        return "publicacion-form";
    }

    // 3. PROCESAR Y GUARDAR EL FORMULARIO (HU 2.1 / HU 2.3)
    @PostMapping("/guardar")
    public String guardarPublicacion(@ModelAttribute("publicacion") Publicacion publicacion, Model model) {
        try {
            publicacionService.guardarPublicacion(publicacion);
            return "redirect:/publicaciones";
        } catch (RuntimeException e) {
            // Si el servicio salta con un error de negocio, volvemos al formulario con el mensaje
            model.addAttribute("error", e.getMessage());
            model.addAttribute("propiedades", propiedadService.obtenerTodasActivas());
            model.addAttribute("estados", EstadoPublicacion.values());
            return "publicacion-form";
        }
    }

    // 4. ELIMINACIÓN LÓGICA (HU 2.2)
    @GetMapping("/eliminar/{id}")
    public String eliminarPublicacion(@PathVariable("id") Long id, Model model) {
        try {
            publicacionService.eliminarPublicacion(id);
            return "redirect:/publicaciones";
        } catch (RuntimeException e) {
            // Si falla la eliminación por regla de negocio, volvemos a la lista mostrando el error
            model.addAttribute("error", e.getMessage());
            model.addAttribute("publicaciones", publicacionService.obtenerTodasActivas());
            return "publicaciones-lista";
        }
    }
}