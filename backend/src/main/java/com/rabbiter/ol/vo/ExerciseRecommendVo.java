package com.rabbiter.ol.vo;

import java.io.Serializable;

public class ExerciseRecommendVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer exerciseId;
    private String title;
    private String content;
    private Integer classId;
    private Double weaknessScore;
    private String recommendReason;
    private String knowledgePoint;

    public Integer getExerciseId() { return exerciseId; }
    public void setExerciseId(Integer exerciseId) { this.exerciseId = exerciseId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Integer getClassId() { return classId; }
    public void setClassId(Integer classId) { this.classId = classId; }

    public Double getWeaknessScore() { return weaknessScore; }
    public void setWeaknessScore(Double weaknessScore) { this.weaknessScore = weaknessScore; }

    public String getRecommendReason() { return recommendReason; }
    public void setRecommendReason(String recommendReason) { this.recommendReason = recommendReason; }

    public String getKnowledgePoint() { return knowledgePoint; }
    public void setKnowledgePoint(String knowledgePoint) { this.knowledgePoint = knowledgePoint; }
}
