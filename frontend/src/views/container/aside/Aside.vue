<template>
    <el-row class="aside">
        <el-col :span="24">
            <el-menu :default-active="this.$route.path" background-color="#157d08" text-color="#ffffff" :router="true"
                active-text-color="white">
                <el-menu-item index="/teacherdashboard">
                    <i class="el-icon-s-custom" style="font-size: 26px;color: white;"></i>
                    <span slot="title" style="font-size: 20px;"> 教师工作台</span>
                </el-menu-item>
                <el-menu-item index="/studentinfo">
                    <i class="el-icon-s-grid" style="font-size: 26px;color: white;"></i>
                    <span slot="title" style="font-size: 20px;"> 班级学生管理</span>
                </el-menu-item>
                <el-menu-item index="/classmanagement">
                    <i class="el-icon-edit-outline" style="font-size: 26px;color: white;"></i>
                    <span slot="title" style="font-size: 20px;"> 作业管理</span>
                </el-menu-item>
                <el-menu-item index="/scoremanagment">
                    <i class="el-icon-document" style="font-size: 26px;color: white;"></i>
                    <span slot="title" style="font-size: 20px;"> 试题管理</span>
                </el-menu-item>
                <el-menu-item index="/teacherapplicant">
                    <i class="el-icon-s-check" style="font-size: 26px;color: white;"></i>
                    <span slot="title" style="font-size: 20px;"> 加入班级审核</span>
                </el-menu-item>
                <el-menu-item index="/teacheraskandanswer">
                    <i class="el-icon-chat-dot-round" style="font-size: 28px;color: white;"></i>
                    <span slot="title" style="font-size: 20px;"> 问答社区</span>
                </el-menu-item>
                <el-menu-item index="/video">
                    <i class="el-icon-video-camera" style="font-size: 26px;color: white;"></i>
                    <span slot="title" style="font-size: 20px;"> 视频管理</span>
                </el-menu-item>
                <el-menu-item index="/personalinfo">
                    <i class="el-icon-collection" style="font-size: 26px;color: white;"></i>
                    <span slot="title" style="font-size: 20px;"> 知识点补充</span>
                </el-menu-item>
                <el-menu-item index="/teacherpersonalinfo">
                    <i class="el-icon-user" style="font-size: 26px;color: white;"></i>
                    <span slot="title" style="font-size: 20px;"> 个人信息</span>
                </el-menu-item>
                <el-menu-item index="/coursemanagement">
                    <i class="el-icon-reading" style="font-size: 26px;color: white;"></i>
                    <span slot="title" style="font-size: 20px;"> 课程管理</span>
                </el-menu-item>
                <el-menu-item index="/noticemanagement">
                    <i class="el-icon-bell" style="font-size: 26px;color: white;"></i>
                    <span slot="title" style="font-size: 20px;"> 公告管理</span>
                </el-menu-item>
            </el-menu>
        </el-col>
    </el-row>
</template>

<script>
export default {
    name: "Aside"
}
</script>

<style scoped>
.el-aside {
    text-align: left;
    background-color: #157d08;
}

.aside {
    margin-top: 0;
    background-color: #157d08;
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