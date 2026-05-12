<template>
    <el-row class="aside">
        <el-col :span="24">
            <el-menu :default-active="$route.path" background-color="#085e03" text-color="#fff" :router="true"
                active-text-color="#fff">
                <el-menu-item index="/studenthome">
                    <i class="el-icon-s-home menu-icon"></i>
                    <span slot="title" class="menu-text"> 首页</span>
                </el-menu-item>
                <el-menu-item index="/studentcourses">
                    <i class="el-icon-reading menu-icon"></i>
                    <span slot="title" class="menu-text"> 课程列表</span>
                </el-menu-item>
                <el-menu-item index="/studentpractice">
                    <i class="el-icon-edit-outline menu-icon"></i>
                    <span slot="title" class="menu-text"> 作业习题</span>
                </el-menu-item>
                <el-menu-item index="/askandanswer">
                    <i class="el-icon-chat-dot-round menu-icon"></i>
                    <span slot="title" class="menu-text"> 问答社区</span>
                </el-menu-item>
                <el-menu-item index="/studentnotes">
                    <i class="el-icon-notebook-2 menu-icon"></i>
                    <span slot="title" class="menu-text"> 我的笔记</span>
                </el-menu-item>
                <el-menu-item index="/studentinbox">
                    <i class="el-icon-message menu-icon"></i>
                    <span slot="title" class="menu-text"> 收件箱</span>
                </el-menu-item>
                <el-menu-item index="/essentiainfo">
                    <i class="el-icon-user menu-icon"></i>
                    <span slot="title" class="menu-text"> 个人信息</span>
                </el-menu-item>
                <el-menu-item index="/watchtime">
                    <i class="el-icon-time menu-icon"></i>
                    <span slot="title" class="menu-text"> 学习统计</span>
                </el-menu-item>
            </el-menu>
        </el-col>
    </el-row>
</template>

<script>
export default {
    name: "StudentAside"
}
</script>

<style scoped>
.menu-icon {
    font-size: 22px;
    color: #fff;
}
.menu-text {
    font-size: 16px;
}
.el-aside {
    text-align: left;
    background-color: #085e03;
}
.aside {
    margin-top: 0;
    background-color: #085e03;
    text-align: left;
}
.el-menu-item {
    font-size: 14px;
    cursor: pointer;
    box-sizing: border-box;
}
.el-menu-item,
.el-submenu__title {
    position: relative;
    box-sizing: border-box;
    white-space: nowrap;
    list-style: none;
}
</style>
