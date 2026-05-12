import { post } from "../request";

/**
 * 获取所有班级列表（含关联课程信息）
 * 调用后端 /study/class/findList
 */
export function getAllClasses(data) {
    return post('/study/class/findList', data)
}