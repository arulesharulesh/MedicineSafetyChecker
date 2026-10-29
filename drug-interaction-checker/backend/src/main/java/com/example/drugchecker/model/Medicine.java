package com.example.drugchecker.model;

import jakarta.persistence.*;

@Entity
@Table(name = "medicines", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
public class Medicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String name;

    @Column(name = "generic_name", length = 150)
    private String genericName;

    @Column(name = "brand_name", length = 150)
    private String brandName;

    @Column(length = 1000)
    private String description;

    public Medicine() {}

    public Medicine(String name, String genericName, String brandName, String description) {
        this.name = name;
        this.genericName = genericName;
        this.brandName = brandName;
        this.description = description;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGenericName() { return genericName; }
    public void setGenericName(String genericName) { this.genericName = genericName; }

    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
