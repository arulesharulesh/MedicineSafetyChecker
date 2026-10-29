package com.example.drugchecker.dto;

public class MedicineDto {
    private String name;
    private String genericName;
    private String brandName;
    private String description;

    public MedicineDto() {}

    public MedicineDto(String name, String genericName, String brandName, String description) {
        this.name = name;
        this.genericName = genericName;
        this.brandName = brandName;
        this.description = description;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getGenericName() { return genericName; }
    public void setGenericName(String genericName) { this.genericName = genericName; }
    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
