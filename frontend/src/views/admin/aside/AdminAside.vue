<template>
    <el-row class="aside">
        <el-col :span="24">
            <el-menu :default-active="this.$route.path" background-color="#09720e" text-color="#ffffff" :router="true"
                active-text-color="white">
                <el-menu-item index="/adminHome">
                    <i class="el-icon-s-home" style="font-size: 26px;color: white;"></i>
                    <span slot="title" style="font-size: 20px;"> 首页</span>
                </el-menu-item>
                <el-menu-item index="/adminstudentmanagement">
                    <i class="el-icon-user-solid" style="font-size: 26px;color: white;"></i>
                    <span slot="title" style="font-size: 20px;"> 学生管理</span>
                </el-menu-item>
                <el-menu-item index="/teachermanagement">
                    <i class="el-icon-user" style="font-size: 26px;color: white;"></i>
                    <span slot="title" style="font-size: 20px;"> 教师管理</span>
                </el-menu-item>
                <el-menu-item index="/classmanagementadmin">
                    <i class="el-icon-school" style="font-size: 26px;color: white;"></i>
                    <span slot="title" style="font-size: 20px;"> 班级管理</span>
                </el-menu-item>
                <el-menu-item index="/subject">
                    <i class="el-icon-collection" style="font-size: 26px;color: white;"></i>
                    <span slot="title" style="font-size: 20px;"> 科目管理</span>
                </el-menu-item>
                <el-menu-item index="/adminhomework">
                    <i class="el-icon-document" style="font-size: 26px;color: white;"></i>
                    <span slot="title" style="font-size: 20px;"> 作业管理</span>
                </el-menu-item>
                <el-menu-item index="/testmanagement">
                    <i class="el-icon-edit-outline" style="font-size: 26px;color: white;"></i>
                    <span slot="title" style="font-size: 20px;"> 试题管理</span>
                </el-menu-item>
                <el-menu-item index="/knowledgepoints">
                    <i class="el-icon-lightbulb" style="font-size: 26px;color: white;"></i>
                    <span slot="title" style="font-size: 20px;"> 知识点管理</span>
                </el-menu-item>
                <el-menu-item index="/adminpersonalinfo">
                    <i class="el-icon-setting" style="font-size: 26px;color: white;"></i>
                    <span slot="title" style="font-size: 20px;"> 个人信息</span>
                </el-menu-item>
            </el-menu>
        </el-col>
    </el-row>
</template>

<script>
export default {
    name: "AdminAside"
}
</script>

<style scoped>
.el-aside {
    text-align: left;
    background-color: #09720e;
}

.aside {
    margin-top: 0;
    background-color: #09720e;
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