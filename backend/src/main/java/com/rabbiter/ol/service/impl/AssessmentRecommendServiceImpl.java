package com.rabbiter.ol.service.impl;

import com.rabbiter.ol.service.AssessmentRecommendDataRepository;
import com.rabbiter.ol.service.AssessmentRecommendService;
import com.rabbiter.ol.service.AssessmentSimilarityUtil;
import com.rabbiter.ol.service.SimilarityCalculator;
import com.rabbiter.ol.vo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service("assessmentRecommendService")
public class AssessmentRecommendServiceImpl implements AssessmentRecommendService {

    private static final Logger log = LoggerFactory.getLogger(AssessmentRecommendServiceImpl.class);

    private static final int MAX_SIMILAR_PEERS = 5;
    private static final double MASTERY_THRESHOLD = 0.6;

    @Autowired
    private AssessmentRecommendDataRepository dataRepo;

    @Override
    public List<WrongQuestionRecommendVo> recommendWrongQuestionReview(Integer userId, Integer courseId, int topN) {
        List<WrongQuestionRecommendVo> result = new ArrayList<>();

        List<Map<String, Object>> wrongAnswers = dataRepo.getStudentWrongAnswers(userId, courseId);
        if (wrongAnswers.isEmpty()) {
            return result;
        }

        List<Map<String, Object>> allQuestions = dataRepo.getAllQuestionsByCourse(courseId);
        if (allQuestions.isEmpty()) {
            return result;
        }

        Set<Integer> doneIds = dataRepo.getDoneQuestionIds(userId).stream()
                .map(m -> ((Number) m.get("question_id")).intValue())
                .collect(Collectors.toSet());

        for (Map<String, Object> wrong : wrongAnswers) {
            Integer wrongQid = ((Number) wrong.get("questionId")).intValue();
            String wrongStem = wrong.get("stem") != null ? wrong.get("stem").toString() : "";
            Integer wrongType = wrong.get("questionType") != null ? ((Number) wrong.get("questionType")).intValue() : 1;
            Integer wrongChapter = wrong.get("chapterId") != null ? ((Number) wrong.get("chapterId")).intValue() : null;
            Integer wrongDiff = wrong.get("difficulty") != null ? ((Number) wrong.get("difficulty")).intValue() : 1;
            Date submitTime = (Date) wrong.get("submitTime");
            double forgettingWeight = AssessmentSimilarityUtil.calculateForgettingWeight(submitTime);

            for (Map<String, Object> candidate : allQuestions) {
                Integer cid = ((Number) candidate.get("id")).intValue();
                if (cid.equals(wrongQid) || doneIds.contains(cid)) continue;

                Integer cType = candidate.get("questionType") != null ? ((Number) candidate.get("questionType")).intValue() : 1;
                String cStem = candidate.get("stem") != null ? candidate.get("stem").toString() : "";
                Integer cChapter = candidate.get("chapterId") != null ? ((Number) candidate.get("chapterId")).intValue() : null;
                Integer cDiff = candidate.get("difficulty") != null ? ((Number) candidate.get("difficulty")).intValue() : 1;

                double typeMatch = wrongType.equals(cType) ? 1.0 : 0.3;
                double textSim = AssessmentSimilarityUtil.textSimilarity(wrongStem, cStem);
                double chapterMatch = (wrongChapter != null && wrongChapter.equals(cChapter)) ? 1.0 : 0.2;
                double diffMatch = 1.0 - Math.abs(wrongDiff - cDiff) / 3.0;

                double similarity = 0.4 * typeMatch + 0.3 * textSim + 0.2 * chapterMatch + 0.1 * diffMatch;
                if (similarity < 0.3) continue;

                WrongQuestionRecommendVo vo = new WrongQuestionRecommendVo();
                vo.setQuestionId(cid);
                vo.setSourceQuestionId(wrongQid);
                vo.setSourceQuestionStem(wrongStem.length() > 50 ? wrongStem.substring(0, 50) + "..." : wrongStem);
                vo.setStem(cStem);
                vo.setOptions(candidate.get("options") != null ? candidate.get("options").toString() : null);
                vo.setAnswer(candidate.get("answer") != null ? candidate.get("answer").toString() : null);
                vo.setAnalysis(candidate.get("analysis") != null ? candidate.get("analysis").toString() : null);
                vo.setQuestionType(cType);
                vo.setDifficulty(cDiff);
                vo.setSimilarityScore(Math.round(similarity * 100.0) / 100.0);
                vo.setForgettingWeight(Math.round(forgettingWeight * 100.0) / 100.0);
                vo.setRecommendReason("与错题《" + vo.getSourceQuestionStem() + "》相似，建议针对性巩固");
                vo.setCourseId(courseId);
                vo.setChapterId(cChapter);
                vo.setChapterName(candidate.get("chapterName") != null ? candidate.get("chapterName").toString() : null);
                result.add(vo);
            }
        }

        return result.stream()
                .sorted(Comparator.comparingDouble((WrongQuestionRecommendVo v) ->
                        v.getSimilarityScore() * v.getForgettingWeight()).reversed())
                .limit(topN)
                .collect(Collectors.toList());
    }

