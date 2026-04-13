package com.somepharm.hrportal.controller;

import com.somepharm.hrportal.entity.Utilisateur;
import com.somepharm.hrportal.repository.UtilisateurRepository;
import org.springframework.http.HttpStatus;
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

    // --- NEW: THE SECURE DIRECTORY ENDPOINT (Role-Based Visibility) ---
    @GetMapping("/directory")
    public ResponseEntity<?> getEmployeeDirectory() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Utilisateur currentUser = utilisateurRepository.findByMatricule(auth.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        String role = currentUser.getRole() != null ? currentUser.getRole().getNomRole() : "EMPLOYEE";

        if (role.equals("HR_ADMIN") || role.equals("ROLE_HR_ADMIN")) {
            // HR sees EVERYONE
            return ResponseEntity.ok(utilisateurRepository.findAll());
        } else if (role.equals("MANAGER") || role.equals("ROLE_MANAGER")) {
            // Manager sees ONLY their department
            return ResponseEntity.ok(utilisateurRepository.findByDepartement(currentUser.getDepartement()));
        } else {
            // Employee sees NOTHING (Returns a 403 Forbidden Error to trigger the React block screen)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Accès refusé. Réservé aux Managers et RH.");
        }
    }

    @PostMapping("/create")
    public ResponseEntity<Utilisateur> createUser(@RequestBody Utilisateur newUser) {
        // 🚀 FIXED: Changed 30 to 30.0 because the field is now a Double!
        newUser.setSoldeConges(30.0);
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
        // Add these lines inside updateEmployee to save the new data
        existing.setNom(updatedData.getNom());
        existing.setPrenom(updatedData.getPrenom());
        existing.setTelephone(updatedData.getTelephone());
        existing.setEmail(updatedData.getEmail());
        existing.setRole(updatedData.getRole());

        // Save the new department
        if (updatedData.getDepartement() != null) {
            existing.setDepartement(updatedData.getDepartement());
        }

        // 🚀 FIXED: Added null check and changed 0 to 0.0 for the Double comparison
        if (updatedData.getSoldeConges() != null && updatedData.getSoldeConges() >= 0.0) {
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
    @GetMapping("/{id}")
    public ResponseEntity<Utilisateur> getUserById(@PathVariable Long id) {
        return utilisateurRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new RuntimeException("Employé introuvable"));
    }
}