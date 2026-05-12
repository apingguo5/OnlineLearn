import { get, post } from "../request";

/**
 * 获取笔记列表
 */
export function getNoteList(params) {
    return get('/note/list', { params })
}

/**
 * 保存笔记
 */
export function saveNote(data) {
    return post('/note/save', data)
}

/**
 * 更新笔记
 */
export function updateNote(data) {
    return post('/note/update', data)
}

/**
 * 删除笔记
 */
export function deleteNote(data) {
    return post('/note/delete', data)
}