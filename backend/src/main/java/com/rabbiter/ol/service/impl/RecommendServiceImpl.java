package com.rabbiter.ol.service.impl;

import com.rabbiter.ol.service.RecommendDataRepository;
import com.rabbiter.ol.service.RecommendService;
import com.rabbiter.ol.service.SimilarityCalculator;
import com.rabbiter.ol.vo.CourseRecommendVo;
import com.rabbiter.ol.vo.ExerciseRecommendVo;
import com.rabbiter.ol.vo.LearningPathVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service("recommendService")
public class RecommendServiceImpl implements RecommendService {

    private static final Logger log = LoggerFactory.getLogger(RecommendServiceImpl.class);

    private static final double USER_CF_WEIGHT = 0.5;
    private static final double ITEM_CF_WEIGHT = 0.5;
    private static final int SIMILAR_NEIGHBOR_COUNT = 10;
    private static final double MASTERY_THRESHOLD = 0.6;

    @Autowired
    private RecommendDataRepository dataRepo;

    // ======================== 课程推荐 ========================

    @Override
    public List<CourseRecommendVo> recommendCourses(Integer userId, int topN) {
        List<CourseRecommendVo> userBased = recommendCoursesByUserCF(userId, topN);
        List<CourseRecommendVo> itemBased = recommendCoursesByItemCF(userId, topN);

        Map<Integer, CourseRecommendVo> merged = new LinkedHashMap<>();
        for (CourseRecommendVo vo : userBased) {
            vo.setRecommendType("hybrid");
            merged.put(vo.getCourseId(), vo);
        }
        for (CourseRecommendVo vo : itemBased) {
            if (merged.containsKey(vo.getCourseId())) {
                CourseRecommendVo existing = merged.get(vo.getCourseId());
                existing.setRecommendScore(
                        USER_CF_WEIGHT * existing.getRecommendScore() + ITEM_CF_WEIGHT * vo.getRecommendScore()
                );
                existing.setRecommendReason("协同过滤混合推荐");
            } else {
                vo.setRecommendType("hybrid");
                vo.setRecommendScore(ITEM_CF_WEIGHT * vo.getRecommendScore());
                merged.put(vo.getCourseId(), vo);
            }
        }

        return merged.values().stream()
                .sorted(Comparator.comparingDouble(CourseRecommendVo::getRecommendScore).reversed())
                .limit(topN)
                .collect(Collectors.toList());
    }

    @Override
    public List<CourseRecommendVo> recommendCoursesByUserCF(Integer userId, int topN) {
        Map<Integer, Map<Integer, Double>> ratingMatrix = buildUserRatingMatrix();
        Map<Integer, Double> targetRatings = ratingMatrix.get(userId);
        if (targetRatings == null || targetRatings.isEmpty()) {
            return fallbackPopularCourses(userId, topN, "User-CF");
        }

        // 计算所有用户与目标用户的相似度
        List<Neighbor> neighbors = new ArrayList<>();
        for (Map.Entry<Integer, Map<Integer, Double>> entry : ratingMatrix.entrySet()) {
            if (entry.getKey().equals(userId)) continue;
            double sim = SimilarityCalculator.cosine(targetRatings, entry.getValue());
            if (sim > 0) {
                neighbors.add(new Neighbor(entry.getKey(), sim, entry.getValue()));
            }
        }
        neighbors.sort(Comparator.comparingDouble(Neighbor::getSim).reversed());
        List<Neighbor> topNeighbors = neighbors.stream()
                .limit(SIMILAR_NEIGHBOR_COUNT)
                .collect(Collectors.toList());

        // 预测目标用户对未学课程的评分
        Set<Integer> learned = targetRatings.keySet();
        Map<Integer, Double> predictions = new HashMap<>();
        for (Neighbor nb : topNeighbors) {
            for (Map.Entry<Integer, Double> item : nb.ratings.entrySet()) {
                if (!learned.contains(item.getKey())) {
                    predictions.merge(item.getKey(), nb.sim * item.getValue(), Double::sum);
                }
            }
        }

        // 归一化
        Map<Integer, Double> simSum = new HashMap<>();
        for (Neighbor nb : topNeighbors) {
            for (Map.Entry<Integer, Double> item : nb.ratings.entrySet()) {
                if (!learned.contains(item.getKey())) {
                    simSum.merge(item.getKey(), Math.abs(nb.sim), Double::sum);
                }
            }
        }

        List<CourseRecommendVo> result = new ArrayList<>();
        for (Map.Entry<Integer, Double> pred : predictions.entrySet()) {
            double norm = simSum.getOrDefault(pred.getKey(), 1.0);
            double score = norm > 0 ? pred.getValue() / norm : 0;
            result.add(buildCourseVo(pred.getKey(), score, "User-Based CF", "与您兴趣相似的学生也在学习"));
        }

        return result.stream()
                .sorted(Comparator.comparingDouble(CourseRecommendVo::getRecommendScore).reversed())
                .limit(topN)
                .collect(Collectors.toList());
    }

