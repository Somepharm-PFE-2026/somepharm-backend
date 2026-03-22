package com.somepharm.hrportal.controller;
import com.somepharm.hrportal.dto.UtilisateurDTO;
import com.somepharm.hrportal.entity.Utilisateur;
import com.somepharm.hrportal.service.UtilisateurService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/utilisateurs")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    public UtilisateurController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    // POST: Create a new User
    @PostMapping
    public ResponseEntity<Utilisateur> createUtilisateur(@RequestBody Utilisateur utilisateur) {
        Utilisateur savedUser = utilisateurService.createUtilisateur(utilisateur);
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }

    // GET: Retrieve all Users
    // GET: Retrieve all Users (NOW SECURED WITH DTO)
    @GetMapping
    public ResponseEntity<List<UtilisateurDTO>> getAllUtilisateurs() {
        return ResponseEntity.ok(utilisateurService.getAllUtilisateurs());
    }
}