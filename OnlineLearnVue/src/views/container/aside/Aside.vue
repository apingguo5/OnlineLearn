<template>
    <el-row class="aside">
        <el-col :span="24">
            <el-menu :default-active="this.$route.path" background-color="#157d08" text-color="#ffffff" :router="true"
                active-text-color="white">
                <el-menu-item index="/teacherHome">
                    <i class="el-icon-menu" style="font-size: 26px;color: white;"></i>
                    <span slot="title" style="font-size: 20px;"> 首页</span>
                </el-menu-item>
                <el-menu-item index="/studentinfo">
                    <i class="el-icon-menu" style="font-size: 26px;color: white;"></i>
                    <span slot="title" style="font-size: 20px;"> 班级信息管理</span>
                </el-menu-item>
                <el-menu-item index="/teacherapplicant">
                    <i class="el-icon-menu" style="font-size: 26px;color: white;"></i>
                    <span slot="title" style="font-size: 20px;"> 加入班级审核</span>
                </el-menu-item>
                <el-menu-item index="/scoremanagment">
                    <i class="el-icon-menu" style="font-size: 26px;color: white;"></i>
                    <span slot="title" style="font-size: 20px;"> 试题管理</span>
                </el-menu-item>
                <el-menu-item index="/classmanagement">
                    <i class="el-icon-menu" style="font-size: 26px;color: white;"></i>
                    <span slot="title" style="font-size: 20px;"> 作业管理</span>
                </el-menu-item>
                <el-menu-item index="/teacheraskandanswer">
                    <i class="el-icon-menu" style="font-size: 28px;color: white;"></i>
                    <span slot="title" style="font-size: 20px;"> 问答社区</span>
                </el-menu-item>
                <el-menu-item index="/personalinfo">
                    <i class="el-icon-menu" style="font-size: 26px;color: white;"></i>
                    <span slot="title" style="font-size: 20px;"> 知识点补充</span>
                </el-menu-item>
                <el-menu-item index="/video">
                    <i class="el-icon-menu" style="font-size: 26px;color: white;"></i>
                    <span slot="title" style="font-size: 20px;"> 视频管理</span>
                </el-menu-item>
                <el-menu-item index="/teacherpersonalinfo">
                    <i class="el-icon-menu" style="font-size: 26px;color: white;"></i>
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
    background-color: #157d08;

}

.aside {
    margin-top: 0;
    background-color: #157d08;
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
