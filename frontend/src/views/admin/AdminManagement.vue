<template>
    <div>
        <el-container>
            <el-header>
                <Header></Header>
            </el-header>
            <el-container>
                <el-aside width="200px">
                    <AdminAside></AdminAside>
                </el-aside>
                <el-main>
                    <div style="font-size: 30px;font-weight: bold">
                        欢迎登录管理员端
                    </div>
                    <hr>
                    <router-view></router-view>
                </el-main>
            </el-container>
        </el-container>
    </div>
</template>

<script>
import Header from "./header/Header.vue"
import AdminAside from "./aside/AdminAside";
import Cookies from 'js-cookie'
export default {
    name: "AdminManagement",
    components: { AdminAside, Header },
    updated() {
        var roleId = Cookies.get("roleId")
        if(roleId != 1) {
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

.el-header[data-v-9aae6930],
.el-footer[data-v-9aae6930] {
    margin-left: -20px;
    margin-right: -20px;
    background-color: #ffffff;
    color: #1a1a2e;
    text-align: center;
    line-height: 60px;
    border-bottom: 1px solid #e8ecf1;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.el-aside {
    background-color: #ffffff;
    color: #1a1a2e;
    text-align: center;
    line-height: 200px;
    border-right: 1px solid #eef0f4;
}

.aside[data-v-2c9fb0d2] {
    margin-top: 10px;
    background-color: #ffffff;
}

.welcome {
    font-size: 34px;
    font-weight: 600;
}

.el-main {
    background-color: #f5f7fa;
    color: #1a1a2e;
    overflow-y: auto;
    height: 90vh;
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
}</style>
