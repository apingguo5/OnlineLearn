package com.rabbiter.ol.controller;

import com.rabbiter.ol.common.Result;
import com.rabbiter.ol.dao.StudentGradeDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("study/ai")
@CrossOrigin
public class AiAssistantController {

    @Autowired
    private StudentGradeDao studentGradeDao;

    @Value("${ai.api.url:}")
    private String aiApiUrl;

    @Value("${ai.api.key:}")
    private String aiApiKey;

    @Value("${ai.api.model:gpt-3.5-turbo}")
    private String aiModel;

    @RequestMapping("/analyzeClass")
    public Result analyzeClass(@RequestBody Map<String, Object> params) {
        Integer classId = (Integer) params.get("classId");
        if (classId == null) {
            return Result.failureCode();
        }

        List<Map<String, Object>> allStudents = studentGradeDao.queryClassGrades(classId);
        if (allStudents.isEmpty()) {
            Map<String, Object> empty = new HashMap<>();
            empty.put("strugglingStudents", new ArrayList<>());
            empty.put("totalStudents", 0);
            empty.put("warningCount", 0);
            return Result.success(empty);
        }

        List<Map<String, Object>> strugglingStudents = new ArrayList<>();
        double sum = 0;
        for (Map<String, Object> s : allStudents) {
            Object totalObj = s.get("totalScore");
            double total = totalObj != null ? Double.parseDouble(totalObj.toString()) : 0;
            sum += total;
        }
        double avgScore = sum / allStudents.size();
        double threshold = Math.min(60, avgScore * 0.8);

        for (Map<String, Object> s : allStudents) {
            Object totalObj = s.get("totalScore");
            double total = totalObj != null ? Double.parseDouble(totalObj.toString()) : 0;
            if (total < threshold) {
                Map<String, Object> item = new HashMap<>(s);
                item.put("belowAverage", total < avgScore);
                item.put("totalScore", total);
                strugglingStudents.add(item);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("strugglingStudents", strugglingStudents);
        result.put("totalStudents", allStudents.size());
        result.put("warningCount", strugglingStudents.size());
        result.put("averageScore", Math.round(avgScore * 10) / 10.0);
        result.put("allStudents", allStudents);
        return Result.success(result);
    }

    @RequestMapping("/recommend")
    public Result recommend(@RequestBody Map<String, Object> params) {
        Integer classId = (Integer) params.get("classId");
        if (classId == null) {
            return Result.failureCode();
        }

        List<Map<String, Object>> allStudents = studentGradeDao.queryClassGrades(classId);
        if (allStudents.isEmpty()) {
            return Result.success(new ArrayList<>());
        }

        double sum = 0;
        for (Map<String, Object> s : allStudents) {
            Object totalObj = s.get("totalScore");
            double total = totalObj != null ? Double.parseDouble(totalObj.toString()) : 0;
            sum += total;
        }
        double avgScore = sum / allStudents.size();
        double threshold = Math.min(60, avgScore * 0.8);

        List<Map<String, Object>> recommendations = new ArrayList<>();
        for (Map<String, Object> s : allStudents) {
            Object totalObj = s.get("totalScore");
            double total = totalObj != null ? Double.parseDouble(totalObj.toString()) : 0;
            if (total < threshold) {
                Map<String, Object> rec = buildStrugglingStudentRecommendation(s, total);
                recommendations.add(rec);
            }
        }

        return Result.success(recommendations);
    }

    @RequestMapping("/aiRecommend")
    public Result aiRecommend(@RequestBody Map<String, Object> params) {
        Integer classId = (Integer) params.get("classId");
        if (classId == null) {
            return Result.failureCode();
        }

        List<Map<String, Object>> allStudents = studentGradeDao.queryClassGrades(classId);
        if (allStudents.isEmpty()) {
            return Result.success(new ArrayList<>());
        }

        double sum = 0;
        for (Map<String, Object> s : allStudents) {
            Object totalObj = s.get("totalScore");
            double total = totalObj != null ? Double.parseDouble(totalObj.toString()) : 0;
            sum += total;
        }
        double avgScore = sum / allStudents.size();
        double threshold = Math.min(60, avgScore * 0.8);

        List<Map<String, Object>> results = new ArrayList<>();
        int maxCalls = allStudents.size(); // safety limit
        int callCount = 0;

        for (Map<String, Object> s : allStudents) {
            if (callCount >= maxCalls) break;
            Object totalObj = s.get("totalScore");
            double total = totalObj != null ? Double.parseDouble(totalObj.toString()) : 0;
            if (total < threshold) {
                String aiAdvice = callAiApi(s, avgScore);
                Map<String, Object> item = new HashMap<>();
                item.put("studentName", s.get("studentName"));
                item.put("studentNo", s.get("studentNo"));
                item.put("totalScore", total);
                item.put("videoScore", s.get("videoScore"));
                item.put("homeworkScore", s.get("homeworkScore"));
                item.put("examScore", s.get("examScore"));
                item.put("discussionScore", s.get("discussionScore"));
                item.put("aiAdvice", aiAdvice);
                results.add(item);
                callCount++;
            }
        }

        return Result.success(results);
    }

    private Map<String, Object> buildStrugglingStudentRecommendation(Map<String, Object> student, double totalScore) {
        Map<String, Object> rec = new HashMap<>();
        rec.put("studentName", student.get("studentName"));
        rec.put("studentNo", student.get("studentNo"));
        rec.put("totalScore", totalScore);
        rec.put("videoScore", student.get("videoScore"));
        rec.put("homeworkScore", student.get("homeworkScore"));
        rec.put("examScore", student.get("examScore"));
        rec.put("discussionScore", student.get("discussionScore"));

        List<Map<String, String>> suggestions = new ArrayList<>();
        double video = parseDouble(student.get("videoScore"));
        double homework = parseDouble(student.get("homeworkScore"));
        double exam = parseDouble(student.get("examScore"));
        double discussion = parseDouble(student.get("discussionScore"));

        if (video < 60) {
            Map<String, String> sug = new HashMap<>();
            sug.put("area", "视频学习");
            sug.put("advice", "视频观看得分偏低，建议回顾课程视频，重点关注基础概念和核心API用法。每天至少观看30分钟教学视频。");
            suggestions.add(sug);
        }
        if (homework < 60) {
            Map<String, String> sug = new HashMap<>();
            sug.put("area", "作业练习");
            sug.put("advice", "作业得分较低，说明动手实践能力需要加强。建议重新完成课后作业，并参考习题解析加深理解。");
            suggestions.add(sug);
        }
        if (exam < 60) {
            Map<String, String> sug = new HashMap<>();
            sug.put("area", "考试测评");
            sug.put("advice", "考试成绩不理想，可能存在知识盲区。建议梳理错题集，针对性复习薄弱章节，多做模拟题熟练题型。");
            suggestions.add(sug);
        }
        if (discussion < 60) {
            Map<String, String> sug = new HashMap<>();
            sug.put("area", "课堂讨论");
            sug.put("advice", "讨论参与度不足，建议积极参与课堂互动和问答社区，通过与同学老师的交流加深理解。");
            suggestions.add(sug);
        }
        if (suggestions.isEmpty()) {
            Map<String, String> sug = new HashMap<>();
            sug.put("area", "综合提升");
            sug.put("advice", "整体成绩偏低，建议制定系统学习计划，从基础知识开始查漏补缺，定期自测评估进步。");
            suggestions.add(sug);
        }

        rec.put("suggestions", suggestions);
        return rec;
    }

    private String callAiApi(Map<String, Object> student, double avgScore) {
        if (aiApiUrl == null || aiApiUrl.isEmpty() || aiApiKey == null || aiApiKey.isEmpty()) {
            return buildFallbackAdvice(student);
        }

        String studentName = (String) student.getOrDefault("studentName", "未知");
        double video = parseDouble(student.get("videoScore"));
        double homework = parseDouble(student.get("homeworkScore"));
        double exam = parseDouble(student.get("examScore"));
        double discussion = parseDouble(student.get("discussionScore"));
        double total = parseDouble(student.get("totalScore"));

        String prompt = String.format(
            "你是一位智能助学导师。请根据以下学生成绩给出学习建议（用中文，200字以内）：\n" +
            "学生：%s\n视频观看：%d分 | 作业成绩：%d分 | 考试成绩：%d分 | 讨论成绩：%d分 | 综合得分：%d分\n" +
            "班级平均分：%.0f分\n" +
            "请指出该生最薄弱的1-2个方向，并给出具体可操作的学习建议。",
            studentName, (int) video, (int) homework, (int) exam, (int) discussion, (int) total, avgScore
        );

        try {
            URL url = new URL(aiApiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + aiApiKey);
            conn.setDoOutput(true);
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(30000);

            String body = String.format(
                "{\"model\":\"%s\",\"messages\":[{\"role\":\"user\",\"content\":\"%s\"}],\"max_tokens\":400,\"temperature\":0.7}",
                aiModel, escapeJson(prompt)
            );

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            int code = conn.getResponseCode();
            if (code == 200) {
                java.util.Scanner s = new java.util.Scanner(conn.getInputStream(), "UTF-8").useDelimiter("\\A");
                String response = s.hasNext() ? s.next() : "";
                s.close();
                return extractContentFromOpenAiResponse(response);
            } else {
                java.util.Scanner s = new java.util.Scanner(conn.getErrorStream(), "UTF-8").useDelimiter("\\A");
                String err = s.hasNext() ? s.next() : "";
                s.close();
                System.err.println("AI API error: " + err);
                return buildFallbackAdvice(student);
            }
        } catch (Exception e) {
            System.err.println("AI API call failed: " + e.getMessage());
            return buildFallbackAdvice(student);
        }
    }

    private String buildFallbackAdvice(Map<String, Object> student) {
        double video = parseDouble(student.get("videoScore"));
        double homework = parseDouble(student.get("homeworkScore"));
        double exam = parseDouble(student.get("examScore"));
        double discussion = parseDouble(student.get("discussionScore"));

        StringBuilder sb = new StringBuilder();
        sb.append("【智能分析学习建议】（AI未配置，使用规则引擎）\n\n");

        if (video < 60) sb.append("📺 视频学习薄弱：建议每天安排30分钟观看教学视频，从基础章节重新梳理。\n");
        if (homework < 60) sb.append("📝 作业需要加强：建议重新完成课程配套练习，重点攻克易错题型。\n");
        if (exam < 60) sb.append("📋 考试表现不佳：建议整理错题本，针对薄弱知识点做专项训练。\n");
        if (discussion < 60) sb.append("💬 讨论参与不足：多参与课堂问答和社区讨论，通过交流加深理解。\n");

        sb.append("\n推荐学习路径：基础知识回顾 → 专项练习 → 综合测试 → 查漏补缺");
        return sb.toString();
    }

    private String extractContentFromOpenAiResponse(String json) {
        try {
            int idx = json.indexOf("\"content\":\"");
            if (idx < 0) return json;
            int start = idx + 11;
            int end = start;
            while (end < json.length()) {
                if (json.charAt(end) == '"' && (end == start || json.charAt(end - 1) != '\\')) {
                    break;
                }
                end++;
            }
            return json.substring(start, end).replace("\\n", "\n").replace("\\\"", "\"");
        } catch (Exception e) {
            return json;
        }
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    private double parseDouble(Object v) {
        if (v == null) return 0;
        try { return Double.parseDouble(v.toString()); } catch (NumberFormatException e) { return 0; }
    }
}
