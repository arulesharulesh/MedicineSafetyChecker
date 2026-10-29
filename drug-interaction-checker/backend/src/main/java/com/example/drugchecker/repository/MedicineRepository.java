package com.example.drugchecker.repository;

import com.example.drugchecker.model.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    Optional<Medicine> findByNameIgnoreCase(String name);

    List<Medicine> findByNameContainingIgnoreCase(String namePart);
}
