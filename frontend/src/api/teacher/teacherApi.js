/**
 * 教师端 API 封装
 * 基于后端 /study/teacher/dashboard/* 接口
 */
import { post, get } from '../request'

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
 * @param {Object} params - { id, courseName, description? }
 */
export function updateCourse(params) {
  return post(`${TEACHER_BASE}/updateSubject`, params)
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
 * @param {Object} params - { courseId, parentId?, title, type, content?, sort?, publishStatus? }
 */
export function createChapter(params) {
  return post(`/study/teacher/course/addChapter`, params)
}

/**
 * 更新章节
 * @param {Object} params - { id, chapterName?, chapterType?, description?, sortOrder?, parentId?, publishStatus?, content? }
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
  return post(`/study/teacher/course/addQuestion`, params)
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
  createQuestion
}