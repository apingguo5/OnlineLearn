import {post} from "./request";

export function listAllScore(data) {
    return post('/study/exercises/list',data)

}
export function listNotHomework(data) {
    return post('/study/user/findNotDoWork',data)

}
export function listHomework(data) {
    return post('/study/userDoExercise/list',data)

}