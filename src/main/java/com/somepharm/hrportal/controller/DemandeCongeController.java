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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/demandes")
public class DemandeCongeController {

    private final DemandeCongeService demandeCongeService;
    private final UtilisateurRepository utilisateurRepository;

    public DemandeCongeController(DemandeCongeService demandeCongeService, UtilisateurRepository utilisateurRepository) {
        this.demandeCongeService = demandeCongeService;
        this.utilisateurRepository = utilisateurRepository;
    }

    @PostMapping("/submit")
    public ResponseEntity<DemandeCongeDTO> submitDemande(@RequestBody DemandeConge demande) {
        // Initialize date immediately to prevent downstream crashes
        demande.setDateSoumission(java.time.LocalDateTime.now());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Utilisateur user = utilisateurRepository.findByMatricule(auth.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        demande.setDemandeur(user);
        demande.setStatutCycleVie("EN_ATTENTE");

        DemandeConge saved = demandeCongeService.createDemande(demande);
        return new ResponseEntity<>(demandeCongeService.convertToDTO(saved), HttpStatus.CREATED);
    }

    @GetMapping("/me")
    public ResponseEntity<List<DemandeCongeDTO>> getMyRequests(java.security.Principal principal) {
        List<DemandeCongeDTO> list = demandeCongeService.getRequestsByMatricule(principal.getName())
                .stream()
                .map(demandeCongeService::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/all")
    public ResponseEntity<List<DemandeCongeDTO>> getAll() {
        return ResponseEntity.ok(demandeCongeService.getAllDemandes());
    }

    @PutMapping("/{id}/statut")
    public ResponseEntity<DemandeCongeDTO> update(@PathVariable Long id, @RequestParam String statut) {
        DemandeConge updated = demandeCongeService.updateStatut(id, statut);
        return ResponseEntity.ok(demandeCongeService.convertToDTO(updated));
    }
}