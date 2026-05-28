/**
 * 教师端 API 封装
 * 基于后端 /study/teacher/dashboard/* 接口
 */
import { post, get, service } from '../request'

const TEACHER_BASE = '/study/teacher/dashboard'

// ========== 课程管理 ==========

/**
 * 获取我的课程列表
 * @param {Object} params - { userId }
 */
export function getMyCourses(params) {
  return post(`${TEACHER_BASE}/mySubjects`, params)
}

/**
 * 创建新课程
 * @param {Object} params - { courseName, userId, description? }
 */
export function createCourse(params) {
  return post(`${TEACHER_BASE}/createSubject`, params)
}

/**
 * 更新课程
 * @param {Object} params - { id, courseName, description?, coverUrl? }
 */
export function updateCourse(params) {
  return post(`${TEACHER_BASE}/updateSubject`, params)
}

/**
 * 获取 resource 目录下的图片列表（课程封面选择）
 * GET /study/teacher/dashboard/listCoverImages
 */
export function getResourceImages() {
  return get(`${TEACHER_BASE}/listCoverImages`)
}

/**
 * 上传课程封面图片到 resource 目录，后端直接更新 course 表 cover_url
 * @param {FormData} formData - 包含 "file" 和 "courseId" 字段
 * 注意：不能显式设置 Content-Type 头，axios 会自动删除 Content-Type，
 * 让浏览器写入正确的 multipart/form-data; boundary=... 否则后端无法解析
 */
export function importCoverImage(formData) {
  return service({
    url: `${TEACHER_BASE}/importCoverImage`,
    method: 'post',
    data: formData
  })
}

/**
 * 删除课程
 * @param {Number} id - 课程ID
 */
export function deleteCourse(id) {
  return post(`${TEACHER_BASE}/deleteSubject`, { id })
}

/**
 * 获取所有课程（系统级+教师自建）
 */
export function getAllSubjects() {
  return get(`${TEACHER_BASE}/subjects`)
}

// ========== 章节管理（大纲编辑器） ==========

/**
 * 获取课程的章节列表（扁平）
 * @param {Object} params - { courseId }
 */
export function getChaptersByCourseId(params) {
  return post(`/study/teacher/course/chapters`, params)
}

/**
 * 获取课程的章节树形结构
 * @param {Object} params - { classId }
 */
export function getChapterTree(params) {
  return post(`/study/teacher/course/chapterTree`, params)
}

/**
 * 创建章节
 * @param {Object} params - { classId, chapterName, description?, parentId? }
 */
export function createChapter(params) {
  return post(`/study/teacher/course/addChapter`, params)
}

/**
 * 更新章节
 * @param {Object} params - { id, chapterName?, description? }
 */
export function updateChapter(params) {
  return post(`/study/teacher/course/updateChapter`, params)
}

/**
 * 删除章节
 * @param {Object} params - { id }
 */
export function deleteChapter(params) {
  return post(`/study/teacher/course/deleteChapter`, params)
}

/**
 * 章节排序
 * @param {Object} params - 排序信息
 */
export function reorderChapters(params) {
  return post(`/study/teacher/course/batchUpdateChapters`, params)
}

/**
 * 复制章节
 * @param {Object} params - { id/chapterId }
 */
export function copyChapter(params) {
  return post(`/study/teacher/course/copyChapter`, params)
}

/**
 * 批量更新章节（排序、层级调整）
 * @param {Array} chapters - [{id, sortOrder, parentId}, ...]
 */
export function batchUpdateChapters(chapters) {
  return post(`/study/teacher/course/batchUpdateChapters`, { chapters })
}

/**
 * 添加本地路径资源（测试用）
 * @param {Object} params - { chapterId, courseId, resourceName, localPath }
 */
export function addLocalResource(params) {
  return post(`/study/teacher/course/addLocalResource`, params)
}

// ========== 章节内容管理 ==========

/**
 * 获取章节内容列表
 * @param {Object} params - { chapterId }
 */
export function getChapterContents(params) {
  return post(`/study/teacher/course/contents`, params)
}

