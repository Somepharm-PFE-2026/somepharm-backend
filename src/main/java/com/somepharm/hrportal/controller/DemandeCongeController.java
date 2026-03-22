package com.somepharm.hrportal.controller;

import com.somepharm.hrportal.dto.DemandeCongeDTO;
import com.somepharm.hrportal.entity.DemandeConge;
import com.somepharm.hrportal.entity.Utilisateur;
import com.somepharm.hrportal.repository.UtilisateurRepository;
import com.somepharm.hrportal.service.DemandeCongeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/demandes") // Changed to match the Next.js frontend!
public class DemandeCongeController {

    private final DemandeCongeService demandeCongeService;
    private final UtilisateurRepository utilisateurRepository; // Added this to find the user

    public DemandeCongeController(DemandeCongeService demandeCongeService, UtilisateurRepository utilisateurRepository) {
        this.demandeCongeService = demandeCongeService;
        this.utilisateurRepository = utilisateurRepository;
    }

    @PostMapping
    public ResponseEntity<?> submitDemande(@RequestBody DemandeConge demande) {

        // 1. Who is asking? Extract the matricule from the JWT Token!
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String matricule = auth.getName();

        // 2. Find that exact user in the database
        Utilisateur user = utilisateurRepository.findByMatricule(matricule)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // 3. Attach the user to the leave request and set the default status
        demande.setDemandeur(user);
        demande.setStatutCycleVie("EN_ATTENTE");

        // 4. Save it!
        DemandeConge savedDemande = demandeCongeService.createDemande(demande);
        return new ResponseEntity<>(savedDemande, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<DemandeCongeDTO>> getAllDemandes() {
        return ResponseEntity.ok(demandeCongeService.getAllDemandes());
    }
}