<template>
    <el-row class="aside">
        <el-col :span="24">
            <el-menu :default-active="this.$route.path" background-color="#157d08" text-color="#ffffff" :router="true"
                active-text-color="#ffd04b" :unique-opened="true">

                <!-- ========== 1. 概览工作台 ========== -->
                <el-menu-item index="/teacherdashboard">
                    <i class="el-icon-s-data" style="font-size: 22px;color: white;"></i>
                    <span slot="title" style="font-size: 16px;"> 概览工作台</span>
                </el-menu-item>

                <!-- ========== 2. 课程管理 ========== -->
                <el-submenu index="course-mgmt">
                    <template slot="title">
                        <i class="el-icon-reading" style="font-size: 22px;color: white;"></i>
                        <span style="font-size: 16px;"> 课程管理</span>
                    </template>
                    <el-menu-item index="/teachercoursemanagement">
                        <i class="el-icon-menu"></i>
                        <span slot="title">课程列表</span>
                    </el-menu-item>
                    <el-menu-item index="/teachercoursemanagement">
                        <i class="el-icon-s-management"></i>
                        <span slot="title">章节目录</span>
                    </el-menu-item>
                    <el-menu-item index="/teachercoursemanagement">
                        <i class="el-icon-files"></i>
                        <span slot="title">课件资源库</span>
                    </el-menu-item>
                </el-submenu>

                <!-- ========== 3. 班级与学生管理 ========== -->
                <el-submenu index="class-student-mgmt">
                    <template slot="title">
                        <i class="el-icon-s-custom" style="font-size: 22px;color: white;"></i>
                        <span style="font-size: 16px;"> 班级与学生管理</span>
                    </template>
                    <el-menu-item index="/teacherclassmanagement">
                        <i class="el-icon-s-grid"></i>
                        <span slot="title">班级列表</span>
                    </el-menu-item>
                    <el-menu-item index="/teacherclassmanagement">
                        <i class="el-icon-user-solid"></i>
                        <span slot="title">学生管理</span>
                    </el-menu-item>
                    <el-menu-item index="/teacherclassmanagement">
                        <i class="el-icon-monitor"></i>
                        <span slot="title">学情监控</span>
                    </el-menu-item>
                </el-submenu>

                <!-- ========== 4. 作业与考试 ========== -->
                <el-submenu index="assessment">
                    <template slot="title">
                        <i class="el-icon-edit-outline" style="font-size: 22px;color: white;"></i>
                        <span style="font-size: 16px;"> 作业与考试</span>
                    </template>
                    <el-menu-item index="/teacherassessment">
                        <i class="el-icon-collection"></i>
                        <span slot="title">题库管理</span>
                    </el-menu-item>
                    <el-menu-item index="/teacherassessment">
                        <i class="el-icon-upload"></i>
                        <span slot="title">发布任务</span>
                    </el-menu-item>
                    <el-menu-item index="/teacherassessment">
                        <i class="el-icon-document-checked"></i>
                        <span slot="title">批改与反馈</span>
                    </el-menu-item>
                </el-submenu>

                <!-- ========== 5. 成绩与报表 ========== -->
                <el-submenu index="gradebook">
                    <template slot="title">
                        <i class="el-icon-trophy" style="font-size: 22px;color: white;"></i>
                        <span style="font-size: 16px;"> 成绩与报表</span>
                    </template>
                    <el-menu-item index="/teachergradebook">
                        <i class="el-icon-setting"></i>
                        <span slot="title">权重设置</span>
                    </el-menu-item>
                    <el-menu-item index="/teachergradebook">
                        <i class="el-icon-document"></i>
                        <span slot="title">成绩统计</span>
                    </el-menu-item>
                    <el-menu-item index="/teachergradebook">
                        <i class="el-icon-download"></i>
                        <span slot="title">报表导出</span>
                    </el-menu-item>
                </el-submenu>

                <!-- ========== 6. 互动与答疑 ========== -->
                <el-submenu index="communication">
                    <template slot="title">
                        <i class="el-icon-chat-dot-round" style="font-size: 22px;color: white;"></i>
                        <span style="font-size: 16px;"> 互动与答疑</span>
                    </template>
                    <el-menu-item index="/teachercommunication">
                        <i class="el-icon-bell"></i>
                        <span slot="title">通知公告</span>
                    </el-menu-item>
                    <el-menu-item index="/teachercommunication">
                        <i class="el-icon-question"></i>
                        <span slot="title">问答讨论</span>
                    </el-menu-item>
                </el-submenu>

                <!-- ========== 7. 个人信息 ========== -->
                <el-menu-item index="/teacherpersonalinfo">
                    <i class="el-icon-user" style="font-size: 22px;color: white;"></i>
                    <span slot="title" style="font-size: 16px;"> 个人信息</span>
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

.el-submenu .el-menu {
    background-color: #0f5e06 !important;
}

.el-submenu .el-menu .el-menu-item {
    background-color: #0f5e06 !important;
    color: #e0e0e0 !important;
    font-size: 14px;
}

.el-submenu .el-menu .el-menu-item:hover,
.el-submenu .el-menu .el-menu-item.is-active {
    background-color: #1a8a0a !important;
    color: #ffffff !important;
}

.el-submenu .el-menu .el-menu-item i {
    color: #b8d9b3 !important;
}
</style>