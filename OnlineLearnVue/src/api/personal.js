import {post} from "./request";

export function listKnow(data) {
    return post('/study/knowledgePoint/list',data)

}
export function deleteKnow(data) {
    return post('/study/knowledgePoint/delete ',data)

}
export function personal(data) {
    return post('/study/user/info',data)

}


export function password(data) {
    return post('/study/user/updatePassword',data)

}