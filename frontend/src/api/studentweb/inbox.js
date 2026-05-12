import { get, post } from "../request";

/**
 * 获取通知列表
 */
export function getInboxList(params) {
    return get('/inbox/list', { params })
}

/**
 * 标记通知为已读
 */
export function markAsRead(data) {
    return post('/inbox/read', data)
}

/**
 * 删除通知
 */
export function deleteInbox(data) {
    return post('/inbox/delete', data)
}