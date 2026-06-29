package com.tuti.grupo.kernelpanic.trabajo.controllers;

import com.tuti.grupo.kernelpanic.trabajo.entities.Contrato;
import com.tuti.grupo.kernelpanic.trabajo.entities.EstadoContrato;
import com.tuti.grupo.kernelpanic.trabajo.services.ContratoService;
import com.tuti.grupo.kernelpanic.trabajo.services.PropiedadService;
import com.tuti.grupo.kernelpanic.trabajo.repositories.PersonaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/contratos")
public class ContratoController {

    @Autowired
    private ContratoService contratoService;

    @Autowired
    private PropiedadService propiedadService;

    @Autowired
    private PersonaRepository personaRepository;


    @GetMapping
    public String listarContratos(Model model) {
        model.addAttribute("contratos", contratoService.obtenerTodosActivos());
        return "contratos-lista";
    }


    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("contrato", new Contrato());
        model.addAttribute("propiedades", propiedadService.obtenerTodasActivas()); 
        model.addAttribute("inquilinos", personaRepository.findAllByEliminadaFalseOrderByNombreAsc()); 
        model.addAttribute("estados", EstadoContrato.values());
        return "contrato-form";
    }


    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable("id") Long id, Model model) {
        try {
            Contrato contrato = contratoService.buscarPorId(id);
            model.addAttribute("contrato", contrato);
            model.addAttribute("propiedades", propiedadService.obtenerTodasActivas());
            model.addAttribute("inquilinos", personaRepository.findAllByEliminadaFalseOrderByNombreAsc());
            model.addAttribute("estados", EstadoContrato.values());
            return "contrato-form";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("contratos", contratoService.obtenerTodosActivos());
            return "contratos-lista";
        }
    }


    @PostMapping("/guardar")
    public String guardarContrato(@ModelAttribute("contrato") Contrato contrato, Model model) {
        try {
            contratoService.guardarContrato(contrato);
            return "redirect:/contratos";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("propiedades", propiedadService.obtenerTodasActivas());
            model.addAttribute("inquilinos", personaRepository.findAllByEliminadaFalseOrderByNombreAsc());
            model.addAttribute("estados", EstadoContrato.values());
            return "contrato-form";
        }
    }


    @GetMapping("/eliminar/{id}")
    public String eliminarContrato(@PathVariable("id") Long id, Model model) {
        try {
            contratoService.eliminarContrato(id);
            return "redirect:/contratos";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("contratos", contratoService.obtenerTodosActivos());
            return "contratos-lista";
        }
    }
}