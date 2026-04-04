package com.somepharm.hrportal.controller;

import com.somepharm.hrportal.entity.Utilisateur;
import com.somepharm.hrportal.repository.UtilisateurRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/utilisateurs")
@CrossOrigin(origins = "http://localhost:3000")
public class UtilisateurController {

    private final UtilisateurRepository utilisateurRepository;

    public UtilisateurController(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<Utilisateur> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return utilisateurRepository.findByMatricule(auth.getName())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/all")
    public ResponseEntity<List<Utilisateur>> getAllUsers() {
        return ResponseEntity.ok(utilisateurRepository.findAll());
    }

    @PostMapping("/create")
    public ResponseEntity<Utilisateur> createUser(@RequestBody Utilisateur newUser) {
        newUser.setSoldeConges(30);
        newUser.setStatutCompte("ACTIF");

        // Default to Général if none provided
        if (newUser.getDepartement() == null || newUser.getDepartement().isEmpty()) {
            newUser.setDepartement("Général");
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (newUser.getMotDePasse() != null && !newUser.getMotDePasse().isEmpty()) {
            newUser.setMotDePasse(encoder.encode(newUser.getMotDePasse()));
        }

        Utilisateur savedUser = utilisateurRepository.save(newUser);
        return ResponseEntity.ok(savedUser);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Utilisateur> updateEmployee(@PathVariable Long id, @RequestBody Utilisateur updatedData) {
        Utilisateur existing = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employé introuvable"));

        existing.setEmail(updatedData.getEmail());
        existing.setRole(updatedData.getRole());

        // Save the new department
        if (updatedData.getDepartement() != null) {
            existing.setDepartement(updatedData.getDepartement());
        }

        if (updatedData.getSoldeConges() >= 0) {
            existing.setSoldeConges(updatedData.getSoldeConges());
        }

        return ResponseEntity.ok(utilisateurRepository.save(existing));
    }

    @PutMapping("/{id}/statut")
    public ResponseEntity<Utilisateur> toggleStatus(@PathVariable Long id) {
        Utilisateur existing = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employé introuvable"));

        if ("ACTIF".equals(existing.getStatutCompte())) {
            existing.setStatutCompte("INACTIF");
        } else {
            existing.setStatutCompte("ACTIF");
        }
        return ResponseEntity.ok(utilisateurRepository.save(existing));
    }

    @PutMapping("/{id}/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@PathVariable Long id) {
        Utilisateur existing = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employé introuvable"));

        String tempPassword = "Somepharm" + (int)(Math.random() * 9000 + 1000);
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        existing.setMotDePasse(encoder.encode(tempPassword));
        utilisateurRepository.save(existing);

        return ResponseEntity.ok(Collections.singletonMap("tempPassword", tempPassword));
    }
}