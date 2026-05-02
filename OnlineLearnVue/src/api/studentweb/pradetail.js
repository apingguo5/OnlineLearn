import {post} from "../request";

export function practicesde(data) {
    return post('/study/userDoExercise/list',data)

}