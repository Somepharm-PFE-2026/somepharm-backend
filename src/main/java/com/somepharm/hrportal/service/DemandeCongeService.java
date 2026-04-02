package com.somepharm.hrportal.service;
import java.time.temporal.ChronoUnit;
import com.somepharm.hrportal.dto.DemandeCongeDTO;
import com.somepharm.hrportal.entity.DemandeConge;
import com.somepharm.hrportal.repository.DemandeCongeRepository;
import org.springframework.stereotype.Service;
import com.somepharm.hrportal.entity.Utilisateur;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DemandeCongeService {

    private final DemandeCongeRepository demandeCongeRepository;

    public DemandeCongeService(DemandeCongeRepository demandeCongeRepository) {
        this.demandeCongeRepository = demandeCongeRepository;
    }
    public DemandeConge updateStatut(Long id, String nouveauStatut) {
        // 1. Find the request
        DemandeConge demande = demandeCongeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Demande non trouvée avec l'ID : " + id));

        // 2. If the request is being APPROVED, we do the math
        if ("APPROUVE".equals(nouveauStatut) && "EN_ATTENTE".equals(demande.getStatutCycleVie())) {
            Utilisateur demandeur = demande.getDemandeur();

            // Calculate total days (Adding 1 because if you take Monday to Monday, that's 1 day off)
            long joursDemandes = ChronoUnit.DAYS.between(demande.getDateDebut(), demande.getDateFin()) + 1;

            // Check if they have enough balance!
            if (demandeur.getSoldeConges() < joursDemandes) {
                throw new RuntimeException("Solde de congés insuffisant pour cette demande.");
            }

            // Deduct the days from the balance
            demandeur.setSoldeConges((int) (demandeur.getSoldeConges() - joursDemandes));

            // Note: Because 'demandeur' is linked to 'demande', Hibernate will automatically
            // update the Utilisateur table in PostgreSQL when we save the demande below!
        }

        // 3. Update the status and save
        demande.setStatutCycleVie(nouveauStatut);
        return demandeCongeRepository.save(demande);
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
        // Corrected variable name: demandeCongeRepository
        return demandeCongeRepository.findByDemandeur_Matricule(matricule);
    }
    public DemandeCongeDTO convertToDTO(DemandeConge demande) {
        System.out.println("DEBUG: Running the NEW version of convertToDTO!");
        DemandeCongeDTO dto = new DemandeCongeDTO();

        // From Parent (Requete)
        dto.setIdRequete(demande.getIdRequete());
        dto.setStatutCycleVie(demande.getStatutCycleVie());
        dto.setDateSoumission(demande.getDateSoumission());

        // Safe Requester Details
        if (demande.getDemandeur() != null) {
            dto.setDemandeurId(demande.getDemandeur().getIdUser());
            dto.setDemandeurMatricule(demande.getDemandeur().getMatricule());
        }

        // From Child (DemandeConge)
        dto.setDateDebut(demande.getDateDebut());
        dto.setDateFin(demande.getDateFin());
        dto.setMotif(demande.getMotif());
        dto.setTypeConge(demande.getTypeConge());

        return dto;
    }
}