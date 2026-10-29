package com.example.drugchecker.service;

import com.example.drugchecker.dto.InteractionCheckRequest;
import com.example.drugchecker.dto.InteractionCheckResponse;
import com.example.drugchecker.dto.InteractionResultDto;
import com.example.drugchecker.exception.AiServiceUnavailableException;
import com.example.drugchecker.exception.InvalidRequestException;
import com.example.drugchecker.model.DrugInteraction;
import com.example.drugchecker.model.Severity;
import com.example.drugchecker.repository.DrugInteractionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Core orchestration: normalize input -> generate pairs -> check DB
 * (bidirectionally) -> fall back to ML prediction only when no DB
 * record exists -> build explanations -> assemble response.
 */
@Service
public class InteractionService {

    private static final Logger log = LoggerFactory.getLogger(InteractionService.class);
    public static final String DISCLAIMER =
            "Medical Safety Disclaimer: This application is intended for educational and decision-support " +
            "purposes only. Interaction information may be incomplete or outdated. Do not start, stop, change, " +
            "or substitute medication based solely on this application. Always consult a qualified healthcare professional.";

    private final MedicineNameExtractionService extractionService;
    private final DrugInteractionRepository interactionRepository;
    private final ExplanationService explanationService;
    private final AiClientService aiClientService;

    public InteractionService(MedicineNameExtractionService extractionService,
                               DrugInteractionRepository interactionRepository,
                               ExplanationService explanationService,
                               AiClientService aiClientService) {
        this.extractionService = extractionService;
        this.interactionRepository = interactionRepository;
        this.explanationService = explanationService;
        this.aiClientService = aiClientService;
    }

    public InteractionCheckResponse checkInteractions(InteractionCheckRequest request) {
        if (request == null || request.getMedicines() == null) {
            throw new InvalidRequestException("Please provide a medicine list.");
        }

        List<String> rawMedicines = request.getMedicines().stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .toList();

        if (rawMedicines.size() < 2) {
            throw new InvalidRequestException("At least 2 medicines are required.");
        }
        if (rawMedicines.size() > 10) {
            throw new InvalidRequestException("A maximum of 10 medicines is allowed.");
        }

        // Normalize + extract, tracking unknowns and de-duplicating (case-insensitive)
        LinkedHashMap<String, String> standardizedByInputOrder = new LinkedHashMap<>(); // display name -> display name
        List<String> unknown = new ArrayList<>();
        Set<String> seenLower = new HashSet<>();

        for (String raw : rawMedicines) {
            var result = extractionService.extract(raw);
            if (result.normalizedInput.isEmpty()) {
                continue;
            }
            String standardized = result.getStandardizedName();
            String key = standardized.toLowerCase(Locale.ROOT);
            if (seenLower.contains(key)) {
                continue; // skip duplicates
            }
            seenLower.add(key);
            standardizedByInputOrder.put(standardized, standardized);
            if (!result.isFound()) {
                unknown.add(result.normalizedInput);
            }
        }

        if (standardizedByInputOrder.size() < 2) {
            throw new InvalidRequestException("Please enter at least 2 valid medicine names.");
        }

        List<String> medicines = new ArrayList<>(standardizedByInputOrder.keySet());

        List<InteractionResultDto> results = new ArrayList<>();
        for (int i = 0; i < medicines.size(); i++) {
            for (int j = i + 1; j < medicines.size(); j++) {
                results.add(checkPair(medicines.get(i), medicines.get(j)));
            }
        }

        InteractionCheckResponse response = new InteractionCheckResponse();
        response.setMedicines(medicines);
        response.setUnknownMedicines(unknown);
        response.setInteractions(results);
        response.setDisclaimer(DISCLAIMER);
        return response;
    }

    private InteractionResultDto checkPair(String drugA, String drugB) {
        InteractionResultDto dto = new InteractionResultDto();
        dto.setDrug1(drugA);
        dto.setDrug2(drugB);
        dto.setWarning(ExplanationService.CONSULT_WARNING);

        List<DrugInteraction> matches = interactionRepository.findByDrugPairIgnoreCase(drugA, drugB);

        if (!matches.isEmpty()) {
            DrugInteraction match = matches.get(0);
            dto.setSeverity(match.getSeverity().name());
            dto.setInteraction(match.getInteractionDescription());
            dto.setManagement(match.getManagement());
            dto.setSource("DATABASE");
            dto.setExplanation(explanationService.buildExplanation(
                    drugA, drugB, match.getSeverity(), match.getInteractionDescription()));
            if (match.getWarning() != null && !match.getWarning().isBlank()) {
                dto.setWarning(match.getWarning());
            }
            return dto;
        }

        // No DB record - try ML prediction as a clearly-labeled estimate.
        // Failure here must not break the response.
        try {
            var prediction = aiClientService.predictSeverity(drugA, drugB, null);
            Severity predictedSeverity = Severity.fromStringSafe(prediction.severity);
            dto.setSeverity(predictedSeverity.name());
            dto.setInteraction(explanationService.buildNoRecordExplanation(drugA, drugB));
            dto.setSource("ML_MODEL_PREDICTION");
            dto.setConfidence(prediction.confidence);
            dto.setExplanation(
                    explanationService.buildExplanation(drugA, drugB, predictedSeverity, null)
                            + explanationService.buildMlDisclaimerSuffix());
        } catch (AiServiceUnavailableException e) {
            dto.setSeverity(Severity.NONE.name());
            dto.setInteraction(explanationService.buildNoRecordExplanation(drugA, drugB));
            dto.setSource("NO_RECORD");
            dto.setExplanation("No database record was found, and the AI prediction service is currently unavailable. " +
                    "This result is inconclusive, not a confirmation of safety.");
        }

        return dto;
    }
}
