import {post, get} from "../request";

export function askandanswer(data) {
    return post('/study/askQuestions/list', data)
}

export function deleteQuestion(data) {
    return post('/study/askQuestions/delete', data)
}

export function updateQuestion(data) {
    return post('/study/askQuestions/update', data)
}

export function saveQuestion(data) {
    return post('/study/askQuestions/save', data)
}

export function getStudentNotices(data) {
    return post('/study/classNotice/studentList', data)
}

export function getMyClasses(data) {
    return post('/study/class/findList', data)
}

export function getEnrolledClasses(userId) {
    return get(`/study/userClass/enrolledCourses/${userId}`)
}
