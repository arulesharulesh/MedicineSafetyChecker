package com.example.drugchecker.service;

import com.example.drugchecker.dto.InteractionCheckResponse;
import com.example.drugchecker.dto.ReportRequest;
import com.example.drugchecker.exception.ResourceNotFoundException;
import com.example.drugchecker.model.Report;
import com.example.drugchecker.repository.ReportRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final ObjectMapper objectMapper;

    public ReportService(ReportRepository reportRepository, ObjectMapper objectMapper) {
        this.reportRepository = reportRepository;
        this.objectMapper = objectMapper;
    }

    public Report createReport(ReportRequest request) {
        InteractionCheckResponse result = request.getCheckResult();
        Report report = new Report();
        report.setPatientName(request.getPatientName());
        report.setMedicines(result != null && result.getMedicines() != null
                ? String.join(", ", result.getMedicines()) : "");
        report.setInteractionCount(result != null && result.getInteractions() != null
                ? result.getInteractions().size() : 0);
        try {
            report.setReportData(objectMapper.writeValueAsString(result));
        } catch (Exception e) {
            report.setReportData("{\"error\":\"could not serialize report data\"}");
        }
        return reportRepository.save(report);
    }

    public Report getReport(Long id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found: " + id));
    }
}