    @Override
    public List<ExamPaperRecommendVo> recommendExamPapers(Integer userId, Integer courseId, Integer classId, int topN) {
        List<ExamPaperRecommendVo> result = new ArrayList<>();

        List<Map<String, Object>> recentScores = dataRepo.getStudentRecentScores(userId);
        double ability = AssessmentSimilarityUtil.calculateStudentAbility(recentScores);

        List<Map<String, Object>> papers = dataRepo.getPublishedPapers(courseId, classId);
        Set<Integer> donePaperIds = dataRepo.getDonePaperIds(userId).stream()
                .map(m -> ((Number) m.get("paper_id")).intValue())
                .collect(Collectors.toSet());

        for (Map<String, Object> paper : papers) {
            Integer paperId = ((Number) paper.get("id")).intValue();
            if (donePaperIds.contains(paperId)) continue;

            Double avgDifficulty = dataRepo.getPaperAvgDifficulty(paperId);
            double matchScore = AssessmentSimilarityUtil.calculateMatchScore(avgDifficulty, ability);

            ExamPaperRecommendVo vo = new ExamPaperRecommendVo();
            vo.setPaperId(paperId);
            vo.setTitle(paper.get("title") != null ? paper.get("title").toString() : "");
            vo.setDescription(paper.get("description") != null ? paper.get("description").toString() : "");
            vo.setPaperType(paper.get("paperType") != null ? ((Number) paper.get("paperType")).intValue() : 0);
            vo.setTotalScore(paper.get("totalScore") != null ? ((Number) paper.get("totalScore")).doubleValue() : 100.0);
            vo.setDuration(paper.get("duration") != null ? ((Number) paper.get("duration")).intValue() : null);
            vo.setQuestionCount(paper.get("questionCount") != null ? ((Number) paper.get("questionCount")).intValue() : 0);
            vo.setAvgDifficulty(Math.round(avgDifficulty * 10.0) / 10.0);
            vo.setStudentAbility(Math.round(ability * 100.0) / 100.0);
            vo.setMatchScore(Math.round(matchScore * 100.0) / 100.0);
            vo.setCourseId(paper.get("courseId") != null ? ((Number) paper.get("courseId")).intValue() : null);
            vo.setCourseName(paper.get("courseName") != null ? paper.get("courseName").toString() : "");

            int abilityLevel = AssessmentSimilarityUtil.abilityToLevel(ability);
            if (matchScore > 0.8) {
                vo.setMatchLevel("perfect");
                vo.setRecommendReason("难度与您当前水平完美匹配，推荐优先完成");
            } else if (avgDifficulty > abilityLevel) {
                vo.setMatchLevel("challenging");
                vo.setRecommendReason("难度略高于您当前水平，适合作为提升挑战");
            } else {
                vo.setMatchLevel("easy");
                vo.setRecommendReason("难度低于您当前水平，可用于巩固基础");
            }
            result.add(vo);
        }

        return result.stream()
                .sorted((a, b) -> {
                    int levelCompare = compareMatchLevel(a.getMatchLevel(), b.getMatchLevel());
                    if (levelCompare != 0) return levelCompare;
                    return Double.compare(b.getMatchScore() != null ? b.getMatchScore() : 0,
                            a.getMatchScore() != null ? a.getMatchScore() : 0);
                })
                .limit(topN)
                .collect(Collectors.toList());
    }

    private int compareMatchLevel(String a, String b) {
        Map<String, Integer> order = new HashMap<>();
        order.put("perfect", 0);
        order.put("challenging", 1);
        order.put("easy", 2);
        return Integer.compare(order.getOrDefault(a, 9), order.getOrDefault(b, 9));
    }

