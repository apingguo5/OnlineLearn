<template>
    <el-row class="aside">
        <el-col :span="24">
            <el-menu :default-active="this.$route.path" background-color="#085e03" text-color="#ffffff" :router="true"
                active-text-color="white">
                <el-menu-item index="/studenthome">
                    <i class="el-icon-s-home" style="font-size: 26px;color: white;"></i>
                    <span slot="title" style="font-size: 20px;"> 首页</span>
                </el-menu-item>
                <el-menu-item index="/studentcourses">
                    <i class="el-icon-reading" style="font-size: 26px;color: white;"></i>
                    <span slot="title" style="font-size: 20px;"> 课程列表（正在学的）</span>
                </el-menu-item>
                <el-submenu index="1">
                    <template slot="title">
                        <i class="el-icon-edit-outline" style="font-size: 26px;color: white;"></i>
                        <span style="font-size: 20px;"> 作业习题</span>
                    </template>
                    <el-menu-item index="/studentpractice">
                        <i class="el-icon-document" style="font-size: 22px;color: white;"></i>
                        <span slot="title"> 试题练习</span>
                    </el-menu-item>
                    <el-menu-item index="/checkhomework">
                        <i class="el-icon-tickets" style="font-size: 22px;color: white;"></i>
                        <span slot="title"> 作业管理</span>
                    </el-menu-item>
                </el-submenu>
                <el-menu-item index="/askandanswer">
                    <i class="el-icon-chat-dot-round" style="font-size: 26px;color: white;"></i>
                    <span slot="title" style="font-size: 20px;"> 问答社区</span>
                </el-menu-item>
                <el-submenu index="2">
                    <template slot="title">
                        <i class="el-icon-document-copy" style="font-size: 26px;color: white;"></i>
                        <span style="font-size: 20px;"> 笔记</span>
                    </template>
                    <el-menu-item index="/studentnotes">
                        <i class="el-icon-notebook-2" style="font-size: 22px;color: white;"></i>
                        <span slot="title"> 我的笔记</span>
                    </el-menu-item>
                    <el-menu-item index="/markdown">
                        <i class="el-icon-edit" style="font-size: 22px;color: white;"></i>
                        <span slot="title"> 新建笔记</span>
                    </el-menu-item>
                </el-submenu>
                <el-menu-item index="/studentinbox">
                    <i class="el-icon-message" style="font-size: 26px;color: white;"></i>
                    <span slot="title" style="font-size: 20px;"> 收件箱</span>
                </el-menu-item>
                <el-menu-item index="/essentiainfo">
                    <i class="el-icon-user" style="font-size: 26px;color: white;"></i>
                    <span slot="title" style="font-size: 20px;"> 个人信息</span>
                </el-menu-item>
                <el-menu-item index="/watchtime">
                    <i class="el-icon-time" style="font-size: 26px;color: white;"></i>
                    <span slot="title" style="font-size: 20px;"> 学习统计</span>
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
    -webkit-box-sizing: border-box;
    white-space: nowrap;
    list-style: none;
}
</style>