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
    background-color: #ffffff;
    color: #1a1a2e;
    text-align: center;
    line-height: 60px;
    border-bottom: 1px solid #e8ecf1;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}
.el-main {
    background-color: #f5f7fa;
    color: #1a1a2e;
    overflow-y: auto;
    height: 90vh;
}
</style>