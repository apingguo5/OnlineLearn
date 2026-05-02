import {post} from "../request";


export function tapplicant(data) {
    // alert('ppppp')
    return post('/study/applicant/list', data)
}
export function alertapplicant(data) {
    // alert('ppppp')
    return post('/study/applicant/update', data)
}