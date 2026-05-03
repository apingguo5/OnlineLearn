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
                    <span slot="title" style="font-size: 20px;"> 学生信息管理</span>
                </el-menu-item>
                <el-menu-item index="/teachermanagement">
                    <i class="el-icon-s-custom" style="font-size: 24px;color: white;"></i>
                    <span slot="title" style="font-size: 20px;"> 教师信息管理</span>
                </el-menu-item>
                <el-menu-item index="/adminhomework">
                    <i class="el-icon-s-order" style="font-size: 24px;color: white;"></i>
                    <span slot="title" style="font-size: 20px;"> 作业信息管理</span>
                </el-menu-item>
                <el-menu-item index="/subject">
                    <i class="el-icon-menu" style="font-size: 24px;color: white;"></i>
                    <span slot="title" style="font-size: 20px;"> 科目信息管理</span>
                </el-menu-item>
                <el-menu-item index="/classmanagementadmin">
                    <i class="el-icon-s-grid" style="font-size: 24px;color: white;"></i>
                    <span slot="title" style="font-size: 20px;"> 班级信息管理</span>
                </el-menu-item>
                <el-menu-item index="/testmanagement">
                    <i class="el-icon-s-opportunity" style="font-size: 24px;color: white;"></i>
                    <span slot="title" style="font-size: 20px;"> 试题信息管理</span>
                </el-menu-item>
                <el-menu-item index="/knowledgepoints">
                    <i class="el-icon-s-release" style="font-size: 24px;color: white;"></i>
                    <span slot="title" style="font-size: 20px;"> 知识点管理</span>
                </el-menu-item>
                <el-menu-item index="/adminpersonalinfo">
                    <i class="el-icon-user" style="font-size: 24px;color: white;"></i>
                    <span slot="title" style="font-size: 20px;"> 个人信息</span>
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
    /*background-color: #;*/
    text-align: left;
    background-color: #09720e;

}

.aside {
    /* margin-top: 10px; */
    background-color: #09720e;
    text-align: left;
}

.el-menu-item {
    font-size: 14px;
    /* padding: 0 20px; */
    cursor: pointer;
    /* transition: border-color .3s,background-color .3s,color .3s; */
    box-sizing: border-box;
}

.el-menu-item,
.el-submenu__title {
    /* height: 90px; */
    /* line-height:90px; */
    position: relative;
    -webkit-box-sizing: border-box;
    white-space: nowrap;
    list-style: none;
}
</style>
