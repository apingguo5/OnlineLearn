import {post} from "./request";

export function listCourse(data) {
    return post('/study/videoTotal/list',data)

}