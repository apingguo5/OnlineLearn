package com.rabbiter.ol.vo;

import java.util.Date;
import java.util.List;

public class ExamPaperVo {

    private Integer id;
    private String title;
    private String description;
    private Integer paperType;
    private Integer courseId;
    private Integer chapterId;
    private Integer classId;
    private Integer creatorId;
    private Integer duration;
    private Double totalScore;
    private Integer status;
    private Date publishTime;
    private Date endTime;

    // question ids for paper composition
    private List<Integer> questionIds;

    // page params
    private Integer page;
    private Integer pageSize;
    private Integer offset;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getPaperType() { return paperType; }
    public void setPaperType(Integer paperType) { this.paperType = paperType; }

    public Integer getCourseId() { return courseId; }
    public void setCourseId(Integer courseId) { this.courseId = courseId; }

    public Integer getChapterId() { return chapterId; }
    public void setChapterId(Integer chapterId) { this.chapterId = chapterId; }

    public Integer getClassId() { return classId; }
    public void setClassId(Integer classId) { this.classId = classId; }

    public Integer getCreatorId() { return creatorId; }
    public void setCreatorId(Integer creatorId) { this.creatorId = creatorId; }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }

    public Double getTotalScore() { return totalScore; }
    public void setTotalScore(Double totalScore) { this.totalScore = totalScore; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Date getPublishTime() { return publishTime; }
    public void setPublishTime(Date publishTime) { this.publishTime = publishTime; }

    public Date getEndTime() { return endTime; }
    public void setEndTime(Date endTime) { this.endTime = endTime; }

    public List<Integer> getQuestionIds() { return questionIds; }
    public void setQuestionIds(List<Integer> questionIds) { this.questionIds = questionIds; }

    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page; }

    public Integer getPageSize() { return pageSize; }
    public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }

    /**
     * 兼容前端传参 limit -> pageSize
     */
    public void setLimit(Integer limit) {
        this.pageSize = limit;
    }

    public Integer getOffset() { return offset; }
    public void setOffset(Integer offset) { this.offset = offset; }
}