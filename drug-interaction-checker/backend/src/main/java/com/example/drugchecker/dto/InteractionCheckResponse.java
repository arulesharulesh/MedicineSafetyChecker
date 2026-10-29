package com.example.drugchecker.dto;

import java.util.List;

public class InteractionCheckResponse {

    private List<String> medicines;
    private List<String> unknownMedicines; // medicines not found in the local database
    private List<InteractionResultDto> interactions;
    private String disclaimer;

    public InteractionCheckResponse() {}

    public List<String> getMedicines() { return medicines; }
    public void setMedicines(List<String> medicines) { this.medicines = medicines; }

    public List<String> getUnknownMedicines() { return unknownMedicines; }
    public void setUnknownMedicines(List<String> unknownMedicines) { this.unknownMedicines = unknownMedicines; }

    public List<InteractionResultDto> getInteractions() { return interactions; }
    public void setInteractions(List<InteractionResultDto> interactions) { this.interactions = interactions; }

    public String getDisclaimer() { return disclaimer; }
    public void setDisclaimer(String disclaimer) { this.disclaimer = disclaimer; }
}
