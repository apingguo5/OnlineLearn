<template>
    <div>
        <el-container>
            <el-header>
                <Header></Header>
            </el-header>
            <el-container>
                <el-aside width="200px">
                    <StudentAside></StudentAside>
                </el-aside>
                <el-main>
                    <div style="font-size: 30px;font-weight: bold">
                        欢迎访问学生端，{{ Info.userName }} 同学
                    </div>
                    <hr>
                    <router-view></router-view>
                </el-main>
            </el-container>
        </el-container>
    </div>
</template>

<script>
import Header from "./header/Header";
import StudentAside from "../aside/StudentAside";
import {personal} from '../../../api/personal.js'
import Cookies from 'js-cookie'
export default {
    name: "StudentContainWeb",
    data() {
        return {
            Info: {}
        }
    },
    components: { Header, StudentAside },
    mounted() {
        personal({'id' : Cookies.get("userId")}).then(resp => {
            this.Info = resp.data.resultData
        })
    },
    updated() {
        var roleId = Cookies.get("roleId")
        if(roleId != 3) {
            this.$router.push("/login")
        }
    }
}
</script>

<style scoped>
.el-container.is-vertical {
    flex-direction: column;
    height: 100%;
}

.el-header {
    margin-left: -20px;
    margin-right: -20px;
    background-color: #0b5013;
    color: #333;
    text-align: center;
    line-height: 60px;
    border-bottom: 1px solid #0b5013;
}

.el-aside {
    background-color: #085e03;
    color: #333;
    text-align: center;
    line-height: 200px;
}

.el-main {
    background-color: rgba(233, 238, 243, 0.1);
    color: #333;
    overflow-y: auto;
    height: 90vh;
}

.welcome {
    font-size: 26px;
    margin: 20px 0 35px 0;
}

body>.el-container {
    margin-bottom: 40px;
}

.el-container:nth-child(5) .el-aside,
.el-container:nth-child(6) .el-aside {
    line-height: 260px;
}

.el-container:nth-child(7) .el-aside {
    line-height: 320px;
}
</style>