    @Override
    public List<CourseRecommendVo> recommendCoursesByItemCF(Integer userId, int topN) {
        Map<Integer, Map<Integer, Double>> ratingMatrix = buildUserRatingMatrix();
        Map<Integer, Double> targetRatings = ratingMatrix.get(userId);
        if (targetRatings == null || targetRatings.isEmpty()) {
            return fallbackPopularCourses(userId, topN, "Item-CF");
        }

        // 转置为 item -> user -> rating
        Map<Integer, Map<Integer, Double>> itemMatrix = SimilarityCalculator.transpose(ratingMatrix);

        // 计算 item 之间相似度，仅计算与已学课程相关的
        Set<Integer> learned = targetRatings.keySet();
        Map<Integer, Map<Integer, Double>> itemSimCache = new HashMap<>();
        for (Integer learnedItem : learned) {
            Map<Integer, Double> learnedVec = itemMatrix.get(learnedItem);
            if (learnedVec == null) continue;
            for (Map.Entry<Integer, Map<Integer, Double>> entry : itemMatrix.entrySet()) {
                if (learned.contains(entry.getKey())) continue;
                double sim = SimilarityCalculator.cosine(learnedVec, entry.getValue());
                if (sim > 0) {
                    itemSimCache.computeIfAbsent(learnedItem, k -> new HashMap<>())
                            .put(entry.getKey(), sim);
                }
            }
        }

        // 预测目标用户对未学课程的评分
        Map<Integer, Double> predictions = new HashMap<>();
        Map<Integer, Double> simSums = new HashMap<>();
        for (Integer learnedItem : learned) {
            Map<Integer, Double> sims = itemSimCache.get(learnedItem);
            if (sims == null) continue;
            double userRating = targetRatings.get(learnedItem);
            for (Map.Entry<Integer, Double> simEntry : sims.entrySet()) {
                Integer candidate = simEntry.getKey();
                double sim = simEntry.getValue();
                predictions.merge(candidate, sim * userRating, Double::sum);
                simSums.merge(candidate, sim, Double::sum);
            }
        }

        List<CourseRecommendVo> result = new ArrayList<>();
        for (Map.Entry<Integer, Double> pred : predictions.entrySet()) {
            double norm = simSums.getOrDefault(pred.getKey(), 1.0);
            double score = norm > 0 ? pred.getValue() / norm : 0;
            result.add(buildCourseVo(pred.getKey(), score, "Item-Based CF", "与您已学课程内容相关"));
        }

        return result.stream()
                .sorted(Comparator.comparingDouble(CourseRecommendVo::getRecommendScore).reversed())
                .limit(topN)
                .collect(Collectors.toList());
    }

    // ======================== 练习推荐 ========================

