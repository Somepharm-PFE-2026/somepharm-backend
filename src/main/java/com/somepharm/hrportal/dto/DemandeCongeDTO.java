package com.somepharm.hrportal.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class DemandeCongeDTO {
    // Parent Data (Requete)
    private Long idRequete;
    private String statutCycleVie;
    private LocalDateTime dateSoumission;
    private Long demandeurId;
    private String demandeurMatricule;

    // Child Data (DemandeConge)
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String motif;
    private String typeConge;
}