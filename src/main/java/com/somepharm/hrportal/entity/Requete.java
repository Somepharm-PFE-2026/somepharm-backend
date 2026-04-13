package com.somepharm.hrportal.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "requete")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
public abstract class Requete {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idRequete;

    @Column(nullable = false)
    private LocalDateTime dateSoumission;

    private String description;

    /**
     * Possible values:
     * EN_ATTENTE_MANAGER (Initial state)
     * VALIDE_MANAGER (Approved by Dept Boss)
     * APPROUVE (Final approval by HR - Deducts balance)
     * REFUSE (Rejected at any step)
     */
    @Column(nullable = false)
    private String statutCycleVie;

    /**
     * Stores the reason for refusal or a general comment
     * from the validator (Manager or HR).
     */
    @Column(name = "commentaire_action", length = 500)
    private String commentaireAction;

    @ManyToOne
    @JoinColumn(name = "id_user", nullable = false)
    private Utilisateur demandeur;
}