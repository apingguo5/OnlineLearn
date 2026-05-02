import { get } from "../request";

export function getWatchTimeByUserId(userId) {
    return get('/study/videoWatchRecord/queryByUserId', { userId });
}

export function getTotalWatchTimeByUserId(userId) {
    return get('/study/videoWatchRecord/queryTotalByUserId', { userId });
}

export function getWatchTimeByVideoTotalId(videoTotalId) {
    return get('/study/videoWatchRecord/queryByVideoTotalId', { videoTotalId });
}

export function getWatchTimeByClassId(classId) {
    return get('/study/videoWatchRecord/queryByClassId', { classId });
}