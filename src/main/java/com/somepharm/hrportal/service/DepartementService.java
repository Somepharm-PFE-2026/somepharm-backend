package com.somepharm.hrportal.service;

import com.somepharm.hrportal.entity.Departement;
import com.somepharm.hrportal.repository.DepartementRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartementService {

    private final DepartementRepository departementRepository;

    // Constructor Injection (Best Practice)
    public DepartementService(DepartementRepository departementRepository) {
        this.departementRepository = departementRepository;
    }

    public Departement createDepartement(Departement departement) {
        return departementRepository.save(departement);
    }

    public List<Departement> getAllDepartements() {
        return departementRepository.findAll();
    }
}