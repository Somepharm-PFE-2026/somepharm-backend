package com.somepharm.hrportal.repository;

import com.somepharm.hrportal.entity.DemandeConge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DemandeCongeRepository extends JpaRepository<DemandeConge, Long> {
    // This finds requests where the Demandeur's matricule matches the input
    List<DemandeConge> findByDemandeur_Matricule(String matricule);
}