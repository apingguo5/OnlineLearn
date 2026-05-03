import {post} from "../request";


export function tclassmanagemt(data) {
    // alert('ppppp')
    return post('/study/userClass/list', data)
}
export function deleteStudent(data) {
    // alert('ppppp')
    return post('/study/userClass/delete', data)
}
