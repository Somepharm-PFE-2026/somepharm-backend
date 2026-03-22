package com.somepharm.hrportal.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "UTILISATEUR")
@Data
@NoArgsConstructor
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_user")
    private Long idUser;

    @Column(nullable = false, unique = true, length = 20)
    private String matricule;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "mot_de_passe", nullable = false)
    private String motDePasse;

    @Column(name = "statut_compte", length = 20)
    private String statutCompte = "ACTIF";

    @ManyToOne
    @JoinColumn(name = "id_role", referencedColumnName = "id_role")
    private Role role;
}