    @Override
    public List<ExerciseRecommendVo> recommendExercises(Integer userId, Integer classId, int topN) {
        List<ExerciseRecommendVo> result = new ArrayList<>();

        List<Integer> doneIds = dataRepo.getDoneExerciseIds(userId);

        Double hwAvg = classId != null ? dataRepo.getHomeworkAvgScore(userId, classId) : 0.0;
        Double exAvg = classId != null ? dataRepo.getExerciseAvgScore(userId, classId) : 0.0;

        List<Map<String, Object>> exercises;
        if (classId != null) {
            exercises = dataRepo.getExercisesByClassId(classId);
        } else {
            List<Integer> courseIds = dataRepo.getLearnedCourseIds(userId);
            exercises = new ArrayList<>();
            for (Integer cid : courseIds) {
                exercises.addAll(dataRepo.getExercisesByClassId(cid));
            }
        }

        double weaknessBase = (100 - hwAvg) * 0.4 + (100 - exAvg) * 0.6;

        for (Map<String, Object> ex : exercises) {
            Integer eid = ((Number) ex.get("id")).intValue();
            if (doneIds.contains(eid)) continue;

            ExerciseRecommendVo vo = new ExerciseRecommendVo();
            vo.setExerciseId(eid);
            vo.setTitle(ex.get("title") != null ? ex.get("title").toString() : "");
            vo.setContent(ex.get("content") != null ? ex.get("content").toString() : "");
            vo.setClassId(ex.get("classId") != null ? ((Number) ex.get("classId")).intValue() : null);

            double wScore = weaknessBase / 100.0;
            vo.setWeaknessScore(wScore);

            if (hwAvg < 60) {
                vo.setRecommendReason("作业成绩偏低，建议加强练习");
            } else if (exAvg < 60) {
                vo.setRecommendReason("练习正确率偏低，推荐针对性训练");
            } else {
                vo.setRecommendReason("巩固提升练习");
            }

            result.add(vo);
        }

        // 加入知识点维度
        if (classId != null) {
            List<Map<String, Object>> kps = dataRepo.getKnowledgePointsByClassId(classId);
            if (!kps.isEmpty()) {
                List<Map<String, Object>> answerStats = dataRepo.getStudentAnswerStats(userId);
                Set<Integer> weakQuestionIds = new HashSet<>();
                for (Map<String, Object> stat : answerStats) {
                    Object rateObj = stat.get("correctRate");
                    double rate = rateObj != null ? ((Number) rateObj).doubleValue() : 1.0;
                    if (rate < MASTERY_THRESHOLD) {
                        weakQuestionIds.add(((Number) stat.get("questionId")).intValue());
                    }
                }
                if (!weakQuestionIds.isEmpty() && !kps.isEmpty()) {
                    Map<String, Object> weakestKp = kps.get(0);
                    for (ExerciseRecommendVo vo : result) {
                        vo.setKnowledgePoint(weakestKp.get("title") != null ? weakestKp.get("title").toString() : "");
                    }
                }
            }
        }

        return result.stream()
                .sorted(Comparator.comparingDouble(ExerciseRecommendVo::getWeaknessScore).reversed())
                .limit(topN)
                .collect(Collectors.toList());
    }

    // ======================== 学习路径推荐 ========================

    @Override
    public LearningPathVo recommendLearningPath(Integer userId, Integer courseId) {
        LearningPathVo vo = new LearningPathVo();
        vo.setStudentId(userId);
        vo.setCourseId(courseId);

        String courseName = dataRepo.getCourseName(courseId);
        vo.setCourseName(courseName);

        List<Map<String, Object>> chapters = dataRepo.getChaptersByCourseId(courseId);
        Map<Integer, Double> masteryMap = dataRepo.getChapterMastery(userId, courseId);

        List<LearningPathVo.PathNode> pathNodes = new ArrayList<>();
        int order = 1;
        double totalMastery = 0.0;
        int chapterCount = chapters.size();

        // 构建章节图
        Map<Integer, List<Map<String, Object>>> childrenMap = new LinkedHashMap<>();
        List<Map<String, Object>> roots = new ArrayList<>();
        for (Map<String, Object> ch : chapters) {
            Integer parentId = ch.get("parentId") != null ? ((Number) ch.get("parentId")).intValue() : 0;
            if (parentId == null || parentId == 0) {
                roots.add(ch);
            } else {
                childrenMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(ch);
            }
        }

        // 拓扑排序遍历章节树
        List<Map<String, Object>> sorted = new ArrayList<>();
        Queue<Map<String, Object>> queue = new LinkedList<>(roots);
        while (!queue.isEmpty()) {
            Map<String, Object> node = queue.poll();
            sorted.add(node);
            Integer nid = ((Number) node.get("id")).intValue();
            List<Map<String, Object>> children = childrenMap.getOrDefault(nid, Collections.emptyList());
            children.sort(Comparator.comparingInt(a ->
                    a.get("sortOrder") != null ? ((Number) a.get("sortOrder")).intValue() : 0));
            queue.addAll(children);
        }

        // 生成路径节点
        for (Map<String, Object> ch : sorted) {
            Integer chId = ((Number) ch.get("id")).intValue();
            Double mastery = masteryMap.getOrDefault(chId, 0.0);
            totalMastery += mastery;

            LearningPathVo.PathNode pNode = new LearningPathVo.PathNode();
            pNode.setChapterId(chId);
            pNode.setChapterName(ch.get("chapterName") != null ? ch.get("chapterName").toString() : "");
            pNode.setChapterType(ch.get("chapterType") != null ? ch.get("chapterType").toString() : "");
            pNode.setParentId(ch.get("parentId") != null ? ((Number) ch.get("parentId")).intValue() : 0);
            pNode.setOrder(order++);
            pNode.setMastery(mastery);

            if (mastery >= MASTERY_THRESHOLD) {
                pNode.setStatus("mastered");
                pNode.setSuggestion("已掌握，可跳过");
            } else if (mastery > 0) {
                pNode.setStatus("learning");
                pNode.setSuggestion("学习中，建议继续巩固");
            } else {
                pNode.setStatus("not_started");
                // 检查前置章节是否已掌握
                Integer parentId = ch.get("parentId") != null ? ((Number) ch.get("parentId")).intValue() : 0;
                if (parentId > 0) {
                    Double parentMastery = masteryMap.getOrDefault(parentId, 0.0);
                    if (parentMastery < MASTERY_THRESHOLD) {
                        pNode.setSuggestion("请先完成前置章节学习");
                    } else {
                        pNode.setSuggestion("前置已完成，建议开始学习");
                    }
                } else {
                    pNode.setSuggestion("推荐立即开始学习");
                }
            }

            pathNodes.add(pNode);
        }

        vo.setMasteryRate(chapterCount > 0 ? totalMastery / chapterCount : 0.0);
        vo.setPath(pathNodes);
        return vo;
    }

