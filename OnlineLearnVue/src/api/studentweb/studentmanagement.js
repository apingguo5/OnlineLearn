import {post} from "../request";


export function smanamgent(data) {
    // alert('ppppp')
    return post('/study/class/list', data)
}

export function addClass(data) {
    return post('/study/applicant/save',data)

}

export function detailhistory(data) {
    return post('/study/applicant/list',data)

}

export function isFlage(data) {
    return post('/study/applicant/joinTrueOrFalse',data)

}
import '../admin/initialize'

