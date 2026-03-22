package com.somepharm.hrportal.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "REQUETE")
@Inheritance(strategy = InheritanceType.JOINED) // This is the magic annotation!
@Data
@NoArgsConstructor
public class Requete {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_requete")
    private Long idRequete;

    @Column(name = "statut_cycle_vie", nullable = false, length = 50)
    private String statutCycleVie = "EN_ATTENTE";

    @Column(name = "date_soumission", updatable = false)
    private LocalDateTime dateSoumission = LocalDateTime.now();

    // Linking the request to the User who created it
    @ManyToOne
    @JoinColumn(name = "id_user", referencedColumnName = "id_user")
    private Utilisateur demandeur;
}