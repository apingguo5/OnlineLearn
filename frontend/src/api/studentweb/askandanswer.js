import {post} from "../request";

export function askandanswer(data) {
    return post('/study/askQuestions/list',data)
}

// 删除问题
export function deleteQuestion(data) {
    return post('/study/askQuestions/delete', data)
}

// 修改问题
export function updateQuestion(data) {
    return post('/study/askQuestions/update', data)
}
