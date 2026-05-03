import {post} from "../request";

export function practices(data) {
    return post('/study/exercises/findNotDoExercises',data)

}