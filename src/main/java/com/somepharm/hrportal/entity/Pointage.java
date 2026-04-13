package com.somepharm.hrportal.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Data
@Table(name = "pointage")
public class Pointage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_utilisateur", nullable = false)
    private Utilisateur employe;

    private LocalDate dateJour;
    private LocalTime heureEntree;
    private LocalTime heureSortie;

    // Statuts possibles : "A_L_HEURE", "EN_RETARD", "ABSENT"
    private String statut;
}