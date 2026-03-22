package com.somepharm.hrportal.controller;

import com.somepharm.hrportal.entity.Departement;
import com.somepharm.hrportal.service.DepartementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departements")
public class DepartementController {

    private final DepartementService departementService;

    public DepartementController(DepartementService departementService) {
        this.departementService = departementService;
    }

    // POST: Create a new Department
    @PostMapping
    public ResponseEntity<Departement> createDepartement(@RequestBody Departement departement) {
        Departement savedDept = departementService.createDepartement(departement);
        return new ResponseEntity<>(savedDept, HttpStatus.CREATED);
    }

    // GET: Get all Departments
    @GetMapping
    public ResponseEntity<List<Departement>> getAllDepartements() {
        return ResponseEntity.ok(departementService.getAllDepartements());
    }
}