/**
 * 添加章节内容
 * @param {Object} params - { chapterId, contentType, contentData }
 */
export function addContent(params) {
  return post(`/study/teacher/course/addContent`, params)
}

/**
 * 删除章节内容
 * @param {Object} params - { id }
 */
export function deleteContent(params) {
  return post(`/study/teacher/course/deleteContent`, params)
}

// ========== 课件资源管理 ==========

/**
 * 上传课件资源文件
 * @param {FormData} formData - 包含文件的表单数据
 */
export function uploadResource(formData) {
  return post(`/study/teacher/dashboard/uploadFile`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/**
 * 获取课程资源列表
 * @param {Object} params - { subjectId }
 */
export function getCourseResources(params) {
  return post(`/study/teacher/dashboard/getResources`, params)
}

/**
 * 删除资源
 * @param {Object} params - { id }
 */
export function deleteResource(params) {
  return post(`/study/teacher/dashboard/deleteResource`, params)
}

// ========== 文件系统课程扫描 ==========

export function scanCourseTree() {
  return get('/study/teacher/course-scanner/tree')
}

export function scanCourseOverview() {
  return get('/study/teacher/course-scanner/overview')
}

// ========== 题库相关 ==========

/**
 * 获取题目列表
 * @param {Object} params - { classId?, courseId?, keywords? }
 */
export function getQuestions(params) {
  return post(`/study/teacher/course/questions`, params)
}

/**
 * 创建题目
 * @param {Object} params - { title, type, options?, answer?, classId?, courseId? }
 */
export function createQuestion(params) {
  return post(`/study/exam/question/save`, params)
}

// ========== 班级与学生管理 ==========

/**
 * 获取所有课程（用于创建班级时选择课程）
 * 调用 /study/teacher/dashboard/subjects (GET)
 * 返回格式: { code: 0, data: { list: [...] } }
 * 课程对象字段: { id, courseName, creatorId, status, createTime, ... }
 */
export function getCourses() {
  return get(`${TEACHER_BASE}/subjects`)
}

/**
 * 获取我的班级列表
 * 调用 /study/class/findList (POST) 按教师ID查询，无需分页参数
 * @param {Object} params - { userId }
 * 返回格式: { resultData: [...], code: 200 }  (resultData 为数组)
 */
export function getMyClasses(params) {
  return post(`/study/class/findList`, params)
}

/**
 * 创建班级
 * 调用 /study/class/save (POST)
 * @param {Object} params - { courseId, className, userId, academicYear?, semester?, maxStudents? }
 * 返回格式: { code: 0, msg: "success" }
 */
export function createClass(params) {
  return post(`/study/class/save`, params)
}

/**
 * 删除班级
 * 调用 /study/class/delete (POST)
 * @param {Object} params - { id }
 * 返回格式: { code: 0, msg: "success" }
 */
export function deleteClass(params) {
  return post(`/study/class/delete`, params)
}

/**
 * 更新班级信息
 * 调用 /study/class/update (POST)
 * @param {Object} params - { id, className?, ... }
 */
export function updateClass(params) {
  return post(`/study/class/update`, params)
}

/**
 * 获取班级学生列表
 * 调用 /study/userClass/findList (POST)
 * @param {Object} params - { classId }
 * 返回格式: { code: 0, data: [...] }
 */
export function getClassStudents(params) {
  return post(`/study/userClass/findList`, params)
}

/**
 * 移出班级学生
 * 调用 /study/userClass/delete (POST)
 * @param {Object} params - { userId, classId? }
 * 返回格式: { code: 0, msg: "success" }
 */
export function removeStudent(params) {
  return post(`/study/userClass/delete`, params)
}

/**
 * 批量导入学生
 * 调用 /study/userClass/save (POST) - 逐条添加
 * @param {Object} params - { classId, studentIds: [...] }
 * 返回格式: { code: 0, msg: "success" }
 */
export function batchAddStudents(params) {
  return post(`/study/userClass/save`, params)
}

/**
 * 获取学生学习状态（学情监控）
 * 调用 /study/class/info (POST) 获取班级详情及学习情况
 * @param {Object} params - { classId }
 */
export function getStudentLearningStatus(params) {
  return post(`/study/userClass/findList`, params)
}

// ========== 课程资源管理（course_resource 表） ==========

/**
 * 根据章节ID获取资源列表
 * @param {Object} params - { chapterId }
 * POST /study/teacher/course/resource/listByChapter
 */
export function getResourcesByChapter(params) {
  return post(`/study/teacher/course/resource/listByChapter`, params)
}

/**
 * 根据课程ID获取资源列表
 * @param {Object} params - { courseId }
 * POST /study/teacher/course/resource/listByCourse
 */
export function getResourcesByCourse(params) {
  return post(`/study/teacher/course/resource/listByCourse`, params)
}

/**
 * 添加本地路径资源（测试用）
 * @param {Object} params - { chapterId, resourceName, localPath, resourceType?, uploaderId? }
 * POST /study/teacher/course/resource/addLocal
 */
export function addLocalResourceToDb(params) {
  return post(`/study/teacher/course/resource/addLocal`, params)
}

/**
 * 添加资源（通用）
 * @param {Object} params - { courseId, resourceName, resourceType, fileUrl, chapterId?, ... }
 * POST /study/teacher/course/resource/add
 */
export function addCourseResource(params) {
  return post(`/study/teacher/course/resource/add`, params)
}

/**
 * 更新资源
 * @param {Object} params - { id, resourceName?, resourceType?, fileUrl?, ... }
 * POST /study/teacher/course/resource/update
 */
export function updateCourseResource(params) {
  return post(`/study/teacher/course/resource/update`, params)
}

/**
 * 删除资源
 * @param {Object} params - { id }
 * POST /study/teacher/course/resource/delete
 */
export function deleteCourseResourceById(params) {
  return post(`/study/teacher/course/resource/delete`, params)
}

/**
 * 批量删除资源
 * @param {Object} params - { ids: [1,2,3] }
 * POST /study/teacher/course/resource/batchDelete
 */
export function batchDeleteCourseResources(params) {
  return post(`/study/teacher/course/resource/batchDelete`, params)
}

/**
 * 资源排序
 * @param {Object} params - { ids: [3,1,2] }
 * POST /study/teacher/course/resource/sort
 */
export function sortCourseResources(params) {
  return post(`/study/teacher/course/resource/sort`, params)
}

/**
 * 检索资源
 * @param {Object} params - { courseId?, chapterId?, resourceType? }
 * POST /study/teacher/course/resource/search
 */
export function searchCourseResources(params) {
  return post(`/study/teacher/course/resource/search`, params)
}

// ========== 作业/任务管理 ==========

/**
 * 获取已发布任务列表
 * @param {Object} params - { page?, pageSize? }
 * POST /study/homework/list
 */
export function getPublishedTasks(params) {
  return post(`/study/homework/list`, params)
}

/**
 * 发布任务（创建作业）
 * @param {Object} params - { title, type, classId, paperId?, deadline }
 * POST /study/homework/save
 */
export function publishTask(params) {
  return post(`/study/homework/save`, params)
}

/**
 * 删除任务
 * @param {Object} params - { id }
 * POST /study/homework/delete
 */
export function deleteTask(params) {
  return post(`/study/homework/delete`, params)
}

// ========== 通知管理 ==========

/**
 * 获取通知列表（教师端）
 * @param {Object} params - { page, pageSize, senderId?, classId? }
 * POST /study/classNotice/list
 */
export function getNotifications(params) {
  return post(`/study/classNotice/list`, params)
}

/**
 * 发送通知
 * @param {Object} params - { title, content, classId, senderId }
 * POST /study/classNotice/save
 */
export function sendNotification(params) {
  return post(`/study/classNotice/save`, params)
}

/**
 * 删除通知
 * @param {Object} params - { id }
 * POST /study/classNotice/delete
 */
export function deleteNotification(params) {
  return post(`/study/classNotice/delete`, params)
}

// ========== 问答（ask_questions 表） ==========

/**
 * 获取问答列表（教师端）
 * @param {Object} params - { page, pageSize, userId, roleId }
 * POST /study/askQuestions/list
 */
export function getQaQuestions(params) {
  return post(`/study/askQuestions/list`, params)
}

/**
 * 回答/回复问题
 * @param {Object} params - { id, restore, status }
 * POST /study/askQuestions/update
 */
export function answerQaQuestion(params) {
  return post(`/study/askQuestions/update`, params)
}

/**
 * 删除问答问题
 * @param {Object} params - { id }
 * POST /study/askQuestions/delete
 */
export function deleteQaQuestion(params) {
  return post(`/study/askQuestions/delete`, params)
}

// ========== 学生端通知 ==========

/**
 * 学生端查询通知（仅显示学生所在班级的通知）
 * @param {Object} params - { userId, page, pageSize, classId? }
 * POST /study/classNotice/studentList
 */
export function getStudentNotices(params) {
  return post(`/study/classNotice/studentList`, params)
}

// ========== 成绩与报表 ==========

/**
 * 获取班级成绩列表
 * @param {Object} params - { classId }
 * POST /study/grade/classGrades
 */
export function getClassGrades(params) {
  return post(`/study/grade/classGrades`, params)
}

/**
 * 保存权重设置
 * @param {Object} params - { classId, weights: [{label, percent}] }
 * POST /study/grade/saveWeights
 */
export function saveWeightConfig(params) {
  return post(`/study/grade/saveWeights`, params)
}

/**
 * 加载权重设置
 * @param {Object} params - { classId }
 * POST /study/grade/loadWeights
 */
export function loadWeights(params) {
  return post(`/study/grade/loadWeights`, params)
}

/**
 * 导出成绩报表
 * @param {Object} params - { classId }
 */
export function exportGradeReport(params) {
  return post(`/study/grade/classGrades`, params)
}

// ========== AI助学 ==========

/**
 * 分析班级学生成绩
 * @param {Object} params - { classId }
 * POST /study/ai/analyzeClass
 */
export function analyzeClass(params) {
  return post(`/study/ai/analyzeClass`, params)
}

/**
 * 获取AI学习建议
 * @param {Object} params - { classId }
 * POST /study/ai/aiRecommend
 */
export function getAiRecommend(params) {
  return post(`/study/ai/aiRecommend`, params)
}

// 默认导出（向后兼容）
export default {
  getMyCourses,
  createCourse,
  updateCourse,
  deleteCourse,
  getAllSubjects,
  getChaptersByCourseId,
  getChapterTree,
  createChapter,
  updateChapter,
  deleteChapter,
  reorderChapters,
  copyChapter,
  batchUpdateChapters,
  getChapterContents,
  addContent,
  deleteContent,
  uploadResource,
  getCourseResources,
  deleteResource,
  scanCourseTree,
  scanCourseOverview,
  getQuestions,
  createQuestion,
  // 班级管理
  getCourses,
  getMyClasses,
  createClass,
  deleteClass,
  updateClass,
  getClassStudents,
  removeStudent,
  batchAddStudents,
  getStudentLearningStatus,
  getResourcesByChapter,
  getResourcesByCourse,
  addLocalResourceToDb,
  addCourseResource,
  updateCourseResource,
  deleteCourseResourceById,
  batchDeleteCourseResources,
  sortCourseResources,
  searchCourseResources,
  // 作业/任务管理
  getPublishedTasks,
  publishTask,
  deleteTask,
  // 通知管理
  getNotifications,
  sendNotification,
  deleteNotification,
  // 问答
  getQaQuestions,
  answerQaQuestion,
  deleteQaQuestion,
  // 学生端通知
  getStudentNotices,
  // 成绩与报表
  getClassGrades,
  saveWeightConfig,
  loadWeights,
  exportGradeReport,
  // AI助学
  analyzeClass,
  getAiRecommend
}
