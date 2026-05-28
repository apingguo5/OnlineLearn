package com.rabbiter.ol.vo;

import java.io.Serializable;
import java.util.List;

public class WeakPointDiagnosisVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer chapterId;
    private String chapterName;
    private Integer courseId;
    private String courseName;
    private Double masteryRate;
    private Integer totalQuestions;
    private Integer wrongCount;
    private Double weaknessScore;
    private String level;
    private List<WeakPointResourceVo> recommendedQuestions;
    private List<WeakPointResourceVo> recommendedVideos;

    public Integer getChapterId() { return chapterId; }
    public void setChapterId(Integer chapterId) { this.chapterId = chapterId; }

    public String getChapterName() { return chapterName; }
    public void setChapterName(String chapterName) { this.chapterName = chapterName; }

    public Integer getCourseId() { return courseId; }
    public void setCourseId(Integer courseId) { this.courseId = courseId; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public Double getMasteryRate() { return masteryRate; }
    public void setMasteryRate(Double masteryRate) { this.masteryRate = masteryRate; }

    public Integer getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(Integer totalQuestions) { this.totalQuestions = totalQuestions; }

    public Integer getWrongCount() { return wrongCount; }
    public void setWrongCount(Integer wrongCount) { this.wrongCount = wrongCount; }

    public Double getWeaknessScore() { return weaknessScore; }
    public void setWeaknessScore(Double weaknessScore) { this.weaknessScore = weaknessScore; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public List<WeakPointResourceVo> getRecommendedQuestions() { return recommendedQuestions; }
    public void setRecommendedQuestions(List<WeakPointResourceVo> recommendedQuestions) { this.recommendedQuestions = recommendedQuestions; }

    public List<WeakPointResourceVo> getRecommendedVideos() { return recommendedVideos; }
    public void setRecommendedVideos(List<WeakPointResourceVo> recommendedVideos) { this.recommendedVideos = recommendedVideos; }

    public static class WeakPointResourceVo implements Serializable {
        private static final long serialVersionUID = 1L;

        private Integer resourceId;
        private String title;
        private String description;
        private Integer difficulty;
        private String resourceType;

        public Integer getResourceId() { return resourceId; }
        public void setResourceId(Integer resourceId) { this.resourceId = resourceId; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public Integer getDifficulty() { return difficulty; }
        public void setDifficulty(Integer difficulty) { this.difficulty = difficulty; }

        public String getResourceType() { return resourceType; }
        public void setResourceType(String resourceType) { this.resourceType = resourceType; }
    }
}
