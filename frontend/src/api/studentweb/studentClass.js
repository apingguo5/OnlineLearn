import { post } from "../request";

/**
 * 获取所有班级列表（含关联课程信息）
 * 调用后端 /study/class/findList
 */
export function getAllClasses(data) {
    return post('/study/class/findList', data)
}

/**
 * 获取学生已加入的课程列表（通过 userId 过滤）
 */
export function getStudentClasses(data) {
    return post('/study/class/findList', data)
}