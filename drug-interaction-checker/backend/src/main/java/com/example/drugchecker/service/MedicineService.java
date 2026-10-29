package com.example.drugchecker.service;

import com.example.drugchecker.dto.MedicineDto;
import com.example.drugchecker.model.Medicine;
import com.example.drugchecker.repository.MedicineRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MedicineService {

    private final MedicineRepository medicineRepository;

    public MedicineService(MedicineRepository medicineRepository) {
        this.medicineRepository = medicineRepository;
    }

    public List<MedicineDto> search(String namePart) {
        List<Medicine> matches = (namePart == null || namePart.isBlank())
                ? medicineRepository.findAll()
                : medicineRepository.findByNameContainingIgnoreCase(namePart.trim());

        return matches.stream()
                .map(m -> new MedicineDto(m.getName(), m.getGenericName(), m.getBrandName(), m.getDescription()))
                .toList();
    }
}
