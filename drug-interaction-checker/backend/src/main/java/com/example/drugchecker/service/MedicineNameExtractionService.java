package com.example.drugchecker.service;

import com.example.drugchecker.model.Medicine;
import com.example.drugchecker.repository.MedicineRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Normalizes user-entered medicine names and matches them against the
 * known medicines table. Does NOT invent drug information - if a name
 * isn't found (even after light fuzzy matching), it is reported as unknown.
 */
@Service
public class MedicineNameExtractionService {

    private final MedicineRepository medicineRepository;

    public MedicineNameExtractionService(MedicineRepository medicineRepository) {
        this.medicineRepository = medicineRepository;
    }

    public static class ExtractionResult {
        public final String normalizedInput;
        public final Medicine matchedMedicine; // null if not found
        public ExtractionResult(String normalizedInput, Medicine matchedMedicine) {
            this.normalizedInput = normalizedInput;
            this.matchedMedicine = matchedMedicine;
        }
        public boolean isFound() { return matchedMedicine != null; }
        public String getStandardizedName() {
            return matchedMedicine != null ? matchedMedicine.getName() : normalizedInput;
        }
    }

    /** Trim, collapse whitespace. Does not change case of the returned display string. */
    public String normalize(String rawInput) {
        if (rawInput == null) return "";
        return rawInput.trim().replaceAll("\\s+", " ");
    }

    public ExtractionResult extract(String rawInput) {
        String normalized = normalize(rawInput);
        if (normalized.isEmpty()) {
            return new ExtractionResult(normalized, null);
        }

        // 1) Exact case-insensitive match
        Optional<Medicine> exact = medicineRepository.findByNameIgnoreCase(normalized);
        if (exact.isPresent()) {
            return new ExtractionResult(normalized, exact.get());
        }

        // 2) Light fuzzy match: containment match against known names/generic/brand
        //    (handles minor typos/variants without inventing new drug data)
        var candidates = medicineRepository.findByNameContainingIgnoreCase(normalized);
        if (!candidates.isEmpty()) {
            return new ExtractionResult(normalized, candidates.get(0));
        }

        // Not found - report honestly, do not fabricate
        return new ExtractionResult(normalized, null);
    }
}
