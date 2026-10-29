package com.example.drugchecker.controller;

import com.example.drugchecker.dto.ReportRequest;
import com.example.drugchecker.model.Report;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.drugchecker.service.ReportService;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:5500", "http://localhost:5500"}, allowCredentials = "true")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping
    public ResponseEntity<Report> create(@RequestBody ReportRequest request) {
        return ResponseEntity.ok(reportService.createReport(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Report> get(@PathVariable Long id) {
        return ResponseEntity.ok(reportService.getReport(id));
    }
}
