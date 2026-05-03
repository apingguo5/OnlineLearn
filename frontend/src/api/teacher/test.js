import {post,get} from "../request";

export function addt(data) {
    return post('/study/exercises/save',data)

}

export function addKnow(data) {
    return post('/study/knowledgePoint/save',data)

}
export function saveTest(data) {
    return post('/study/knowledgePoint/update ', data)
}

export function saveHomework(data) {
    return post('/study/homework/save', data)  
}

export function updateExerciseScore(id, score) {
    return post('/study/userDoExercise/updateScore/' + id + "/" + score)  
}

export function askandanswer(data) {
    return post('/study/askQuestions/list',data)

}

export function answerQuestion(data) {
    return post('/study/askQuestions/update',data)

}

// 删除问题
export function deleteQuestion(data) {
    return post('/study/askQuestions/delete', data)
}
