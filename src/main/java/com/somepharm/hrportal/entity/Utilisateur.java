package com.somepharm.hrportal.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "UTILISATEUR")
@Data
@NoArgsConstructor
public class Utilisateur implements UserDetails {

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
    @Column(name = "departement")
    private String departement = "Général";

    // --- SPRING SECURITY METHODS ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Assuming your Utilisateur entity has a relationship to the Role entity named 'role'
        if (this.role != null && this.role.getNomRole() != null) {
            return List.of(new SimpleGrantedAuthority(this.role.getNomRole()));
        }
        // Fallback default if no role is found
        return List.of(new SimpleGrantedAuthority("EMPLOYEE"));
    }
    @Column(name = "solde_conges", nullable = false, columnDefinition = "integer default 30")
    private Integer soldeConges = 30;
    @Override
    public String getPassword() {
        return this.motDePasse;
    }

    @Override
    public String getUsername() {
        return this.matricule; // Using Matricule as the login ID!
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return "ACTIF".equals(this.statutCompte);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return "ACTIF".equals(this.statutCompte);
    }

    public String getDepartement() {
        return departement;
    }

    public void setDepartement(String departement) {
        this.departement = departement;
    }
}