    @Override
    public List<WeakPointDiagnosisVo> diagnoseWeakPoints(Integer userId, Integer courseId, int topK) {
        List<WeakPointDiagnosisVo> result = new ArrayList<>();

        List<Map<String, Object>> chapterStats = dataRepo.getStudentChapterStats(userId, courseId);
        String courseName = dataRepo.getCourseName(courseId);

        for (Map<String, Object> stat : chapterStats) {
            Integer chapterId = ((Number) stat.get("chapterId")).intValue();
            int total = stat.get("totalCount") != null ? ((Number) stat.get("totalCount")).intValue() : 0;
            int correct = stat.get("correctCount") != null ? ((Number) stat.get("correctCount")).intValue() : 0;

            if (total == 0) continue;

            double masteryRate = (double) correct / total;
            double weaknessScore = (1.0 - masteryRate) * total;

            String level;
            if (masteryRate >= MASTERY_THRESHOLD) {
                level = "mastered";
            } else if (masteryRate >= 0.3) {
                level = "weak";
            } else {
                level = "very_weak";
            }

            WeakPointDiagnosisVo vo = new WeakPointDiagnosisVo();
            vo.setChapterId(chapterId);
            vo.setChapterName(stat.get("chapterName") != null ? stat.get("chapterName").toString() : "");
            vo.setCourseId(courseId);
            vo.setCourseName(courseName);
            vo.setMasteryRate(Math.round(masteryRate * 1000.0) / 10.0);
            vo.setTotalQuestions(total);
            vo.setWrongCount(total - correct);
            vo.setWeaknessScore(Math.round(weaknessScore * 100.0) / 100.0);
            vo.setLevel(level);

            if (level.equals("weak") || level.equals("very_weak")) {
                List<Map<String, Object>> questions = dataRepo.getChapterQuestions(chapterId);
                List<WeakPointDiagnosisVo.WeakPointResourceVo> questionVos = new ArrayList<>();
                for (Map<String, Object> q : questions) {
                    WeakPointDiagnosisVo.WeakPointResourceVo rv = new WeakPointDiagnosisVo.WeakPointResourceVo();
                    rv.setResourceId(((Number) q.get("id")).intValue());
                    rv.setTitle(q.get("stem") != null ? q.get("stem").toString() : "");
                    rv.setDifficulty(q.get("difficulty") != null ? ((Number) q.get("difficulty")).intValue() : 1);
                    rv.setResourceType("question");
                    questionVos.add(rv);
                }
                vo.setRecommendedQuestions(questionVos);

                List<Map<String, Object>> videos = dataRepo.getChapterVideos(chapterId);
                List<WeakPointDiagnosisVo.WeakPointResourceVo> videoVos = new ArrayList<>();
                for (Map<String, Object> v : videos) {
                    WeakPointDiagnosisVo.WeakPointResourceVo rv = new WeakPointDiagnosisVo.WeakPointResourceVo();
                    rv.setResourceId(((Number) v.get("id")).intValue());
                    rv.setTitle(v.get("title") != null ? v.get("title").toString() : "");
                    rv.setDescription(v.get("description") != null ? v.get("description").toString() : "");
                    rv.setResourceType("video");
                    videoVos.add(rv);
                }
                vo.setRecommendedVideos(videoVos);
            }

            result.add(vo);
        }

        return result.stream()
                .filter(v -> !"mastered".equals(v.getLevel()))
                .sorted(Comparator.comparingDouble(WeakPointDiagnosisVo::getWeaknessScore).reversed())
                .limit(topK)
                .collect(Collectors.toList());
    }

