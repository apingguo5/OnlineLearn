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
