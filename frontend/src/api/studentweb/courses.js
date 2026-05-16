import { get, post } from "../request";

/**
 * 获取所有课程列表（从 course 表查询）
 * 调用后端 /study/subject/findList
 */
export function getAllCourses() {
    return get('/study/subject/findList')
}

/**
 * 分页查询课程
 * 调用后端 /study/subject/list
 */
export function getCoursePage(data) {
    return post('/study/subject/list', data)
}

/**
 * 获取单个课程详情
 * 调用后端 /study/subject/info/{id}
 */
export function getCourseById(id) {
    return get(`/study/subject/info/${id}`)
}

/**
 * 根据课程ID查询关联的班级列表
 * 调用后端 /study/class/byCourse/{courseId}
 */
export function getClassesByCourseId(courseId) {
    return get(`/study/class/byCourse/${courseId}`)
}

/**
 * 查询用户已选的班级列表（用于判断选课状态）
 * 调用后端 /study/userClass/findList (POST)
 * @param {Object} params - { userId }
 */
export function getUserClassList(params) {
    return post('/study/userClass/findList', params)
}

/**
 * 选课 - 加入班级
 * 调用后端 /study/userClass/save (POST)
 * @param {Object} data - { userId, classId }
 */
export function enrollClass(data) {
    return post('/study/userClass/save', data)
}

/**
 * 退选 - 退出班级
 * 调用后端 /study/userClass/delete (POST)
 * @param {Object} data - { userId, classId }
 */
export function unenrollClass(data) {
    return post('/study/userClass/delete', data)
}

/**
 * 查询用户已选课程班级详细信息（含课程信息、班级信息、教师信息）
 * 调用后端 GET /study/userClass/enrolledCourses/{userId}
 * @param {Number} userId - 用户ID
 */
export function getEnrolledCourses(userId) {
    return get(`/study/userClass/enrolledCourses/${userId}`)
}

/**
 * 获取课程的学习章节列表（树形结构）
 * 调用后端 POST /study/student/course/learningChapters
 * @param {Object} data - { classId }
 */
export function getLearningChapters(data) {
    return post('/study/student/course/learningChapters', data)
}

/**
 * 获取章节内容列表（含视频/阅读等资源详情）
 * 调用后端 POST /study/student/course/chapterContents
 * @param {Object} data - { chapterId }
 */
export function getChapterContents(data) {
    return post('/study/student/course/chapterContents', data)
}