    @Override
    public PeerCompareVo compareWithPeers(Integer userId, Integer classId, Integer courseId) {
        PeerCompareVo vo = new PeerCompareVo();
        vo.setUserId(userId);

        List<Map<String, Object>> students = dataRepo.getClassStudentsWithFeatures(classId);
        if (students.isEmpty()) return vo;

        double myTotalScore = 0;
        double sumScore = 0;
        Map<Integer, Double> otherFeatureMap = new HashMap<>();
        Map<Integer, Double> userFeature = new HashMap<>();
        boolean foundSelf = false;

        for (Map<String, Object> s : students) {
            double score = s.get("totalScore") != null ? ((Number) s.get("totalScore")).doubleValue() : 0;
            sumScore += score;
            if (((Number) s.get("userId")).intValue() == userId) {
                myTotalScore = score;
                userFeature.put(1, toDouble(s.get("videoScore")));
                userFeature.put(2, toDouble(s.get("homeworkScore")));
                userFeature.put(3, toDouble(s.get("examScore")));
            } else {
                Map<Integer, Double> feat = new HashMap<>();
                feat.put(1, toDouble(s.get("videoScore")));
                feat.put(2, toDouble(s.get("homeworkScore")));
                feat.put(3, toDouble(s.get("examScore")));
                double sim = SimilarityCalculator.cosine(userFeature, feat);
                if (sim > 0) {
                    otherFeatureMap.put(((Number) s.get("userId")).intValue(), sim);
                }
            }
        }

        double avgScore = sumScore / students.size();
        vo.setAverageScore(Math.round(myTotalScore * 10.0) / 10.0);
        vo.setClassAverageScore(Math.round(avgScore * 10.0) / 10.0);
        vo.setTotalStudents(students.size());

        int rank = dataRepo.getStudentRankInClass(userId, classId);
        vo.setRankPosition(rank);
        vo.setRankPercentile(Math.round((1.0 - (double) rank / students.size()) * 1000.0) / 10.0);

        List<PeerCompareVo.PeerStudentVo> peers = otherFeatureMap.entrySet().stream()
                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                .limit(MAX_SIMILAR_PEERS)
                .map(e -> {
                    PeerCompareVo.PeerStudentVo p = new PeerCompareVo.PeerStudentVo();
                    p.setUserId(e.getKey());
                    p.setSimilarityScore(Math.round(e.getValue() * 100.0) / 100.0);
                    for (Map<String, Object> s : students) {
                        if (((Number) s.get("userId")).intValue() == e.getKey()) {
                            p.setUserName(s.get("userName") != null ? s.get("userName").toString() : "");
                            p.setAverageScore(s.get("totalScore") != null ? ((Number) s.get("totalScore")).doubleValue() : 0);
                            break;
                        }
                    }
                    return p;
                }).collect(Collectors.toList());
        vo.setSimilarPeers(peers);

        List<Map<String, Object>> peerMastered = dataRepo.getPeerQuestionMastery(userId, classId, courseId);
        List<PeerCompareVo.PeerQuestionVo> masteredQ = new ArrayList<>();
        for (Map<String, Object> m : peerMastered) {
            PeerCompareVo.PeerQuestionVo pq = new PeerCompareVo.PeerQuestionVo();
            pq.setQuestionId(((Number) m.get("questionId")).intValue());
            pq.setStem(m.get("stem") != null ? m.get("stem").toString() : "");
            pq.setDifficulty(m.get("difficulty") != null ? ((Number) m.get("difficulty")).intValue() : 1);
            pq.setCorrectPeerCount(m.get("correctPeerCount") != null ? ((Number) m.get("correctPeerCount")).intValue() : 0);
            pq.setRecommendReason("有" + pq.getCorrectPeerCount() + "位同学已经掌握");
            masteredQ.add(pq);
        }
        vo.setPeerMasteredQuestions(masteredQ);

        List<Map<String, Object>> peerPending = dataRepo.getPeerPendingQuestions(userId, classId, courseId);
        List<PeerCompareVo.PeerQuestionVo> pendingQ = new ArrayList<>();
        for (Map<String, Object> m : peerPending) {
            PeerCompareVo.PeerQuestionVo pq = new PeerCompareVo.PeerQuestionVo();
            pq.setQuestionId(((Number) m.get("questionId")).intValue());
            pq.setStem(m.get("stem") != null ? m.get("stem").toString() : "");
            pq.setDifficulty(m.get("difficulty") != null ? ((Number) m.get("difficulty")).intValue() : 1);
            pq.setCorrectPeerCount(m.get("correctPeerCount") != null ? ((Number) m.get("correctPeerCount")).intValue() : 0);
            pq.setRecommendReason(pq.getCorrectPeerCount() + "位同学已通过，推荐你也试试");
            pendingQ.add(pq);
        }
        vo.setPeerPendingQuestions(pendingQ);

        return vo;
    }

