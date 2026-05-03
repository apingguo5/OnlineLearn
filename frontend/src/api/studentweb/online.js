import {post} from "../request";

export function onlineweb(data) {
    return post('/study/videoTotal/pageVideo',data)

}
export function onlinecourse(data) {
    return post('/study/subject/queryList',data)

}