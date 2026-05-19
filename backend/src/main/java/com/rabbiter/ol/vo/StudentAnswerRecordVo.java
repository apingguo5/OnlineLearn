package com.rabbiter.ol.vo;

import java.util.Date;

public class StudentAnswerRecordVo {

    private Integer id;
    private Integer paperId;
    private Integer questionId;
    private Integer studentId;
    private String answer;
    private Integer status;
    private Double score;
    private Integer reviewStatus;
    private Integer reviewerId;
    private String remark;

    // page params
    private Integer page;
    private Integer pageSize;
    // offset for pagination
    private Integer offset;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getPaperId() { return paperId; }
    public void setPaperId(Integer paperId) { this.paperId = paperId; }

    public Integer getQuestionId() { return questionId; }
    public void setQuestionId(Integer questionId) { this.questionId = questionId; }

    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }

    public Integer getReviewStatus() { return reviewStatus; }
    public void setReviewStatus(Integer reviewStatus) { this.reviewStatus = reviewStatus; }

    public Integer getReviewerId() { return reviewerId; }
    public void setReviewerId(Integer reviewerId) { this.reviewerId = reviewerId; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

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