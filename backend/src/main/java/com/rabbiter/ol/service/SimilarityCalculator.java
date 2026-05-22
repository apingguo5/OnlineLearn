package com.rabbiter.ol.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 协同过滤算法工具类
 * 提供：余弦相似度 / Pearson 相关系数
 */
public final class SimilarityCalculator {

    private SimilarityCalculator() {}

    /**
     * 余弦相似度
     * sim(A, B) = (A · B) / (||A|| * ||B||)
     */
    public static double cosine(Map<Integer, Double> a, Map<Integer, Double> b) {
        if (a == null || b == null || a.isEmpty() || b.isEmpty()) {
            return 0.0;
        }
        Set<Integer> common = new HashSet<>(a.keySet());
        common.retainAll(b.keySet());
        if (common.isEmpty()) {
            return 0.0;
        }

        double dot = 0.0;
        for (Integer k : common) {
            dot += a.get(k) * b.get(k);
        }

        double normA = 0.0;
        for (Double v : a.values()) {
            normA += v * v;
        }
        double normB = 0.0;
        for (Double v : b.values()) {
            normB += v * v;
        }
        if (normA == 0 || normB == 0) {
            return 0.0;
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    /**
     * Pearson 相关系数
     * 适用于打分有偏好倾向（部分用户整体打分偏高/偏低）的场景
     */
    public static double pearson(Map<Integer, Double> a, Map<Integer, Double> b) {
        if (a == null || b == null || a.isEmpty() || b.isEmpty()) {
            return 0.0;
        }
        Set<Integer> common = new HashSet<>(a.keySet());
        common.retainAll(b.keySet());
        int n = common.size();
        if (n < 2) {
            return 0.0;
        }

        double sumA = 0.0, sumB = 0.0;
        for (Integer k : common) {
            sumA += a.get(k);
            sumB += b.get(k);
        }
        double meanA = sumA / n;
        double meanB = sumB / n;

        double num = 0.0, denA = 0.0, denB = 0.0;
        for (Integer k : common) {
            double da = a.get(k) - meanA;
            double db = b.get(k) - meanB;
            num += da * db;
            denA += da * da;
            denB += db * db;
        }
        if (denA == 0 || denB == 0) {
            return 0.0;
        }
        return num / (Math.sqrt(denA) * Math.sqrt(denB));
    }

    /**
     * 转置：将 user->course->rating 转换为 course->user->rating
     */
    public static Map<Integer, Map<Integer, Double>> transpose(Map<Integer, Map<Integer, Double>> matrix) {
        Map<Integer, Map<Integer, Double>> t = new HashMap<>();
        for (Map.Entry<Integer, Map<Integer, Double>> e : matrix.entrySet()) {
            Integer rowKey = e.getKey();
            for (Map.Entry<Integer, Double> inner : e.getValue().entrySet()) {
                t.computeIfAbsent(inner.getKey(), k -> new HashMap<>()).put(rowKey, inner.getValue());
            }
        }
        return t;
    }
}
