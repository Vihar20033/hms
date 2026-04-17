package com.hms.appointment.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class SeverityScoreService {

    private static final Map<String, Integer> SYMPTOM_WEIGHTS = Map.of(
        "chest pain", 10,
        "difficulty breathing", 9,
        "heavy bleeding", 9,
        "high fever", 7,
        "severe headache", 6,
        "broken bone", 5,
        "cough", 2,
        "cold", 1,
        "regular checkup", 0
    );

    /**
     * Calculates a severity score based on symptoms and emergency status.
     * @param symptoms The patient's reported symptoms
     * @param isEmergency Whether it's marked as an emergency
     * @return Score between 0 and 100
     */
    public int calculateScore(String symptoms, boolean isEmergency) {
        if (isEmergency) return 100;
        if (symptoms == null || symptoms.isBlank()) return 10;

        String normalized = symptoms.toLowerCase();
        int score = 0;

        for (Map.Entry<String, Integer> entry : SYMPTOM_WEIGHTS.entrySet()) {
            if (normalized.contains(entry.getKey())) {
                score = Math.max(score, entry.getValue() * 10);
            }
        }

        return Math.min(score, 100);
    }
}
