package com.somepharm.hrportal.controller;

import com.somepharm.hrportal.entity.Pointage;
import com.somepharm.hrportal.entity.Utilisateur;
import com.somepharm.hrportal.repository.PointageRepository;
import com.somepharm.hrportal.repository.UtilisateurRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/pointage")
@CrossOrigin(origins = "http://localhost:3000")
public class PointageController {

    private final PointageRepository pointageRepository;
    private final UtilisateurRepository utilisateurRepository;

    // ⏰ Configuration : Heure limite d'arrivée fixée à 08h30
    private final LocalTime HEURE_LIMITE_MATIN = LocalTime.of(8, 30);

    public PointageController(PointageRepository pointageRepository, UtilisateurRepository utilisateurRepository) {
        this.pointageRepository = pointageRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    // 1. Endpoint pour pointer (Entrée ou Sortie)
    @PostMapping("/action")
    public ResponseEntity<?> pointer() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Utilisateur employe = utilisateurRepository.findByMatricule(auth.getName())
                .orElseThrow(() -> new RuntimeException("Employé introuvable"));

        LocalDate aujourdhui = LocalDate.now();
        LocalTime maintenant = LocalTime.now();

        Optional<Pointage> pointageOpt = pointageRepository.findByEmployeMatriculeAndDateJour(employe.getMatricule(), aujourdhui);

        if (pointageOpt.isEmpty()) {
            // ---> SCÉNARIO 1 : L'employé arrive au travail (Pointage d'entrée)
            Pointage nouveau = new Pointage();
            nouveau.setEmploye(employe);
            nouveau.setDateJour(aujourdhui);
            nouveau.setHeureEntree(maintenant);

            if (maintenant.isAfter(HEURE_LIMITE_MATIN)) {
                nouveau.setStatut("EN_RETARD");
            } else {
                nouveau.setStatut("A_L_HEURE");
            }
            return ResponseEntity.ok(pointageRepository.save(nouveau));

        } else {
            // ---> SCÉNARIO 2 : L'employé quitte le travail (Pointage de sortie)
            Pointage existant = pointageOpt.get();
            if (existant.getHeureSortie() != null) {
                return ResponseEntity.badRequest().body("Vous avez déjà validé votre sortie pour aujourd'hui.");
            }
            existant.setHeureSortie(maintenant);
            return ResponseEntity.ok(pointageRepository.save(existant));
        }
    }

    // 2. Endpoint pour que React sache quoi afficher sur le bouton (Entrée ou Sortie)
    @GetMapping("/statut-jour")
    public ResponseEntity<Pointage> getStatutAujourdhui() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Optional<Pointage> pointageOpt = pointageRepository.findByEmployeMatriculeAndDateJour(auth.getName(), LocalDate.now());

        return pointageOpt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.ok().build());
    }
}