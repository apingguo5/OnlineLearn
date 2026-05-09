/**
 * 教师端 API 封装
 * 基于后端 /study/teacher/dashboard/* 接口
 */
import { post, get } from '../request'

const TEACHER_BASE = '/study/teacher/dashboard'

export default {
  /**
   * 获取我的课程列表
   * @param {Number} userId - 教师用户ID
   */
  getMyCourses(userId) {
    return post(`${TEACHER_BASE}/mySubjects`, { userId })
  },

  /**
   * 创建新课程
   * @param {Object} params - { courseName, userId, description? }
   */
  createCourse(params) {
    return post(`${TEACHER_BASE}/createSubject`, params)
  },

  /**
   * 更新课程
   * @param {Object} params - { id, courseName, description? }
   */
  updateCourse(params) {
    return post(`${TEACHER_BASE}/updateSubject`, params)
  },

  /**
   * 删除课程
   * @param {Number} id - 课程ID
   */
  deleteCourse(id) {
    return post(`${TEACHER_BASE}/deleteSubject`, { id })
  },

  /**
   * 获取所有课程（系统级+教师自建）
   */
  getAllSubjects() {
    return get(`${TEACHER_BASE}/subjects`)
  },

  // ========== 章节管理 ==========

  /**
   * 获取课程的章节列表
   * @param {Number} courseId - 课程ID
   */
  getChapters(courseId) {
    return post(`/study/teacher/course/chapters`, { classId: courseId })
  },

  /**
   * 添加章节
   * @param {Object} params - { classId, chapterName }
   */
  addChapter(params) {
    return post(`/study/teacher/course/addChapter`, params)
  },

  /**
   * 更新章节
   * @param {Object} params - { id, chapterName }
   */
  updateChapter(params) {
    return post(`/study/teacher/course/updateChapter`, params)
  },

  /**
   * 删除章节
   * @param {Number} id - 章节ID
   */
  deleteChapter(id) {
    return post(`/study/teacher/course/deleteChapter`, { id })
  },

  // ========== 章节内容管理 ==========

  /**
   * 获取章节内容列表
   * @param {Number} chapterId - 章节ID
   */
  getContents(chapterId) {
    return post(`/study/teacher/course/contents`, { chapterId })
  },

  /**
   * 添加章节内容
   * @param {Object} params - { chapterId, contentType, contentData }
   */
  addContent(params) {
    return post(`/study/teacher/course/addContent`, params)
  },

  /**
   * 删除章节内容
   * @param {Number} id - 内容ID
   */
  deleteContent(id) {
    return post(`/study/teacher/course/deleteContent`, { id })
  },

  // ========== 课件资源管理 ==========

  /**
   * 上传课件资源文件
   * @param {FormData} formData - 包含文件的表单数据
   */
  uploadResource(formData) {
    return post(`/study/teacher/dashboard/uploadFile`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  /**
   * 获取课程资源列表
   * @param {Number} courseId - 课程ID
   */
  getResources(courseId) {
    return post(`/study/teacher/dashboard/getResources`, { subjectId: courseId })
  },

  /**
   * 删除资源
   * @param {Number} id - 资源ID
   */
  deleteResource(id) {
    return post(`/study/teacher/dashboard/deleteResource`, { id })
  }
}
