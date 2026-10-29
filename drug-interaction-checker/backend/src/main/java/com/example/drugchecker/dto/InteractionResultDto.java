package com.example.drugchecker.dto;

public class InteractionResultDto {

    private String drug1;
    private String drug2;
    private String severity;          // NONE / LOW / MODERATE / HIGH
    private String interaction;       // verified DB description, or a "not found" message
    private String explanation;       // human-readable explanation grounded in DB data
    private String warning;
    private String management;
    private String source;            // "DATABASE" or "ML_MODEL_PREDICTION" or "NO_RECORD"
    private Double confidence;        // only set when source == ML_MODEL_PREDICTION

    public InteractionResultDto() {}

    public String getDrug1() { return drug1; }
    public void setDrug1(String drug1) { this.drug1 = drug1; }

    public String getDrug2() { return drug2; }
    public void setDrug2(String drug2) { this.drug2 = drug2; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getInteraction() { return interaction; }
    public void setInteraction(String interaction) { this.interaction = interaction; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public String getWarning() { return warning; }
    public void setWarning(String warning) { this.warning = warning; }

    public String getManagement() { return management; }
    public void setManagement(String management) { this.management = management; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }
}
