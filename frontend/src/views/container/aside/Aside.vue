<template>
    <div class="aside-container">
        <!-- ====== 用户信息区（紧凑） ====== -->
        <div class="user-profile">
            <div class="user-avatar">
                <img v-if="avatarUrl" :src="avatarUrl" class="avatar-img" alt="头像" />
                <i v-else class="el-icon-user-solid avatar-icon"></i>
            </div>
            <div class="user-info">
                <div class="user-name">{{ userName }}</div>
                <div class="user-role">教师</div>
            </div>
            <el-tooltip content="退出登录" placement="bottom">
                <span class="logout-btn" @click="logout">
                    <i class="el-icon-switch-button"></i>
                </span>
            </el-tooltip>
        </div>

        <el-divider class="profile-divider"></el-divider>

        <!-- ====== 导航菜单 ====== -->
        <div class="aside-menu">
            <el-menu :default-active="this.$route.path" background-color="#ffffff" text-color="#4a5568" :router="true"
                active-text-color="#4e6ef2" :unique-opened="true">

                <el-menu-item index="/teacherdashboard">
                    <i class="el-icon-s-data" style="font-size: 20px;"></i>
                    <span slot="title">概览工作台</span>
                </el-menu-item>

                <el-menu-item index="/teachercourselist">
                    <i class="el-icon-reading" style="font-size: 20px;"></i>
                    <span slot="title">课程管理</span>
                </el-menu-item>

                <el-menu-item index="/teacherclassmanagement">
                    <i class="el-icon-s-custom" style="font-size: 20px;"></i>
                    <span slot="title">班级与学生管理</span>
                </el-menu-item>

                <el-menu-item index="/teacherquestionbank">
                    <i class="el-icon-collection-tag" style="font-size: 20px;"></i>
                    <span slot="title">题库管理</span>
                </el-menu-item>

                <el-submenu index="/teacherassessment">
                    <template slot="title">
                        <i class="el-icon-edit-outline" style="font-size: 20px;"></i>
                        <span slot="title">作业与考试</span>
                    </template>
                    <el-menu-item index="/teacherassessment">
                        <i class="el-icon-document"></i>
                        <span slot="title">作业管理</span>
                    </el-menu-item>
                    <el-menu-item index="/teachergrading">
                        <i class="el-icon-reading"></i>
                        <span slot="title">批改管理</span>
                    </el-menu-item>
                </el-submenu>

                <el-menu-item index="/teachergradebook">
                    <i class="el-icon-trophy" style="font-size: 20px;"></i>
                    <span slot="title">成绩与报表</span>
                </el-menu-item>

                <el-menu-item index="/teachercommunication">
                    <i class="el-icon-chat-dot-round" style="font-size: 20px;"></i>
                    <span slot="title">互动与答疑</span>
                </el-menu-item>

                <el-menu-item index="/teacherpersonalinfo">
                    <i class="el-icon-user" style="font-size: 20px;"></i>
                    <span slot="title">个人信息</span>
                </el-menu-item>
            </el-menu>
        </div>
    </div>
</template>

<script>
import Cookies from "js-cookie";

export default {
    name: "Aside",
    data() {
        return {
            userName: '',
            avatarUrl: ''
        }
    },
    created() {
        this.userName = localStorage.getItem('userName') || '教师'
        this.loadAvatar()
    },
    methods: {
        loadAvatar() {
            const uid = Cookies.get('userId')
            if (uid) {
                this.avatarUrl = localStorage.getItem('user_avatar_' + uid) || ''
            }
        },
        logout() {
            this.$confirm('确定要退出登录吗？', '提示', { type: 'warning' }).then(() => {
                Cookies.remove('userId')
                Cookies.remove('classId')
                Cookies.remove('roleId')
                this.$router.push('/login')
                this.$message({ message: '退出成功', type: 'success' });
            }).catch(() => {})
        }
    }
}
</script>

<style scoped>
.aside-container {
    display: flex;
    flex-direction: column;
    height: 100%;
    background: #fff;
}

/* ====== 用户信息区 ====== */
.user-profile {
    display: flex;
    align-items: center;
    padding: 10px 14px;
    gap: 10px;
}

.user-avatar {
    width: 36px;
    height: 36px;
    border-radius: 50%;
    overflow: hidden;
    flex-shrink: 0;
    background: #f0f5ff;
    display: flex;
    align-items: center;
    justify-content: center;
}

.avatar-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.avatar-icon {
    font-size: 22px;
    color: #4e6ef2;
}

.user-info {
    flex: 1;
    min-width: 0;
    line-height: 1.3;
}

.user-name {
    font-size: 14px;
    font-weight: 600;
    color: #303133;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.user-role {
    font-size: 11px;
    color: #909399;
}

.logout-btn {
    flex-shrink: 0;
    width: 28px;
    height: 28px;
    border-radius: 6px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    color: #909399;
    font-size: 15px;
    transition: all 0.2s;
}

.logout-btn:hover {
    background: #fef0f0;
    color: #F56C6C;
}

.profile-divider {
    margin: 4px 14px;
}

/* ====== 菜单 ====== */
.aside-menu {
    flex: 1;
    overflow-y: auto;
}

.el-menu-item {
    font-size: 14px;
    cursor: pointer;
    color: #4a5568;
}

.el-menu-item.is-active {
    color: #4e6ef2 !important;
    background-color: #f0f5ff !important;
}

.el-menu-item:hover {
    background-color: #f5f7fa !important;
}

.el-submenu .el-menu {
    background-color: #f8fafc !important;
}

.el-submenu .el-menu .el-menu-item {
    background-color: #f8fafc !important;
    color: #5a6478 !important;
    font-size: 13px;
}

.el-submenu .el-menu .el-menu-item:hover,
.el-submenu .el-menu .el-menu-item.is-active {
    background-color: #eef2ff !important;
    color: #4e6ef2 !important;
}

.el-submenu .el-menu .el-menu-item i {
    color: #8e99b0 !important;
}
</style>
