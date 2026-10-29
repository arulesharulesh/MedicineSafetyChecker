package com.example.drugchecker.repository;

import com.example.drugchecker.model.DrugInteraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DrugInteractionRepository extends JpaRepository<DrugInteraction, Long> {

    @Query("SELECT di FROM DrugInteraction di WHERE " +
           "(LOWER(di.drug1) = LOWER(:a) AND LOWER(di.drug2) = LOWER(:b)) OR " +
           "(LOWER(di.drug1) = LOWER(:b) AND LOWER(di.drug2) = LOWER(:a))")
    List<DrugInteraction> findByDrugPairIgnoreCase(@Param("a") String a, @Param("b") String b);
}
