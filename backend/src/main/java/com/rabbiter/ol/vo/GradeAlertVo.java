package com.rabbiter.ol.vo;

import java.io.Serializable;
import java.util.List;

public class GradeAlertVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer userId;
    private String userName;
    private Integer classId;
    private String className;
    private String alertLevel;
    private String alertLabel;
    private Double currentScore;
    private Double averageScore;
    private String trend;
    private Double trendSlope;
    private Double volatility;
    private List<AlertActionVo> recommendedActions;

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public Integer getClassId() { return classId; }
    public void setClassId(Integer classId) { this.classId = classId; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getAlertLevel() { return alertLevel; }
    public void setAlertLevel(String alertLevel) { this.alertLevel = alertLevel; }

    public String getAlertLabel() { return alertLabel; }
    public void setAlertLabel(String alertLabel) { this.alertLabel = alertLabel; }

    public Double getCurrentScore() { return currentScore; }
    public void setCurrentScore(Double currentScore) { this.currentScore = currentScore; }

    public Double getAverageScore() { return averageScore; }
    public void setAverageScore(Double averageScore) { this.averageScore = averageScore; }

    public String getTrend() { return trend; }
    public void setTrend(String trend) { this.trend = trend; }

    public Double getTrendSlope() { return trendSlope; }
    public void setTrendSlope(Double trendSlope) { this.trendSlope = trendSlope; }

    public Double getVolatility() { return volatility; }
    public void setVolatility(Double volatility) { this.volatility = volatility; }

    public List<AlertActionVo> getRecommendedActions() { return recommendedActions; }
    public void setRecommendedActions(List<AlertActionVo> recommendedActions) { this.recommendedActions = recommendedActions; }

    public static class AlertActionVo implements Serializable {
        private static final long serialVersionUID = 1L;

        private String actionType;
        private String title;
        private String description;
        private String targetUrl;

        public String getActionType() { return actionType; }
        public void setActionType(String actionType) { this.actionType = actionType; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getTargetUrl() { return targetUrl; }
        public void setTargetUrl(String targetUrl) { this.targetUrl = targetUrl; }
    }
}
