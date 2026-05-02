import {post,get} from "../request"; 


export function konwpoint(data) {
    // alert('ppppp')
    return post('/study/knowledgePoint/list', data)
}
export function deletekonwpoint(data) {
    // alert('ppppp')
    return post('/study/knowledgePoint/delete', data)
}

export function stuPointList(data) { 
    // alert('ppppp')
    return get('/study/knowledgePoint/stuPointList', data) 
}
import '@/api/admin/initialize'
