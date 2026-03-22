package com.somepharm.hrportal.service;

import com.somepharm.hrportal.dto.DepartementDTO;
import com.somepharm.hrportal.entity.Departement;
import com.somepharm.hrportal.repository.DepartementRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepartementService {

    private final DepartementRepository departementRepository;

    public DepartementService(DepartementRepository departementRepository) {
        this.departementRepository = departementRepository;
    }

    public Departement createDepartement(Departement departement) {
        return departementRepository.save(departement);
    }

    // 1. Change this to return a List of DTOs
    public List<DepartementDTO> getAllDepartements() {
        return departementRepository.findAll()
                .stream()
                .map(this::convertToDTO) // Pack each department into a DTO
                .collect(Collectors.toList());
    }

    // 2. The magic "Packing" method
    public DepartementDTO convertToDTO(Departement departement) {
        DepartementDTO dto = new DepartementDTO();
        dto.setIdDept(departement.getIdDept());
        dto.setNomDept(departement.getNomDept());

        // Safely extract the manager's public details if a manager exists
        if (departement.getManager() != null) {
            dto.setManagerId(departement.getManager().getIdUser());
            dto.setManagerMatricule(departement.getManager().getMatricule());
            dto.setManagerEmail(departement.getManager().getEmail());
        }

        return dto;
    }
}