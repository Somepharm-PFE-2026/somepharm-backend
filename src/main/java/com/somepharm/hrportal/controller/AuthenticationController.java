package com.somepharm.hrportal.controller;

import com.somepharm.hrportal.config.JwtService;
import com.somepharm.hrportal.dto.AuthenticationRequest;
import com.somepharm.hrportal.dto.AuthenticationResponse;
import com.somepharm.hrportal.dto.RegisterRequest;
import com.somepharm.hrportal.entity.Utilisateur;
import com.somepharm.hrportal.repository.UtilisateurRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final UtilisateurRepository repository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationController(
            AuthenticationManager authenticationManager,
            UtilisateurRepository repository,
            JwtService jwtService,
            PasswordEncoder passwordEncoder
    ) {
        this.authenticationManager = authenticationManager;
        this.repository = repository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    // --- 1. THE REGISTER ENDPOINT ---
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest request) {
        Utilisateur user = new Utilisateur();
        user.setMatricule(request.getMatricule());
        user.setEmail(request.getEmail());

        // Encrypting the password before it hits the database!
        user.setMotDePasse(passwordEncoder.encode(request.getPassword()));
        user.setStatutCompte("ACTIF");

        repository.save(user);
        var jwtToken = jwtService.generateToken(user);
        return ResponseEntity.ok(AuthenticationResponse.builder().token(jwtToken).build());
    }

    // --- 2. THE LOGIN ENDPOINT ---
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getMatricule(), request.getPassword())
        );

        var user = repository.findByMatricule(request.getMatricule()).orElseThrow();
        var jwtToken = jwtService.generateToken(user);
        return ResponseEntity.ok(AuthenticationResponse.builder().token(jwtToken).build());
    }
}