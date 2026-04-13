package com.somepharm.hrportal.controller;

import com.somepharm.hrportal.entity.Utilisateur;
import com.somepharm.hrportal.repository.UtilisateurRepository;
import com.somepharm.hrportal.service.DocumentService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "http://localhost:3000")
public class DocumentController {

    private final DocumentService documentService;
    private final UtilisateurRepository utilisateurRepository;

    public DocumentController(DocumentService documentService, UtilisateurRepository utilisateurRepository) {
        this.documentService = documentService;
        this.utilisateurRepository = utilisateurRepository;
    }

    @GetMapping("/attestation")
    public ResponseEntity<byte[]> telechargerAttestation() {
        // 1. Identifier l'utilisateur connecté via son Token JWT
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Utilisateur employe = utilisateurRepository.findByMatricule(auth.getName())
                .orElseThrow(() -> new RuntimeException("Employé introuvable"));

        // 2. Générer le PDF
        byte[] pdfBytes = documentService.genererAttestationTravail(employe);

        // 3. Préparer l'en-tête HTTP pour forcer le téléchargement du fichier PDF
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "Attestation_Travail_" + employe.getMatricule() + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
    @GetMapping("/fiche-paie")
    public ResponseEntity<byte[]> telechargerFichePaie() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Utilisateur employe = utilisateurRepository.findByMatricule(auth.getName())
                .orElseThrow(() -> new RuntimeException("Employé introuvable"));

        byte[] pdfBytes = documentService.genererFicheDePaie(employe);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "Fiche_Paie_" + employe.getMatricule() + ".pdf");

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }
    @GetMapping("/salaire")
    public ResponseEntity<byte[]> telechargerAttestationSalaire() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Utilisateur employe = utilisateurRepository.findByMatricule(auth.getName()).orElseThrow();
        byte[] pdfBytes = documentService.genererAttestationSalaire(employe);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "Attestation_Salaire.pdf");
        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    @GetMapping("/titre-conge")
    public ResponseEntity<byte[]> telechargerTitreConge() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Utilisateur employe = utilisateurRepository.findByMatricule(auth.getName()).orElseThrow();
        byte[] pdfBytes = documentService.genererTitreConge(employe);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "Titre_Conge.pdf");
        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }
}