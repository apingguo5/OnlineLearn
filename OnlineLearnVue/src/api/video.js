import {post} from "./request";

export function listVideo(data) {
    return post('/study/videoTotal/list',data)

}
export function savaVideo(data) {
    return post('/study/videoTotal/save',data)

}
export function deleteVideos(data) {
    return post('/study/videoTotal/delete',data)

}

export function pageVideo(data) {
    return post('/study/videoTotal/pageVideo',data)

}