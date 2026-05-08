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
  }
}
