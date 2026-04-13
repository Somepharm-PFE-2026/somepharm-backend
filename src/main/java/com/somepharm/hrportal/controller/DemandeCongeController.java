package com.somepharm.hrportal.controller;

import com.somepharm.hrportal.dto.DemandeCongeDTO;
import com.somepharm.hrportal.entity.DemandeConge;
import com.somepharm.hrportal.entity.Utilisateur;
import com.somepharm.hrportal.repository.DemandeCongeRepository; // NEW: Needed to fetch raw data
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
@CrossOrigin(origins = "http://localhost:3000")
public class DemandeCongeController {

    private final DemandeCongeService demandeCongeService;
    private final UtilisateurRepository utilisateurRepository;
    private final DemandeCongeRepository demandeCongeRepository; // NEW INJECTION

    public DemandeCongeController(DemandeCongeService demandeCongeService,
                                  UtilisateurRepository utilisateurRepository,
                                  DemandeCongeRepository demandeCongeRepository) {
        this.demandeCongeService = demandeCongeService;
        this.utilisateurRepository = utilisateurRepository;
        this.demandeCongeRepository = demandeCongeRepository;
    }

    @PostMapping("/submit")
    public ResponseEntity<DemandeCongeDTO> submitDemande(@RequestBody DemandeConge demande) {
        demande.setDateSoumission(java.time.LocalDateTime.now());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Utilisateur user = utilisateurRepository.findByMatricule(auth.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        demande.setDemandeur(user);
        demande.setStatutCycleVie("EN_ATTENTE_MANAGER");

        DemandeConge saved = demandeCongeService.createDemande(demande);
        return new ResponseEntity<>(demandeCongeService.convertToDTO(saved), HttpStatus.CREATED);
    }

    @GetMapping("/me")
    public ResponseEntity<List<DemandeCongeDTO>> getMyRequests(java.security.Principal principal) {
        List<DemandeCongeDTO> list = demandeCongeService.getRequestsByMatricule(principal.getName())
                .stream()
                // 🚀 UPGRADE: Sort the employee's personal requests so the newest is at the top
                .sorted((d1, d2) -> {
                    if (d1.getDateSoumission() == null) return 1;
                    if (d2.getDateSoumission() == null) return -1;
                    return d2.getDateSoumission().compareTo(d1.getDateSoumission());
                })
                .map(demandeCongeService::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/all")
    public ResponseEntity<List<DemandeCongeDTO>> getAll(Authentication auth) {
        // 1. Who is asking to see the data?
        Utilisateur currentUser = utilisateurRepository.findByMatricule(auth.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // 2. Fetch ALL raw entities directly from the database
        List<DemandeConge> allDemandesRaw = demandeCongeRepository.findAll();

        // 3. Filter by Department and Sort by Date
        List<DemandeCongeDTO> filteredList = allDemandesRaw.stream()
                .filter(demande -> {
                    // HR_ADMIN (Role 3) has ultimate power: Sees every department
                    if (currentUser.getRole().getIdRole() == 3) {
                        return true;
                    }

                    // MANAGER (Role 2) is restricted: Only sees their exact department
                    if (currentUser.getRole().getIdRole() == 2) {
                        String managerDept = currentUser.getDepartement();
                        String employeeDept = (demande.getDemandeur() != null) ? demande.getDemandeur().getDepartement() : null;

                        // Only return true if they belong to the same team
                        return managerDept != null && managerDept.equals(employeeDept);
                    }

                    // NORMAL EMPLOYEES shouldn't be calling this, but if they do, block it.
                    return false;
                })
                // 🚀 UPGRADE: Sort the manager's view so newest requests are at the top
                .sorted((d1, d2) -> {
                    if (d1.getDateSoumission() == null) return 1;
                    if (d2.getDateSoumission() == null) return -1;
                    return d2.getDateSoumission().compareTo(d1.getDateSoumission());
                })
                // 4. Convert the secure, sorted list back to DTOs for the Frontend
                .map(demandeCongeService::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(filteredList);
    }

    @PutMapping("/{id}/statut")
    public ResponseEntity<DemandeCongeDTO> update(
            @PathVariable Long id,
            @RequestParam String statut,
            @RequestParam(required = false) String commentaire) {

        DemandeConge updated = demandeCongeService.updateStatut(id, statut, commentaire);
        return ResponseEntity.ok(demandeCongeService.convertToDTO(updated));
    }
}