    // ======================== 辅助方法 ========================

    private Map<Integer, Map<Integer, Double>> buildUserRatingMatrix() {
        List<Map<String, Object>> ratings = dataRepo.getAllUserRatings();
        Map<Integer, Map<Integer, Double>> matrix = new HashMap<>();
        for (Map<String, Object> row : ratings) {
            Integer uid = ((Number) row.get("userId")).intValue();
            Integer cid = ((Number) row.get("courseId")).intValue();
            Double rating = ((Number) row.get("rating")).doubleValue();
            matrix.computeIfAbsent(uid, k -> new HashMap<>()).put(cid, rating);
        }
        return matrix;
    }

    private List<CourseRecommendVo> fallbackPopularCourses(Integer userId, int topN, String method) {
        List<Integer> learned = dataRepo.getLearnedCourseIds(userId);
        List<Map<String, Object>> all = dataRepo.getAllCourses();

        List<CourseRecommendVo> result = new ArrayList<>();
        for (Map<String, Object> c : all) {
            Integer cid = ((Number) c.get("id")).intValue();
            if (learned.contains(cid)) continue;
            CourseRecommendVo vo = new CourseRecommendVo();
            vo.setCourseId(cid);
            vo.setCourseName(c.get("courseName") != null ? c.get("courseName").toString() : "");
            vo.setDescription(c.get("description") != null ? c.get("description").toString() : "");
            vo.setCoverUrl(c.get("coverUrl") != null ? c.get("coverUrl").toString() : null);
            vo.setRecommendScore(1.0);
            vo.setRecommendReason("热门推荐（数据不足，" + method + " 冷启动回退）");
            vo.setRecommendType("popular");
            result.add(vo);
        }
        return result.stream().limit(topN).collect(Collectors.toList());
    }

    private CourseRecommendVo buildCourseVo(Integer courseId, double score, String type, String reason) {
        CourseRecommendVo vo = new CourseRecommendVo();
        vo.setCourseId(courseId);
        vo.setRecommendScore(Math.round(score * 100.0) / 100.0);
        vo.setRecommendType(type);
        vo.setRecommendReason(reason);

        List<Map<String, Object>> all = dataRepo.getAllCourses();
        for (Map<String, Object> c : all) {
            if (((Number) c.get("id")).intValue() == courseId) {
                vo.setCourseName(c.get("courseName") != null ? c.get("courseName").toString() : "");
                vo.setDescription(c.get("description") != null ? c.get("description").toString() : "");
                vo.setCoverUrl(c.get("coverUrl") != null ? c.get("coverUrl").toString() : null);
                break;
            }
        }
        return vo;
    }

    private static class Neighbor {
        int userId;
        double sim;
        Map<Integer, Double> ratings;

        Neighbor(int userId, double sim, Map<Integer, Double> ratings) {
            this.userId = userId;
            this.sim = sim;
            this.ratings = ratings;
        }

        double getSim() { return sim; }
    }
}
