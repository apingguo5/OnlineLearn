import {post} from "../request";

/**
 * 搜索学生（按账号或姓名）
 */
export function searchStudent(data) {
    return post('/study/teacher/dashboard/searchStudent', data)
}

/**
 * 添加单个学生到班级
 */
export function addStudentToClass(data) {
    return post('/study/teacher/dashboard/addStudentToClass', data)
}

/**
 * 批量添加学生到班级
 */
export function batchAddStudents(data) {
    return post('/study/teacher/dashboard/batchAddStudents', data)
}

/**
 * 移出学生
 */
export function removeStudent(data) {
    return post('/study/teacher/dashboard/removeStudent', data)
}

/**
 * 获取班级学生列表
 */
export function getClassStudents(data) {
    return post('/study/teacher/dashboard/classStudents', data)
}

/**
 * 批改作业
 */
export function gradeHomework(data) {
    return post('/study/teacher/dashboard/gradeHomework', data)
}

/**
 * 获取作业提交列表
 */
export function getHomeworkSubmissions(data) {
    return post('/study/teacher/dashboard/homeworkSubmissions', data)
}