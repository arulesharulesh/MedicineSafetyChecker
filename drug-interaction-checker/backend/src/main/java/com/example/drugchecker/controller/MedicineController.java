package com.example.drugchecker.controller;

import com.example.drugchecker.dto.MedicineDto;
import com.example.drugchecker.service.MedicineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medicines")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:5500", "http://localhost:5500"}, allowCredentials = "true")
public class MedicineController {

    private final MedicineService medicineService;

    public MedicineController(MedicineService medicineService) {
        this.medicineService = medicineService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<MedicineDto>> search(@RequestParam(required = false) String name) {
        return ResponseEntity.ok(medicineService.search(name));
    }
}
