package com.example.drugchecker.service;

import com.example.drugchecker.exception.AiServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

/**
 * Talks to the Python AI/ML Flask service for ML-based severity prediction.
 * Only invoked when the database has no verified interaction record.
 * Any failure here must NOT crash the interaction-check flow - the
 * database-verified results should still be returned.
 */
@Service
public class AiClientService {

    private static final Logger log = LoggerFactory.getLogger(AiClientService.class);

    private final RestClient restClient;

    public AiClientService(@Value("${ai.service.url}") String aiServiceUrl) {
        this.restClient = RestClient.builder().baseUrl(aiServiceUrl).build();
    }

    public static class PredictionResult {
        public final String severity;
        public final double confidence;
        public PredictionResult(String severity, double confidence) {
            this.severity = severity;
            this.confidence = confidence;
        }
    }

    @SuppressWarnings("unchecked")
    public PredictionResult predictSeverity(String drug1, String drug2, String interactionText) {
        try {
            Map<String, Object> body = Map.of(
                    "drug1", drug1,
                    "drug2", drug2,
                    "interaction", interactionText == null ? "" : interactionText
            );

            Map<String, Object> response = restClient.post()
                    .uri("/predict")
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            if (response == null || !response.containsKey("severity")) {
                throw new AiServiceUnavailableException("AI service returned an unexpected response.");
            }

            String severity = String.valueOf(response.get("severity"));
            double confidence = response.get("confidence") != null
                    ? Double.parseDouble(String.valueOf(response.get("confidence")))
                    : 0.0;

            return new PredictionResult(severity, confidence);
        } catch (RestClientException e) {
            log.warn("AI service call failed: {}", e.getMessage());
            throw new AiServiceUnavailableException("Could not reach the AI prediction service.");
        }
    }
}
