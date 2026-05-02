<template>
    <div>
        <el-container>
            <el-header>
                <Header style="height: 80px"></Header>
            </el-header>
            <el-main>
<!--                <div class=""><b> 欢迎访问学生端，{{ Info.userName }} 同学</b></div>-->
                <router-view></router-view>
            </el-main>
        </el-container>

    </div>
</template>

<script>
import Header from "./header/Header";

import {personal} from '../../../api/personal.js'
import Cookies from 'js-cookie'
export default {
    name: "StudentContainWeb",
    data() {
        return {
            Info: {}
        }
    },
    components: { Header },
    mounted() {
        personal({'id' : Cookies.get("userId")}).then(resp => {
            this.Info = resp.data.resultData
        })
    },
    updated() {
        // 暂时注释掉角色检查，以便直接访问问答页面
        // var roleId = Cookies.get("roleId")
        // if(roleId != 3) {
        //     this.$router.push("/login")
        // }
    }

}
</script>

<style scoped>
.el-main {
    height: 90vh !important;
    overflow-y: auto !important;
    padding: 0 50px 120px 50px;
    margin-top: 70px;
}

.welcome {
    font-size: 26px;
    margin: 20px 0 35px 0;
}
.el-carousel__item h3 {
    color: #475669;
    font-size: 14px;
    opacity: 0.75;
    line-height: 400px;
    margin: 0;
}

.el-header {
    /* padding: 0 20px; */
    /* box-sizing: border-box; */
    /* flex-shrink: 0; */
    background-color: #ffffff;
    height: 80px !important;
    z-index: 99 !important;
}

.el-carousel__item:nth-child(2n) {
    background-color: #99a9bf;
}

.el-carousel__item:nth-child(2n+1) {
    background-color: #d3dce6;
}

.el-submenu__title {
    font-size: 20px;
}
</style>