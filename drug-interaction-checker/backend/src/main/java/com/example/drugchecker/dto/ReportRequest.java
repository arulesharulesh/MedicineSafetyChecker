package com.example.drugchecker.dto;

public class ReportRequest {
    private String patientName;
    private InteractionCheckResponse checkResult;

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public InteractionCheckResponse getCheckResult() { return checkResult; }
    public void setCheckResult(InteractionCheckResponse checkResult) { this.checkResult = checkResult; }
}
