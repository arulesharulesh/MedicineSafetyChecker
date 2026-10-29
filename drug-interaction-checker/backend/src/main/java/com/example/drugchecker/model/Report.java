package com.example.drugchecker.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_name", length = 150)
    private String patientName;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Comma-separated list of normalized medicine names
    @Column(name = "medicines", length = 1000, nullable = false)
    private String medicines;

    @Column(name = "interaction_count", nullable = false)
    private Integer interactionCount;

    // Full JSON snapshot of the interaction-check response, for reproducing the report later
    @Lob
    @Column(name = "report_data", columnDefinition = "LONGTEXT")
    private String reportData;

    public Report() {}

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getMedicines() { return medicines; }
    public void setMedicines(String medicines) { this.medicines = medicines; }

    public Integer getInteractionCount() { return interactionCount; }
    public void setInteractionCount(Integer interactionCount) { this.interactionCount = interactionCount; }

    public String getReportData() { return reportData; }
    public void setReportData(String reportData) { this.reportData = reportData; }
}
