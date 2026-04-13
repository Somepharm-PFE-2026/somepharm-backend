package com.somepharm.hrportal.service;

import com.somepharm.hrportal.entity.BonDeSortie;
import com.somepharm.hrportal.entity.Utilisateur;
import com.somepharm.hrportal.repository.BonDeSortieRepository;
import com.somepharm.hrportal.repository.UtilisateurRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class BonDeSortieService {

    private final BonDeSortieRepository bonDeSortieRepository;
    private final UtilisateurRepository utilisateurRepository;

    public BonDeSortieService(BonDeSortieRepository bonDeSortieRepository, UtilisateurRepository utilisateurRepository) {
        this.bonDeSortieRepository = bonDeSortieRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    @Transactional
    public String scannerQrCode(String tokenQr) {
        BonDeSortie bds = bonDeSortieRepository.findByTokenQr(tokenQr)
                .orElseThrow(() -> new RuntimeException("QR Code Invalide ou introuvable."));

        if ("EN_ATTENTE".equals(bds.getStatut())) {
            // SCÉNARIO 1 : L'employé sort
            bds.setHeureSortieReelle(LocalDateTime.now());
            bds.setStatut("EN_COURS");
            bonDeSortieRepository.save(bds);
            return "Sortie autorisée et enregistrée à " + bds.getHeureSortieReelle().toLocalTime();

        } else if ("EN_COURS".equals(bds.getStatut())) {
            // SCÉNARIO 2 : L'employé revient
            bds.setHeureRetourReelle(LocalDateTime.now());
            bds.setStatut("CLOTURE");
            calculerEtDeduireHeures(bds);
            bonDeSortieRepository.save(bds);
            return "Retour enregistré. Solde de congés mis à jour.";
        }

        return "Ce bon de sortie est déjà clôturé.";
    }

    private void calculerEtDeduireHeures(BonDeSortie bds) {
        LocalDateTime sortie = bds.getHeureSortieReelle();
        LocalDateTime retour = bds.getHeureRetourReelle();

        // THE SHIFT-CAP LOGIC: If they never returned, cap the return time at 16:00 (4:00 PM) today
        if (retour == null) {
            retour = sortie.withHour(16).withMinute(0).withSecond(0);
            bds.setHeureRetourReelle(retour);
        }

        // Calculate total minutes absent
        long minutesAbsence = ChronoUnit.MINUTES.between(sortie, retour);
        if (minutesAbsence < 0) minutesAbsence = 0; // Security check

        // Convert minutes to fractions of a working day (Assuming 8 hours = 1 day)
        double heuresAbsence = minutesAbsence / 60.0;
        double joursADeduire = heuresAbsence / 8.0;

        Utilisateur demandeur = bds.getDemandeur();

        // Ensure solde doesn't go below 0 unexpectedly, though negative balance is sometimes allowed in HR
        demandeur.setSoldeConges(demandeur.getSoldeConges() - joursADeduire);
        utilisateurRepository.save(demandeur);
    }

    /**
     * CRON JOB: Runs automatically every day at 16:30 to catch anyone who didn't return.
     * "0 30 16 * * *" = 16:30:00 every day.
     */
    @Scheduled(cron = "0 30 16 * * *")
    @Transactional
    public void cloturerSortiesNonRetournees() {
        List<BonDeSortie> sortiesEnCours = bonDeSortieRepository.findByStatut("EN_COURS");

        for (BonDeSortie bds : sortiesEnCours) {
            bds.setStatut("CLOTURE");
            // This will trigger the Shift-Cap logic because heureRetourReelle is null
            calculerEtDeduireHeures(bds);
            bonDeSortieRepository.save(bds);
            System.out.println("Auto-clôture du bon " + bds.getTokenQr() + " effectuée à 16:00.");
        }
    }
}