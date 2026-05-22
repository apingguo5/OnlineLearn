package com.rabbiter.ol.vo;

import java.io.Serializable;
import java.util.List;

public class CourseRecommendVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer courseId;
    private String courseName;
    private String description;
    private String coverUrl;
    private Double recommendScore;
    private String recommendReason;
    private String recommendType;

    public Integer getCourseId() { return courseId; }
    public void setCourseId(Integer courseId) { this.courseId = courseId; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }

    public Double getRecommendScore() { return recommendScore; }
    public void setRecommendScore(Double recommendScore) { this.recommendScore = recommendScore; }

    public String getRecommendReason() { return recommendReason; }
    public void setRecommendReason(String recommendReason) { this.recommendReason = recommendReason; }

    public String getRecommendType() { return recommendType; }
    public void setRecommendType(String recommendType) { this.recommendType = recommendType; }
}
