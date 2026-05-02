import {post} from "../request";

export function tho(data) {
    return post('/study/homework/list',data)

}
export function deletHomework(data) {
    return post('/study/homework/delete',data)

}
export function listDoHomework(data) {
    return post('/study/userdohomework/list',data)

}
export function listNotDoHomework(data) {
    return post('/study/user/findNotDoHomework',data)

}