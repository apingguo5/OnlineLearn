<template>
    <el-container>
        <el-header>
            <Header></Header>
        </el-header>
        <el-container>
            <el-aside id="tc-el-aside" width="200px">
                <Aside></Aside>
            </el-aside>
            <el-main>
                <div style="font-size: 30px;font-weight: bold" >
                     欢迎登录教师端
                </div>
                <hr>
                <router-view>

                </router-view>
            </el-main>
        </el-container>
    </el-container>
</template>

<script>
import Header from "./header/Header"
import Aside from "./aside/Aside";
import Cookies from 'js-cookie'
export default {
    name: "StudentContainer",
    components: { Aside, Header },
    updated() {
        var roleId = Cookies.get("roleId")
        if(roleId != 2) {
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

.el-header,
.el-footer {
    margin-left: -20px;
    margin-right: -20px;
    background-color: #0b5013;
    color: #333;
    text-align: center;
    line-height: 60px;

    border-bottom: 1px solid #0b5013;
}

#tc-el-aside {
    background-color: #157d08;
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
    font-size: 34px;
    font-weight: 600;
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
