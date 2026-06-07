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
                <div class="user-role">管理员</div>
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

                <el-menu-item index="/adminHome">
                    <i class="el-icon-s-home" style="font-size: 20px;"></i>
                    <span slot="title">首页</span>
                </el-menu-item>

                <el-menu-item index="/adminstudentmanagement">
                    <i class="el-icon-user-solid" style="font-size: 20px;"></i>
                    <span slot="title">学生管理</span>
                </el-menu-item>

                <el-menu-item index="/teachermanagement">
                    <i class="el-icon-user" style="font-size: 20px;"></i>
                    <span slot="title">教师管理</span>
                </el-menu-item>
            </el-menu>
        </div>
    </div>
</template>

<script>
import Cookies from "js-cookie";

export default {
    name: "AdminAside",
    data() {
        return {
            userName: '',
            avatarUrl: ''
        }
    },
    created() {
        this.userName = localStorage.getItem('userName') || '管理员'
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
    background: #fef5e7;
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
    color: #E6A23C;
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

.el-menu {
    border-right: none;
}

.el-menu-item {
    font-size: 14px;
    cursor: pointer;
    box-sizing: border-box;
    color: #4a5568;
}

.el-menu-item i {
    margin-right: 5px;
}

.el-menu-item.is-active {
    color: #4e6ef2 !important;
    background-color: #f0f5ff !important;
}

.el-menu-item:hover {
    background-color: #f5f7fa !important;
}
</style>
