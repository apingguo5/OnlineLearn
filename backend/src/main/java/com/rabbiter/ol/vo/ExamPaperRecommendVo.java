package com.rabbiter.ol.vo;

import java.io.Serializable;

public class ExamPaperRecommendVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer paperId;
    private String title;
    private String description;
    private Integer paperType;
    private Double totalScore;
    private Integer duration;
    private Integer questionCount;
    private Double avgDifficulty;
    private Double studentAbility;
    private Double matchScore;
    private String matchLevel;
    private String recommendReason;
    private Integer courseId;
    private String courseName;

    public Integer getPaperId() { return paperId; }
    public void setPaperId(Integer paperId) { this.paperId = paperId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getPaperType() { return paperType; }
    public void setPaperType(Integer paperType) { this.paperType = paperType; }

    public Double getTotalScore() { return totalScore; }
    public void setTotalScore(Double totalScore) { this.totalScore = totalScore; }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }

    public Integer getQuestionCount() { return questionCount; }
    public void setQuestionCount(Integer questionCount) { this.questionCount = questionCount; }

    public Double getAvgDifficulty() { return avgDifficulty; }
    public void setAvgDifficulty(Double avgDifficulty) { this.avgDifficulty = avgDifficulty; }

    public Double getStudentAbility() { return studentAbility; }
    public void setStudentAbility(Double studentAbility) { this.studentAbility = studentAbility; }

    public Double getMatchScore() { return matchScore; }
    public void setMatchScore(Double matchScore) { this.matchScore = matchScore; }

    public String getMatchLevel() { return matchLevel; }
    public void setMatchLevel(String matchLevel) { this.matchLevel = matchLevel; }

    public String getRecommendReason() { return recommendReason; }
    public void setRecommendReason(String recommendReason) { this.recommendReason = recommendReason; }

    public Integer getCourseId() { return courseId; }
    public void setCourseId(Integer courseId) { this.courseId = courseId; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
}