    @Override
    public GradeAlertVo generateAlert(Integer userId, Integer classId) {
        GradeAlertVo vo = new GradeAlertVo();
        vo.setUserId(userId);
        vo.setClassId(classId);

        List<Map<String, Object>> gradeHistory = dataRepo.getStudentGradesForTrend(userId, classId);
        double classAvg = dataRepo.getClassAverageScore(classId);

        if (gradeHistory.isEmpty()) {
            vo.setAlertLevel("normal");
            vo.setAlertLabel("暂无成绩数据");
            vo.setCurrentScore(0.0);
            vo.setAverageScore(classAvg);
            vo.setTrend("unknown");
            return vo;
        }

        Map<String, Object> latest = gradeHistory.get(gradeHistory.size() - 1);
        double currentScore = latest.get("totalScore") != null ? ((Number) latest.get("totalScore")).doubleValue() : 0;
        vo.setCurrentScore(Math.round(currentScore * 10.0) / 10.0);
        vo.setAverageScore(Math.round(classAvg * 10.0) / 10.0);

        List<Double> timePoints = new ArrayList<>();
        List<Double> scoreValues = new ArrayList<>();
        for (int i = 0; i < gradeHistory.size(); i++) {
            timePoints.add((double) i);
            double s = gradeHistory.get(i).get("totalScore") != null ? ((Number) gradeHistory.get(i).get("totalScore")).doubleValue() : 0;
            scoreValues.add(s);
        }

        double[] reg = AssessmentSimilarityUtil.linearRegression(timePoints, scoreValues);
        double slope = reg[0];

        String trend;
        if (slope > 1.0) {
            trend = "rising";
        } else if (slope < -1.0) {
            trend = "declining";
        } else {
            trend = "stable";
        }
        vo.setTrend(trend);
        vo.setTrendSlope(Math.round(slope * 100.0) / 100.0);

        double cv = AssessmentSimilarityUtil.coefficientOfVariation(scoreValues);
        vo.setVolatility(Math.round(cv * 100.0) / 100.0);

        String alertLevel;
        String alertLabel;
        if (currentScore < 60 && "declining".equals(trend)) {
            alertLevel = "critical";
            alertLabel = "成绩危急，持续下滑";
        } else if (currentScore < classAvg * 0.6 || ("declining".equals(trend) && currentScore < classAvg * 0.8)) {
            alertLevel = "warning";
            alertLabel = "成绩预警，需要关注";
        } else if ("declining".equals(trend) || currentScore < classAvg * 0.8) {
            alertLevel = "attention";
            alertLabel = "成绩偏低或下滑，建议加强";
        } else {
            alertLevel = "normal";
            alertLabel = "表现正常，继续保持";
        }
        vo.setAlertLevel(alertLevel);
        vo.setAlertLabel(alertLabel);

        List<GradeAlertVo.AlertActionVo> actions = buildAlertActions(alertLevel, userId, classId);
        vo.setRecommendedActions(actions);

        return vo;
    }

    private List<GradeAlertVo.AlertActionVo> buildAlertActions(String alertLevel, Integer userId, Integer classId) {
        List<GradeAlertVo.AlertActionVo> actions = new ArrayList<>();

        if ("critical".equals(alertLevel) || "warning".equals(alertLevel)) {
            GradeAlertVo.AlertActionVo a1 = new GradeAlertVo.AlertActionVo();
            a1.setActionType("wrong_question_review");
            a1.setTitle("错题复习冲刺");
            a1.setDescription("系统已为你筛选了最需要复习的错题，每天刷5道，查漏补缺");
            a1.setTargetUrl("/study/recommend/wrong-questions?userId=" + userId);
            actions.add(a1);

            GradeAlertVo.AlertActionVo a2 = new GradeAlertVo.AlertActionVo();
            a2.setActionType("basic_exam");
            a2.setTitle("基础巩固测试");
            a2.setDescription("推荐难度较低的试卷，先打好基础再挑战高难度");
            a2.setTargetUrl("/study/recommend/exam-papers?userId=" + userId + "&classId=" + classId);
            actions.add(a2);

            GradeAlertVo.AlertActionVo a3 = new GradeAlertVo.AlertActionVo();
            a3.setActionType("teacher_help");
            a3.setTitle("找老师求助");
            a3.setDescription("你的成绩已触发预警，建议主动联系老师获取一对一指导");
            a3.setTargetUrl("/ask-questions");
            actions.add(a3);
        } else if ("attention".equals(alertLevel)) {
            GradeAlertVo.AlertActionVo a1 = new GradeAlertVo.AlertActionVo();
            a1.setActionType("wrong_question_review");
            a1.setTitle("错题复习");
            a1.setDescription("回顾近期错题，巩固薄弱知识点");
            a1.setTargetUrl("/study/recommend/wrong-questions?userId=" + userId);
            actions.add(a1);

            GradeAlertVo.AlertActionVo a2 = new GradeAlertVo.AlertActionVo();
            a2.setActionType("weak_point_diagnosis");
            a2.setTitle("薄弱点诊断");
            a2.setDescription("查看你的知识点掌握雷达图，针对性提升");
            a2.setTargetUrl("/study/recommend/weak-points?userId=" + userId);
            actions.add(a2);
        } else {
            GradeAlertVo.AlertActionVo a1 = new GradeAlertVo.AlertActionVo();
            a1.setActionType("challenge");
            a1.setTitle("挑战更高难度");
            a1.setDescription("当前表现良好，推荐挑战高难度试卷提升水平");
            a1.setTargetUrl("/study/recommend/exam-papers?userId=" + userId + "&classId=" + classId);
            actions.add(a1);
        }
        return actions;
    }

    private Double toDouble(Object v) {
        if (v == null) return 0.0;
        try { return Double.parseDouble(v.toString()); } catch (NumberFormatException e) { return 0.0; }
    }
}
