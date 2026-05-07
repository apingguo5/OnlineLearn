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

// ====== 班级管理新接口 ======

/**
 * 获取所有课程科目
 */
export function getSubjects() {
    return post('/study/teacher/dashboard/subjects', {})
}

/**
 * 获取教师自建的课程列表
 */
export function getMySubjects(data) {
    return post('/study/teacher/dashboard/mySubjects', data)
}

/**
 * 创建课程（教师自建科目）
 */
export function createSubject(data) {
    return post('/study/teacher/dashboard/createSubject', data)
}

/**
 * 删除课程
 */
export function deleteSubject(data) {
    return post('/study/teacher/dashboard/deleteSubject', data)
}

/**
 * 创建班级
 */
export function createClass(data) {
    return post('/study/teacher/dashboard/createClass', data)
}

/**
 * 获取教师管理的班级列表
 */
export function getMyClasses(data) {
    return post('/study/teacher/dashboard/myClasses', data)
}

/**
 * 删除班级
 */
export function deleteClass(data) {
    return post('/study/teacher/dashboard/deleteClass', data)
}

/**
 * 获取所有视频列表
 */
export function getVideoList() {
    return post('/study/teacher/dashboard/videoList', {})
}

/**
 * 获取所有知识点列表
 */
export function getKnowledgePointList() {
    return post('/study/teacher/dashboard/knowledgePointList', {})
}

// ====== 课程章节内容管理 ======

/**
 * 获取班级章节列表
 */
export function getChapters(data) {
    return post('/study/teacher/course/chapters', data)
}

/**
 * 添加章节
 */
export function addChapter(data) {
    return post('/study/teacher/course/addChapter', data)
}

/**
 * 更新章节名称
 */
export function updateChapter(data) {
    return post('/study/teacher/course/updateChapter', data)
}

/**
 * 删除章节
 */
export function deleteChapter(data) {
    return post('/study/teacher/course/deleteChapter', data)
}

/**
 * 章节排序
 */
export function sortChapters(data) {
    return post('/study/teacher/course/sortChapters', data)
}

/**
 * 获取章节内容列表
 */
export function getContents(data) {
    return post('/study/teacher/course/contents', data)
}

/**
 * 添加章节内容（视频或文字阅读）
 */
export function addContent(data) {
    return post('/study/teacher/course/addContent', data)
}

/**
 * 删除章节内容
 */
export function deleteContent(data) {
    return post('/study/teacher/course/deleteContent', data)
}

/**
 * 内容排序
 */
export function sortContents(data) {
    return post('/study/teacher/course/sortContents', data)
}