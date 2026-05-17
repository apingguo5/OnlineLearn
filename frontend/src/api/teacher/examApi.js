/**
 * 考试/答题系统 API 封装
 * 题库管理、组卷、学生答题、教师批改
 */
import { post, get } from '../request'

// ========== 题库管理 ==========

/**
 * 获取题目列表（分页）
 * @param {Object} params - { page, limit, courseId, chapterId, questionType, keywords }
 * POST /study/exam/question/list
 */
export function getQuestionList(params) {
  return post('/study/exam/question/list', params)
}

/**
 * 创建题目
 * @param {Object} params - { questionType, stem, options, answer, analysis, score, difficulty, courseId, chapterId, creatorId }
 * POST /study/exam/question/save
 */
export function createQuestion(params) {
  return post('/study/exam/question/save', params)
}

/**
 * 更新题目
 * @param {Object} params - { id, questionType, stem, options, answer, analysis, score, difficulty, courseId, chapterId }
 * POST /study/exam/question/update
 */
export function updateQuestion(params) {
  return post('/study/exam/question/update', params)
}

/**
 * 删除题目
 * @param {Number} id - 题目ID
 * POST /study/exam/question/delete
 */
export function deleteQuestion(id) {
  return post('/study/exam/question/delete', { id })
}

/**
 * 批量删除题目
 * @param {Array} ids - 题目ID列表
 * POST /study/exam/question/batchDelete
 */
export function batchDeleteQuestions(ids) {
  return post('/study/exam/question/batchDelete', ids)
}

/**
 * 根据ID获取题目详情
 * @param {Number} id
 * GET /study/exam/question/info/{id}
 */
export function getQuestionInfo(id) {
  return get(`/study/exam/question/info/${id}`)
}

/**
 * 根据试卷ID获取题目列表
 * @param {Number} paperId
 * POST /study/exam/question/listByPaper
 */
export function getQuestionsByPaperId(paperId) {
  return post('/study/exam/question/listByPaper', { paperId })
}

/**
 * 获取所有题目（不分页，用于组卷选择）
 * @param {Object} params - { courseId, chapterId, questionType }
 * POST /study/exam/question/all
 */
export function getAllQuestions(params) {
  return post('/study/exam/question/all', params)
}

// ========== 试卷管理 ==========

/**
 * 获取试卷列表（分页）
 * @param {Object} params - { page, limit, courseId, classId, status, title }
 * POST /study/exam/paper/list
 */
export function getPaperList(params) {
  return post('/study/exam/paper/list', params)
}

/**
 * 创建试卷
 * @param {Object} params - { title, description, paperType, courseId, chapterId, classId, creatorId, duration, totalScore, questionIds }
 * POST /study/exam/paper/save
 */
export function createPaper(params) {
  return post('/study/exam/paper/save', params)
}

/**
 * 更新试卷
 * @param {Object} params - { id, title, description, paperType, duration, totalScore, questionIds }
 * POST /study/exam/paper/update
 */
export function updatePaper(params) {
  return post('/study/exam/paper/update', params)
}

/**
 * 删除试卷
 * @param {Number} id
 * POST /study/exam/paper/delete
 */
export function deletePaper(id) {
  return post('/study/exam/paper/delete', { id })
}

/**
 * 发布试卷
 * @param {Number} id
 * POST /study/exam/paper/publish
 */
export function publishPaper(id) {
  return post('/study/exam/paper/publish', { id })
}

/**
 * 获取试卷详情（含题目列表）
 * @param {Number} paperId
 * POST /study/exam/paper/detail
 */
export function getPaperDetail(paperId) {
  return post('/study/exam/paper/detail', { paperId })
}

/**
 * 获取课程下的所有试卷
 * @param {Number} courseId
 * POST /study/exam/paper/listByCourse
 */
export function getPapersByCourseId(courseId) {
  return post('/study/exam/paper/listByCourse', { courseId })
}

// ========== 学生答题 ==========

/**
 * 获取学生可见的试卷列表
 * @param {Object} params - { studentId, courseId, classId }
 * POST /study/exam/paper/studentPapers
 */
export function getStudentPapers(params) {
  return post('/study/exam/paper/studentPapers', params)
}

/**
 * 开始考试（初始化答题记录）
 * @param {Object} params - { paperId, studentId }
 * POST /study/exam/answer/start
 */
export function startExam(params) {
  return post('/study/exam/answer/start', params)
}

/**
 * 提交答案（支持单题或整卷）
 * @param {Object} params - { paperId, studentId, answers: [...] }
 * POST /study/exam/answer/submit
 */
export function submitAnswer(params) {
  return post('/study/exam/answer/submit', params)
}

/**
 * 暂存答案（存草稿）
 * @param {Object} params - { paperId, questionId, studentId, answer }
 * POST /study/exam/answer/saveDraft
 */
export function saveAnswerDraft(params) {
  return post('/study/exam/answer/saveDraft', params)
}

/**
 * 获取学生的答题记录
 * @param {Object} params - { paperId, studentId }
 * POST /study/exam/answer/studentAnswers
 */
export function getStudentAnswers(params) {
  return post('/study/exam/answer/studentAnswers', params)
}

/**
 * 获取学生某次答题的详细情况（含题目和批改信息）
 * @param {Object} params - { paperId, studentId }
 * POST /study/exam/answer/studentDetail
 */
export function getStudentAnswerDetail(params) {
  return post('/study/exam/answer/studentDetail', params)
}

// ========== 教师批改 ==========

/**
 * 获取试卷的学生答题列表
 * @param {Number} paperId
 * POST /study/exam/answer/paperStudentList
 */
export function getPaperStudentAnswers(paperId) {
  return post('/study/exam/answer/paperStudentList', { paperId })
}

/**
 * 批改单个题目打分
 * @param {Object} params - { recordId, score, remark }
 * POST /study/exam/answer/review
 */
export function reviewAnswer(params) {
  return post('/study/exam/answer/review', params)
}

/**
 * 批量批改
 * @param {Array} reviewList - [{ recordId, score, remark }]
 * POST /study/exam/answer/batchReview
 */
export function batchReviewAnswers(reviewList) {
  return post('/study/exam/answer/batchReview', reviewList)
}

export default {
  getQuestionList,
  createQuestion,
  updateQuestion,
  deleteQuestion,
  batchDeleteQuestions,
  getQuestionInfo,
  getQuestionsByPaperId,
  getAllQuestions,
  getPaperList,
  createPaper,
  updatePaper,
  deletePaper,
  publishPaper,
  getPaperDetail,
  getPapersByCourseId,
  getStudentPapers,
  startExam,
  submitAnswer,
  saveAnswerDraft,
  getStudentAnswers,
  getStudentAnswerDetail,
  getPaperStudentAnswers,
  reviewAnswer,
  batchReviewAnswers
}