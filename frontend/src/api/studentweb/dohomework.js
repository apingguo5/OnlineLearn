import {post} from "../request";

export function work(data) {
    return post('/study/homework/findNotDoHomework',data)

}
export function workdo(data) {
    return post('/study/userdohomework/list',data) 

}