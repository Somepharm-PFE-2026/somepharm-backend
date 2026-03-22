package com.somepharm.hrportal.repository;

import com.somepharm.hrportal.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    // Spring Data JPA magic: It will automatically write the SQL query for this!
    Optional<Utilisateur> findByMatricule(String matricule);

}