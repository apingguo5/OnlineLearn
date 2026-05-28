package com.rabbiter.ol.vo;

import java.io.Serializable;
import java.util.List;

public class PeerCompareVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer userId;
    private Double averageScore;
    private Double classAverageScore;
    private Integer rankPosition;
    private Integer totalStudents;
    private Double rankPercentile;
    private List<PeerStudentVo> similarPeers;
    private List<PeerQuestionVo> peerMasteredQuestions;
    private List<PeerQuestionVo> peerPendingQuestions;

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public Double getAverageScore() { return averageScore; }
    public void setAverageScore(Double averageScore) { this.averageScore = averageScore; }

    public Double getClassAverageScore() { return classAverageScore; }
    public void setClassAverageScore(Double classAverageScore) { this.classAverageScore = classAverageScore; }

    public Integer getRankPosition() { return rankPosition; }
    public void setRankPosition(Integer rankPosition) { this.rankPosition = rankPosition; }

    public Integer getTotalStudents() { return totalStudents; }
    public void setTotalStudents(Integer totalStudents) { this.totalStudents = totalStudents; }

    public Double getRankPercentile() { return rankPercentile; }
    public void setRankPercentile(Double rankPercentile) { this.rankPercentile = rankPercentile; }

    public List<PeerStudentVo> getSimilarPeers() { return similarPeers; }
    public void setSimilarPeers(List<PeerStudentVo> similarPeers) { this.similarPeers = similarPeers; }

    public List<PeerQuestionVo> getPeerMasteredQuestions() { return peerMasteredQuestions; }
    public void setPeerMasteredQuestions(List<PeerQuestionVo> peerMasteredQuestions) { this.peerMasteredQuestions = peerMasteredQuestions; }

    public List<PeerQuestionVo> getPeerPendingQuestions() { return peerPendingQuestions; }
    public void setPeerPendingQuestions(List<PeerQuestionVo> peerPendingQuestions) { this.peerPendingQuestions = peerPendingQuestions; }

    public static class PeerStudentVo implements Serializable {
        private static final long serialVersionUID = 1L;

        private Integer userId;
        private String userName;
        private Double similarityScore;
        private Double averageScore;
        private Integer masteredCount;

        public Integer getUserId() { return userId; }
        public void setUserId(Integer userId) { this.userId = userId; }

        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }

        public Double getSimilarityScore() { return similarityScore; }
        public void setSimilarityScore(Double similarityScore) { this.similarityScore = similarityScore; }

        public Double getAverageScore() { return averageScore; }
        public void setAverageScore(Double averageScore) { this.averageScore = averageScore; }

        public Integer getMasteredCount() { return masteredCount; }
        public void setMasteredCount(Integer masteredCount) { this.masteredCount = masteredCount; }
    }

    public static class PeerQuestionVo implements Serializable {
        private static final long serialVersionUID = 1L;

        private Integer questionId;
        private String stem;
        private Integer difficulty;
        private Integer correctPeerCount;
        private String recommendReason;

        public Integer getQuestionId() { return questionId; }
        public void setQuestionId(Integer questionId) { this.questionId = questionId; }

        public String getStem() { return stem; }
        public void setStem(String stem) { this.stem = stem; }

        public Integer getDifficulty() { return difficulty; }
        public void setDifficulty(Integer difficulty) { this.difficulty = difficulty; }

        public Integer getCorrectPeerCount() { return correctPeerCount; }
        public void setCorrectPeerCount(Integer correctPeerCount) { this.correctPeerCount = correctPeerCount; }

        public String getRecommendReason() { return recommendReason; }
        public void setRecommendReason(String recommendReason) { this.recommendReason = recommendReason; }
    }
}
