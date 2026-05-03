
// let base_api = '127.0.0.1:9090'

import {post,get} from "./request";

export function loginRequest(data) {
    return post('/study/user/login', data)
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
