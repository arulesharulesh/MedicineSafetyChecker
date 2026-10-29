package com.example.drugchecker.service;

import com.example.drugchecker.dto.InteractionCheckRequest;
import com.example.drugchecker.exception.InvalidRequestException;
import com.example.drugchecker.model.DrugInteraction;
import com.example.drugchecker.model.Medicine;
import com.example.drugchecker.model.Severity;
import com.example.drugchecker.repository.DrugInteractionRepository;
import com.example.drugchecker.repository.MedicineRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class InteractionServiceTest {

    private MedicineRepository medicineRepository;
    private DrugInteractionRepository interactionRepository;
    private InteractionService interactionService;

    @BeforeEach
    void setUp() {
        medicineRepository = mock(MedicineRepository.class);
        interactionRepository = mock(DrugInteractionRepository.class);
        MedicineNameExtractionService extractionService = new MedicineNameExtractionService(medicineRepository);
        ExplanationService explanationService = new ExplanationService();
        AiClientService aiClientService = mock(AiClientService.class);
        interactionService = new InteractionService(extractionService, interactionRepository, explanationService, aiClientService);
    }

    @Test
    void rejectsFewerThanTwoMedicines() {
        InteractionCheckRequest req = new InteractionCheckRequest();
        req.setMedicines(List.of("Aspirin"));
        assertThrows(InvalidRequestException.class, () -> interactionService.checkInteractions(req));
    }

    @Test
    void generatesAllPairsAndMatchesBidirectionally() {
        Medicine aspirin = new Medicine("Aspirin", "Acetylsalicylic acid", "Bayer", "demo");
        Medicine warfarin = new Medicine("Warfarin", "Warfarin sodium", "Coumadin", "demo");

        when(medicineRepository.findByNameIgnoreCase("Aspirin")).thenReturn(Optional.of(aspirin));
        when(medicineRepository.findByNameIgnoreCase("Warfarin")).thenReturn(Optional.of(warfarin));

        DrugInteraction interaction = new DrugInteraction();
        interaction.setDrug1("Warfarin"); // stored reversed on purpose to test bidirectional match
        interaction.setDrug2("Aspirin");
        interaction.setSeverity(Severity.HIGH);
        interaction.setInteractionDescription("demo bleeding risk");
        interaction.setWarning("consult a professional");

        when(interactionRepository.findByDrugPairIgnoreCase("Aspirin", "Warfarin"))
                .thenReturn(List.of(interaction));

        InteractionCheckRequest req = new InteractionCheckRequest();
        req.setMedicines(List.of("aspirin ", " Warfarin"));

        var response = interactionService.checkInteractions(req);

        assertEquals(2, response.getMedicines().size());
        assertEquals(1, response.getInteractions().size());
        assertEquals("HIGH", response.getInteractions().get(0).getSeverity());
        assertEquals("DATABASE", response.getInteractions().get(0).getSource());
    }

    @Test
    void deduplicatesCaseInsensitiveDuplicateMedicines() {
        Medicine aspirin = new Medicine("Aspirin", "Acetylsalicylic acid", "Bayer", "demo");
        Medicine metformin = new Medicine("Metformin", "Metformin HCl", "Glucophage", "demo");
        when(medicineRepository.findByNameIgnoreCase("Aspirin")).thenReturn(Optional.of(aspirin));
        when(medicineRepository.findByNameIgnoreCase("ASPIRIN")).thenReturn(Optional.of(aspirin));
        when(medicineRepository.findByNameIgnoreCase("Metformin")).thenReturn(Optional.of(metformin));
        when(interactionRepository.findByDrugPairIgnoreCase(anyString(), anyString())).thenReturn(List.of());

        InteractionCheckRequest req = new InteractionCheckRequest();
        req.setMedicines(List.of("Aspirin", "ASPIRIN", "Metformin"));

        var response = interactionService.checkInteractions(req);
        assertEquals(2, response.getMedicines().size(), "duplicate (case-insensitive) medicine should be collapsed");
    }
}
