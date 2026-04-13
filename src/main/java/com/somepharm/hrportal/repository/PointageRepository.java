package com.somepharm.hrportal.repository;

import com.somepharm.hrportal.entity.Pointage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.Optional;

public interface PointageRepository extends JpaRepository<Pointage, Long> {
    // Permet de vérifier si l'employé a déjà pointé aujourd'hui
    Optional<Pointage> findByEmployeMatriculeAndDateJour(String matricule, LocalDate dateJour);
}