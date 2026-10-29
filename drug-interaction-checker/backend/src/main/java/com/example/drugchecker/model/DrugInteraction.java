package com.example.drugchecker.model;

import jakarta.persistence.*;

@Entity
@Table(name = "drug_interactions")
public class DrugInteraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "drug1", nullable = false, length = 150)
    private String drug1;

    @Column(name = "drug2", nullable = false, length = 150)
    private String drug2;

    @Column(name = "interaction_description", length = 2000)
    private String interactionDescription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Severity severity;

    @Column(length = 1000)
    private String management;

    @Column(length = 1000)
    private String warning;

    public DrugInteraction() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDrug1() { return drug1; }
    public void setDrug1(String drug1) { this.drug1 = drug1; }

    public String getDrug2() { return drug2; }
    public void setDrug2(String drug2) { this.drug2 = drug2; }

    public String getInteractionDescription() { return interactionDescription; }
    public void setInteractionDescription(String interactionDescription) { this.interactionDescription = interactionDescription; }

    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }

    public String getManagement() { return management; }
    public void setManagement(String management) { this.management = management; }

    public String getWarning() { return warning; }
    public void setWarning(String warning) { this.warning = warning; }

    /** True if this record matches the unordered pair {a, b}, case-insensitively. */
    public boolean matchesPair(String a, String b) {
        boolean forward = drug1.equalsIgnoreCase(a) && drug2.equalsIgnoreCase(b);
        boolean reverse = drug1.equalsIgnoreCase(b) && drug2.equalsIgnoreCase(a);
        return forward || reverse;
    }
}
