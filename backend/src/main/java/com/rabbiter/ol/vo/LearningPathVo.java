package com.rabbiter.ol.vo;

import java.io.Serializable;
import java.util.List;

public class LearningPathVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer studentId;
    private Integer courseId;
    private String courseName;
    private Double masteryRate;
    private List<PathNode> path;

    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }

    public Integer getCourseId() { return courseId; }
    public void setCourseId(Integer courseId) { this.courseId = courseId; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public Double getMasteryRate() { return masteryRate; }
    public void setMasteryRate(Double masteryRate) { this.masteryRate = masteryRate; }

    public List<PathNode> getPath() { return path; }
    public void setPath(List<PathNode> path) { this.path = path; }

    public static class PathNode implements Serializable {
        private static final long serialVersionUID = 1L;

        private Integer chapterId;
        private String chapterName;
        private String chapterType;
        private Integer parentId;
        private Integer order;
        private String status;
        private Double mastery;
        private String suggestion;

        public Integer getChapterId() { return chapterId; }
        public void setChapterId(Integer chapterId) { this.chapterId = chapterId; }

        public String getChapterName() { return chapterName; }
        public void setChapterName(String chapterName) { this.chapterName = chapterName; }

        public String getChapterType() { return chapterType; }
        public void setChapterType(String chapterType) { this.chapterType = chapterType; }

        public Integer getParentId() { return parentId; }
        public void setParentId(Integer parentId) { this.parentId = parentId; }

        public Integer getOrder() { return order; }
        public void setOrder(Integer order) { this.order = order; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public Double getMastery() { return mastery; }
        public void setMastery(Double mastery) { this.mastery = mastery; }

        public String getSuggestion() { return suggestion; }
        public void setSuggestion(String suggestion) { this.suggestion = suggestion; }
    }
}
