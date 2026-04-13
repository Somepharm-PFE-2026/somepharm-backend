package com.somepharm.hrportal.controller;

import com.somepharm.hrportal.dto.DemandeCongeDTO;
import com.somepharm.hrportal.entity.DemandeConge;
import com.somepharm.hrportal.repository.DemandeCongeRepository;
import com.somepharm.hrportal.service.DemandeCongeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "http://localhost:3000")
public class DashboardController {

    private final DemandeCongeRepository demandeCongeRepository;
    private final DemandeCongeService demandeCongeService;

    public DashboardController(DemandeCongeRepository demandeCongeRepository, DemandeCongeService demandeCongeService) {
        this.demandeCongeRepository = demandeCongeRepository;
        this.demandeCongeService = demandeCongeService;
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        LocalDate today = LocalDate.now();
        List<DemandeConge> allDemandes = demandeCongeRepository.findAll();

        // 1. Rule #3: The Absence Calendar (Logic for who is absent TODAY)
        // We filter requests that are 'APPROUVE' and where 'today' is between Start and End dates
        List<DemandeCongeDTO> absencesAujourdhui = allDemandes.stream()
                .filter(d -> "APPROUVE".equals(d.getStatutCycleVie()))
                .filter(d -> !today.isBefore(d.getDateDebut()) && !today.isAfter(d.getDateFin()))
                .map(demandeCongeService::convertToDTO)
                .collect(Collectors.toList());

        // 2. Rule #2: Urgent Alerts (Filter for high-priority absences like MALADIE)
        List<DemandeCongeDTO> alertesUrgentes = absencesAujourdhui.stream()
                .filter(d -> "MALADIE".equals(d.getTypeConge()) || "MATERNITE".equals(d.getTypeConge()))
                .collect(Collectors.toList());

        // 3. General Stats for the Dashboard Cards
        long enAttente = allDemandes.stream()
                .filter(d -> d.getStatutCycleVie().startsWith("EN_ATTENTE") || d.getStatutCycleVie().equals("VALIDE_MANAGER"))
                .count();

        Map<String, Object> response = new HashMap<>();
        response.put("absencesAujourdhui", absencesAujourdhui);
        response.put("alertesUrgentes", alertesUrgentes);
        response.put("totalEnAttente", enAttente);
        response.put("dateDuJour", today.toString());

        return ResponseEntity.ok(response);
    }
}