package com.rabbiter.ol.service;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;

public final class AssessmentSimilarityUtil {

    private AssessmentSimilarityUtil() {}

    private static final Pattern TOKEN_PATTERN = Pattern.compile("[\\u4e00-\\u9fa5a-zA-Z0-9]+");

    public static double textSimilarity(String textA, String textB) {
        if (textA == null || textB == null || textA.isEmpty() || textB.isEmpty()) {
            return 0.0;
        }
        Map<String, Integer> tfA = tokenize(textA);
        Map<String, Integer> tfB = tokenize(textB);
        Set<String> allTokens = new HashSet<>();
        allTokens.addAll(tfA.keySet());
        allTokens.addAll(tfB.keySet());
        if (allTokens.isEmpty()) return 0.0;

        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (String token : allTokens) {
            double va = tfA.getOrDefault(token, 0);
            double vb = tfB.getOrDefault(token, 0);
            dot += va * vb;
            normA += va * va;
            normB += vb * vb;
        }
        if (normA == 0 || normB == 0) return 0.0;
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private static Map<String, Integer> tokenize(String text) {
        Map<String, Integer> tf = new HashMap<>();
        java.util.regex.Matcher m = TOKEN_PATTERN.matcher(text);
        while (m.find()) {
            String token = m.group().toLowerCase();
            if (token.length() >= 2) {
                tf.merge(token, 1, Integer::sum);
            }
        }
        return tf;
    }

    public static double calculateStudentAbility(List<Map<String, Object>> recentScores) {
        if (recentScores == null || recentScores.isEmpty()) {
            return 0.0;
        }
        int total = 0;
        int correct = 0;
        double avgDifficulty = 0.0;
        int diffCount = 0;
        for (Map<String, Object> row : recentScores) {
            Double studentScore = toDouble(row.get("studentScore"));
            Double fullScore = toDouble(row.get("fullScore"));
            int diff = toInt(row.get("difficulty"), 1);
            if (fullScore != null && fullScore > 0 && studentScore != null) {
                if (studentScore >= fullScore * 0.6) correct++;
                total++;
                avgDifficulty += diff;
                diffCount++;
            }
        }
        if (total == 0) return 0.0;
        double accuracy = (double) correct / total;
        accuracy = Math.max(0.01, Math.min(0.99, accuracy));
        double ability = Math.log(accuracy / (1.0 - accuracy));
        return Math.max(-3.0, Math.min(3.0, ability));
    }

    public static double calculateForgettingWeight(Date submitTime) {
        if (submitTime == null) return 1.0;
        long days = (System.currentTimeMillis() - submitTime.getTime()) / (24 * 3600_000L);
        if (days < 3) return 1.0;
        if (days < 7) return 1.3;
        if (days < 14) return 1.5;
        return 1.8;
    }

    public static int abilityToLevel(double ability) {
        if (ability <= -1.0) return 1;
        if (ability <= 1.0) return 2;
        return 3;
    }

    public static double calculateMatchScore(double difficulty, double ability) {
        double diff = Math.abs(difficulty - abilityToLevel(ability));
        return 1.0 - diff / 3.0;
    }

    public static double[] linearRegression(List<Double> x, List<Double> y) {
        if (x == null || y == null || x.size() != y.size() || x.size() < 2) {
            return new double[]{0.0, 0.0};
        }
        int n = x.size();
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        for (int i = 0; i < n; i++) {
            double xi = x.get(i);
            double yi = y.get(i);
            sumX += xi;
            sumY += yi;
            sumXY += xi * yi;
            sumX2 += xi * xi;
        }
        double denom = n * sumX2 - sumX * sumX;
        if (Math.abs(denom) < 1e-10) return new double[]{0.0, 0.0};
        double slope = (n * sumXY - sumX * sumY) / denom;
        double intercept = (sumY - slope * sumX) / n;
        return new double[]{slope, intercept};
    }

    public static double coefficientOfVariation(List<Double> values) {
        if (values == null || values.size() < 2) return 0.0;
        double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        if (Math.abs(mean) < 1e-10) return 0.0;
        double variance = values.stream().mapToDouble(v -> Math.pow(v - mean, 2)).average().orElse(0);
        return Math.sqrt(variance) / Math.abs(mean);
    }

    private static Double toDouble(Object v) {
        if (v == null) return null;
        try { return Double.parseDouble(v.toString()); } catch (NumberFormatException e) { return null; }
    }

    private static Integer toInt(Object v, int defaultVal) {
        if (v == null) return defaultVal;
        try { return Integer.parseInt(v.toString()); } catch (NumberFormatException e) { return defaultVal; }
    }
}
