package com.somepharm.hrportal.controller;

import com.somepharm.hrportal.dto.DemandeCongeDTO;
import com.somepharm.hrportal.entity.DemandeConge;
import com.somepharm.hrportal.service.DemandeCongeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/demandes-conge")
public class DemandeCongeController {

    private final DemandeCongeService demandeCongeService;

    public DemandeCongeController(DemandeCongeService demandeCongeService) {
        this.demandeCongeService = demandeCongeService;
    }

    @PostMapping
    public ResponseEntity<DemandeConge> submitDemande(@RequestBody DemandeConge demande) {
        DemandeConge savedDemande = demandeCongeService.createDemande(demande);
        return new ResponseEntity<>(savedDemande, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<DemandeCongeDTO>> getAllDemandes() {
        return ResponseEntity.ok(demandeCongeService.getAllDemandes());
    }
}