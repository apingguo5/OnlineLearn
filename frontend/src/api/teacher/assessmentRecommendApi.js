import { post } from '../request'

const ASSESSMENT_BASE = '/study/recommend/assessment'

/**
 * 智能错题复习推荐
 * @param {Object} params - { userId, courseId?, topN? }
 * POST /study/recommend/assessment/wrong-questions
 */
export function recommendWrongQuestions(params) {
  return post(`${ASSESSMENT_BASE}/wrong-questions`, params)
}

/**
 * 试卷/作业难度匹配推荐
 * @param {Object} params - { userId, courseId?, classId?, topN? }
 * POST /study/recommend/assessment/exam-papers
 */
export function recommendExamPapers(params) {
  return post(`${ASSESSMENT_BASE}/exam-papers`, params)
}

/**
 * 薄弱知识点诊断与靶向训练
 * @param {Object} params - { userId, courseId, topK? }
 * POST /study/recommend/assessment/weak-points
 */
export function diagnoseWeakPoints(params) {
  return post(`${ASSESSMENT_BASE}/weak-points`, params)
}

/**
 * 同类学生对比与榜样推荐
 * @param {Object} params - { userId, classId, courseId? }
 * POST /study/recommend/assessment/peers
 */
export function compareWithPeers(params) {
  return post(`${ASSESSMENT_BASE}/peers`, params)
}

/**
 * 学习预警与干预推荐
 * @param {Object} params - { userId, classId }
 * POST /study/recommend/assessment/alert
 */
export function generateAlert(params) {
  return post(`${ASSESSMENT_BASE}/alert`, params)
}

export default {
  recommendWrongQuestions,
  recommendExamPapers,
  diagnoseWeakPoints,
  compareWithPeers,
  generateAlert,
}
