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
                    <router-view></router-view>
                </el-main>
            </el-container>
        </el-container>
    </div>
</template>

<script>
import Header from "./header/Header";
import StudentAside from "../aside/StudentAside";
export default {
    name: "StudentContainWeb",
    components: { Header, StudentAside }
}
</script>

<style scoped>
.el-header {
    margin-left: -20px;
    margin-right: -20px;
    background-color: #0b5013;
    color: #333;
    text-align: center;
    line-height: 60px;
    border-bottom: 1px solid #0b5013;
}
.el-main {
    background-color: rgba(233, 238, 243, 0.1);
    color: #333;
    overflow-y: auto;
    height: 90vh;
}
</style>