package com.example.drugchecker.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public class InteractionCheckRequest {

    @NotEmpty(message = "medicines list must not be empty")
    @Size(min = 2, max = 10, message = "provide between 2 and 10 medicines")
    private List<@jakarta.validation.constraints.NotBlank(message = "medicine name must not be blank") String> medicines;

    // optional - for attaching a report later
    private String patientName;

    public List<String> getMedicines() { return medicines; }
    public void setMedicines(List<String> medicines) { this.medicines = medicines; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
}
