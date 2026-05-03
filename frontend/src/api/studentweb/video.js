import { post } from "../request";

export function videos(data) {
    return post('/study/videos/selectByVideoTotalId',data)
}
export function deleteVideo(data) {
    return post('/study/videos/delete',data)
}
export function askandanswer(data) {
    return post('/study/askQuestions/save',data)
}