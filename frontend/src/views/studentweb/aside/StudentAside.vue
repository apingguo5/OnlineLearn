<template>
    <div class="student-aside-wrapper">
        <!-- ====== 用户信息区（紧凑） ====== -->
        <div class="user-profile">
            <div class="user-avatar">
                <img v-if="avatarUrl" :src="avatarUrl" class="avatar-img" alt="头像" />
                <i v-else class="el-icon-user-solid avatar-icon"></i>
            </div>
            <div class="user-info">
                <div class="user-name">{{ userName }}</div>
                <div class="user-role">学生</div>
            </div>
            <el-tooltip content="退出登录" placement="bottom">
                <span class="logout-btn" @click="logout">
                    <i class="el-icon-switch-button"></i>
                </span>
            </el-tooltip>
        </div>

        <el-divider class="profile-divider"></el-divider>

        <!-- ====== 导航菜单 ====== -->
        <el-menu
            :default-active="activeMenu"
            class="el-menu-vertical-demo"
            background-color="#ffffff"
            text-color="#4a5568"
            active-text-color="#4e6ef2"
            router
        >
            <el-menu-item index="/studenthome">
                <i class="el-icon-s-home"></i>
                <span slot="title">首页</span>
            </el-menu-item>

            <el-menu-item index="/courses">
                <i class="el-icon-notebook-2"></i>
                <span slot="title">正在学习</span>
            </el-menu-item>

            <el-menu-item index="/studenthomeworkexam">
                <i class="el-icon-edit-outline"></i>
                <span slot="title">作业考试</span>
            </el-menu-item>

            <el-menu-item index="/askandanswer">
                <i class="el-icon-chat-dot-round"></i>
                <span slot="title">问答社区</span>
            </el-menu-item>

            <el-menu-item index="/essentiainfo">
                <i class="el-icon-user"></i>
                <span slot="title">个人信息</span>
            </el-menu-item>
        </el-menu>
    </div>
</template>

<script>
import Cookies from "js-cookie";

export default {
    name: "StudentAside",
    data() {
        return {
            userName: '',
            avatarUrl: ''
        }
    },
    computed: {
        activeMenu() {
            return this.$route.path;
        }
    },
    created() {
        this.userName = localStorage.getItem('userName') || '同学'
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
.student-aside-wrapper {
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
    background: #f0f9eb;
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
    color: #67C23A;
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
.el-menu-vertical-demo {
    width: 200px;
    border-right: 1px solid #eef0f4;
    flex: 1;
    overflow-y: auto;
}

.el-menu-item {
    font-size: 14px;
    color: #4a5568;
}

.el-menu-item i {
    font-size: 18px;
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
