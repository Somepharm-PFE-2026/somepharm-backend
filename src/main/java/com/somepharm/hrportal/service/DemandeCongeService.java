package com.somepharm.hrportal.service;

import com.somepharm.hrportal.dto.DemandeCongeDTO;
import com.somepharm.hrportal.entity.DemandeConge;
import com.somepharm.hrportal.repository.DemandeCongeRepository;
import org.springframework.stereotype.Service;

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

    public DemandeCongeDTO convertToDTO(DemandeConge demande) {
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