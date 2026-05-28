package com.rabbiter.ol.vo;

import java.io.Serializable;

public class WrongQuestionRecommendVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer questionId;
    private Integer sourceQuestionId;
    private String sourceQuestionStem;
    private String stem;
    private String options;
    private String answer;
    private String analysis;
    private Integer questionType;
    private Integer difficulty;
    private Double similarityScore;
    private Double forgettingWeight;
    private String recommendReason;
    private Integer courseId;
    private Integer chapterId;
    private String chapterName;

    public Integer getQuestionId() { return questionId; }
    public void setQuestionId(Integer questionId) { this.questionId = questionId; }

    public Integer getSourceQuestionId() { return sourceQuestionId; }
    public void setSourceQuestionId(Integer sourceQuestionId) { this.sourceQuestionId = sourceQuestionId; }

    public String getSourceQuestionStem() { return sourceQuestionStem; }
    public void setSourceQuestionStem(String sourceQuestionStem) { this.sourceQuestionStem = sourceQuestionStem; }

    public String getStem() { return stem; }
    public void setStem(String stem) { this.stem = stem; }

    public String getOptions() { return options; }
    public void setOptions(String options) { this.options = options; }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public String getAnalysis() { return analysis; }
    public void setAnalysis(String analysis) { this.analysis = analysis; }

    public Integer getQuestionType() { return questionType; }
    public void setQuestionType(Integer questionType) { this.questionType = questionType; }

    public Integer getDifficulty() { return difficulty; }
    public void setDifficulty(Integer difficulty) { this.difficulty = difficulty; }

    public Double getSimilarityScore() { return similarityScore; }
    public void setSimilarityScore(Double similarityScore) { this.similarityScore = similarityScore; }

    public Double getForgettingWeight() { return forgettingWeight; }
    public void setForgettingWeight(Double forgettingWeight) { this.forgettingWeight = forgettingWeight; }

    public String getRecommendReason() { return recommendReason; }
    public void setRecommendReason(String recommendReason) { this.recommendReason = recommendReason; }

    public Integer getCourseId() { return courseId; }
    public void setCourseId(Integer courseId) { this.courseId = courseId; }

    public Integer getChapterId() { return chapterId; }
    public void setChapterId(Integer chapterId) { this.chapterId = chapterId; }

    public String getChapterName() { return chapterName; }
    public void setChapterName(String chapterName) { this.chapterName = chapterName; }
}
