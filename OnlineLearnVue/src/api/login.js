
// let base_api = '127.0.0.1:9090'

import {post,get} from "./request";

export function loginRequest(data) {
    alert('loginRequest called with: ' + JSON.stringify(data))
    console.log('loginRequest called with:', data)
    const result = post('/study/user/login', data)
    console.log('loginRequest result:', result)
    return result
}

export function outLoginRequest() {
     return get('api/tab-user/logout')

}

export function VerificationCode() {
     return get('api/tab-user/code')

}

export function register(data) {
     return post('/study/user/registry',data)

}
