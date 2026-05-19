package com.rabbiter.ol.vo;

import java.util.Date;

public class QuestionVo {

    private Integer id;
    private Integer questionType;
    private String stem;
    private String options;
    private String answer;
    private String analysis;
    private Double score;
    private Integer difficulty;
    private Integer creatorId;
    private Integer courseId;
    private Integer chapterId;
    private Integer status;

    // page params
    private Integer page;
    private Integer pageSize;

    // offset for pagination
    private Integer offset;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getQuestionType() { return questionType; }
    public void setQuestionType(Integer questionType) { this.questionType = questionType; }

    public String getStem() { return stem; }
    public void setStem(String stem) { this.stem = stem; }

    public String getOptions() { return options; }
    public void setOptions(String options) { this.options = options; }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public String getAnalysis() { return analysis; }
    public void setAnalysis(String analysis) { this.analysis = analysis; }

    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }

    public Integer getDifficulty() { return difficulty; }
    public void setDifficulty(Integer difficulty) { this.difficulty = difficulty; }

    public Integer getCreatorId() { return creatorId; }
    public void setCreatorId(Integer creatorId) { this.creatorId = creatorId; }

    public Integer getCourseId() { return courseId; }
    public void setCourseId(Integer courseId) { this.courseId = courseId; }

    public Integer getChapterId() { return chapterId; }
    public void setChapterId(Integer chapterId) { this.chapterId = chapterId; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

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