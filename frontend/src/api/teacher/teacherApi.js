import {post} from "../request";

/**
 * 教师端 API 集合
 * 按模块组织：概览工作台、课程管理、班级与学生管理、作业考试、成绩报表、互动答疑
 */

// ======================== 1. 概览工作台 ========================
/** 获取工作台统计数据（课程数、班级数、学生数、待办事项） */
export function getDashboardStats(data) {
    return post('/study/teacher/dashboard/stats', data)
}

/** 获取待批改作业数 */
export function getPendingHomeworkCount(data) {
    return post('/study/teacher/dashboard/pendingHomeworkCount', data)
}

/** 获取待回复提问数 */
export function getPendingQuestionCount(data) {
    return post('/study/teacher/dashboard/pendingQuestionCount', data)
}

/** 获取学生活跃度趋势 */
export function getStudentActivityTrend(data) {
    return post('/study/teacher/dashboard/activityTrend', data)
}

// ======================== 2. 课程管理 ========================
/** 获取教师自建课程列表 */
export function getMySubjects(data) {
    return post('/study/teacher/dashboard/mySubjects', data)
}

/** 创建课程 */
export function createSubject(data) {
    return post('/study/teacher/dashboard/createSubject', data)
}

/** 删除课程 */
export function deleteSubject(data) {
    return post('/study/teacher/dashboard/deleteSubject', data)
}

/** 获取所有课程 */
export function getSubjects() {
    return post('/study/teacher/dashboard/subjects', {})
}

/** 获取班级章节列表 */
export function getChapters(data) {
    return post('/study/teacher/course/chapters', data)
}

/** 添加章节 */
export function addChapter(data) {
    return post('/study/teacher/course/addChapter', data)
}

/** 更新章节名称 */
export function updateChapter(data) {
    return post('/study/teacher/course/updateChapter', data)
}

/** 删除章节 */
export function deleteChapter(data) {
    return post('/study/teacher/course/deleteChapter', data)
}

/** 章节排序 */
export function sortChapters(data) {
    return post('/study/teacher/course/sortChapters', data)
}

/** 获取章节内容列表 */
export function getContents(data) {
    return post('/study/teacher/course/contents', data)
}

/** 添加章节内容 */
export function addContent(data) {
    return post('/study/teacher/course/addContent', data)
}

/** 删除章节内容 */
export function deleteContent(data) {
    return post('/study/teacher/course/deleteContent', data)
}

/** 内容排序 */
export function sortContents(data) {
    return post('/study/teacher/course/sortContents', data)
}

/** 获取视频列表 */
export function getVideoList() {
    return post('/study/teacher/dashboard/videoList', {})
}

/** 获取知识点列表 */
export function getKnowledgePointList() {
    return post('/study/teacher/dashboard/knowledgePointList', {})
}

// ======================== 3. 班级与学生管理 ========================
/** 创建班级 */
export function createClass(data) {
    return post('/study/teacher/dashboard/createClass', data)
}

/** 获取教师管理的班级列表 */
export function getMyClasses(data) {
    return post('/study/teacher/dashboard/myClasses', data)
}

/** 删除班级 */
export function deleteClass(data) {
    return post('/study/teacher/dashboard/deleteClass', data)
}

/** 获取班级学生列表 */
export function getClassStudents(data) {
    return post('/study/teacher/dashboard/classStudents', data)
}

/** 搜索学生 */
export function searchStudent(data) {
    return post('/study/teacher/dashboard/searchStudent', data)
}

/** 批量添加学生 */
export function batchAddStudents(data) {
    return post('/study/teacher/dashboard/batchAddStudents', data)
}

/** 移出学生 */
export function removeStudent(data) {
    return post('/study/teacher/dashboard/removeStudent', data)
}

/** 获取学生学情（学习进度、活跃时长） */
export function getStudentLearningStatus(data) {
    return post('/study/teacher/dashboard/studentLearningStatus', data)
}

// ======================== 4. 作业与考试 ========================
/** 获取班级作业列表 */
export function getHomeworkList(data) {
    return post('/study/homework/list', data)
}

/** 获取作业提交列表 */
export function getHomeworkSubmissions(data) {
    return post('/study/teacher/dashboard/homeworkSubmissions', data)
}

/** 批改作业 */
export function gradeHomework(data) {
    return post('/study/teacher/dashboard/gradeHomework', data)
}

/** 发布作业 */
export function publishHomework(data) {
    return post('/study/homework/publish', data)
}

/** 获取题库列表 */
export function getQuestionBank(data) {
    return post('/study/question/list', data)
}

/** 批量导入题目 */
export function importQuestions(data) {
    return post('/study/question/import', data)
}

// ======================== 5. 成绩与报表 ========================
/** 获取班级成绩列表 */
export function getClassScores(data) {
    return post('/study/teacher/dashboard/classScores', data)
}

/** 获取成绩权重配置 */
export function getScoreWeight(data) {
    return post('/study/teacher/dashboard/scoreWeight', data)
}

/** 保存成绩权重配置 */
export function saveScoreWeight(data) {
    return post('/study/teacher/dashboard/saveScoreWeight', data)
}

/** 导出成绩报表 */
export function exportScoreReport(data) {
    return post('/study/teacher/dashboard/exportScoreReport', data)
}

// ======================== 6. 互动与答疑 ========================
/** 获取问答列表 */
export function getQuestionList(data) {
    return post('/study/teacher/dashboard/questionList', data)
}

/** 回复提问 */
export function replyQuestion(data) {
    return post('/study/teacher/dashboard/replyQuestion', data)
}

/** 置顶问答 */
export function pinQuestion(data) {
    return post('/study/teacher/dashboard/pinQuestion', data)
}

/** 发送通知 */
export function sendNotification(data) {
    return post('/study/notice/send', data)
}