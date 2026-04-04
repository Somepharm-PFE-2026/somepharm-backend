package com.somepharm.hrportal.service;

import com.somepharm.hrportal.dto.DemandeCongeDTO;
import com.somepharm.hrportal.entity.DemandeConge;
import com.somepharm.hrportal.entity.Utilisateur;
import com.somepharm.hrportal.repository.DemandeCongeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DemandeCongeService {

    private final DemandeCongeRepository demandeCongeRepository;

    public DemandeCongeService(DemandeCongeRepository demandeCongeRepository) {
        this.demandeCongeRepository = demandeCongeRepository;
    }

    public DemandeConge createDemande(DemandeConge demande) {
        return demandeCongeRepository.save(demande);
    }

    public List<DemandeCongeDTO> getAllDemandes() {
        return demandeCongeRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<DemandeConge> getRequestsByMatricule(String matricule) {
        return demandeCongeRepository.findByDemandeur_Matricule(matricule);
    }

    @Transactional
    public DemandeConge updateStatut(Long id, String nouveauStatut, String commentaire) {
        DemandeConge demande = demandeCongeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Demande non trouvée"));

        // Only deduct balance if final status is APPROUVE (HR Validation)
        if ("APPROUVE".equals(nouveauStatut) && !"APPROUVE".equals(demande.getStatutCycleVie())) {
            Utilisateur demandeur = demande.getDemandeur();
            long jours = ChronoUnit.DAYS.between(demande.getDateDebut(), demande.getDateFin()) + 1;

            if (demandeur.getSoldeConges() < (int) jours) {
                throw new RuntimeException("Solde insuffisant (" + demandeur.getSoldeConges() + " jours restants).");
            }
            demandeur.setSoldeConges(demandeur.getSoldeConges() - (int) jours);
        }

        demande.setStatutCycleVie(nouveauStatut);
        demande.setCommentaireAction(commentaire); // Store the reason for decision

        return demandeCongeRepository.save(demande);
    }

    public DemandeCongeDTO convertToDTO(DemandeConge demande) {
        DemandeCongeDTO dto = new DemandeCongeDTO();
        dto.setIdRequete(demande.getIdRequete());
        dto.setDateSoumission(demande.getDateSoumission());
        dto.setDescription(demande.getDescription());
        dto.setStatutCycleVie(demande.getStatutCycleVie());
        dto.setCommentaireAction(demande.getCommentaireAction());

        if (demande.getDemandeur() != null) {
            dto.setDemandeurId(demande.getDemandeur().getIdUser());
            dto.setDemandeurMatricule(demande.getDemandeur().getMatricule());
        }

        dto.setDateDebut(demande.getDateDebut());
        dto.setDateFin(demande.getDateFin());
        dto.setMotif(demande.getMotif());
        dto.setTypeConge(demande.getTypeConge());

        return dto;
    }
}