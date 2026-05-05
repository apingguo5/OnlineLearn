<template>
    <div>
        <div class="page-header">
            <h2><i class="el-icon-message"></i> 收件箱</h2>
            <p>接收学校系统通知、管理员公告、以及所有正在上课的班级通知</p>
        </div>

        <div class="toolbar">
            <div class="filter-tabs">
                <el-button :type="filterType === 'all' ? 'primary' : 'default'" size="small" @click="filterType = 'all'; loadMessages()">全部</el-button>
                <el-button :type="filterType === 'system' ? 'primary' : 'default'" size="small" @click="filterType = 'system'; loadMessages()">系统通知</el-button>
                <el-button :type="filterType === 'admin' ? 'primary' : 'default'" size="small" @click="filterType = 'admin'; loadMessages()">管理员</el-button>
                <el-button :type="filterType === 'class' ? 'primary' : 'default'" size="small" @click="filterType = 'class'; loadMessages()">班级通知</el-button>
            </div>
            <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="unread-badge">
                <el-button @click="markAllRead" :disabled="unreadCount === 0">全部标为已读</el-button>
            </el-badge>
        </div>

        <div class="message-list">
            <el-empty v-if="messages.length === 0" description="暂无消息"></el-empty>
            <el-card v-for="msg in messages" :key="msg.id" class="message-card" :class="{ 'unread': msg.status === 0 }" shadow="hover" @click.native="viewMessage(msg)">
                <div class="message-item">
                    <div class="message-icon" :class="getMsgIconClass(msg.type)">
                        <i :class="getMsgIcon(msg.type)"></i>
                    </div>
                    <div class="message-content">
                        <div class="message-header">
                            <h3>{{ msg.title || '未命名消息' }}</h3>
                            <el-tag size="mini" :type="getMsgTagType(msg.type)">{{ getMsgTypeName(msg.type) }}</el-tag>
                        </div>
                        <p class="message-body">{{ msg.content || '暂无内容' }}</p>
                        <p class="message-footer">
                            <span><i class="el-icon-time"></i> {{ msg.createTime || '未知时间' }}</span>
                            <span v-if="msg.senderName"><i class="el-icon-user"></i> {{ msg.senderName }}</span>
                        </p>
                    </div>
                    <div class="message-status">
                        <span v-if="msg.status === 0" class="unread-dot"></span>
                    </div>
                </div>
            </el-card>
        </div>
    </div>
</template>

<script>
import Cookies from 'js-cookie'
export default {
    name: "StudentInbox",
    data() {
        return {
            messages: [],
            filterType: 'all',
            unreadCount: 0
        }
    },
    created() {
        this.loadMessages()
    },
    methods: {
        loadMessages() {
            const params = { userId: Cookies.get('userId') }
            if (this.filterType !== 'all') {
                params.type = this.filterType
            }
            this.$axios.get('/notice/list', { params }).then(resp => {
                if (resp.data.code === 200) {
                    this.messages = resp.data.resultData || []
                    this.unreadCount = this.messages.filter(m => m.status === 0).length
                }
            }).catch(() => {
                this.messages = []
                this.unreadCount = 0
            })
        },
        getMsgIcon(type) {
            const icons = {
                system: 'el-icon-bell',
                admin: 'el-icon-s-custom',
                class: 'el-icon-s-home'
            }
            return icons[type] || 'el-icon-message'
        },
        getMsgIconClass(type) {
            const classes = {
                system: 'icon-system',
                admin: 'icon-admin',
                class: 'icon-class'
            }
            return classes[type] || ''
        },
        getMsgTagType(type) {
            const types = {
                system: 'danger',
                admin: 'warning',
                class: 'success'
            }
            return types[type] || 'info'
        },
        getMsgTypeName(type) {
            const names = {
                system: '系统通知',
                admin: '管理员',
                class: '班级通知'
            }
            return names[type] || '其他'
        },
        viewMessage(msg) {
            if (msg.status === 0) {
                this.$axios.post('/notice/read', { id: msg.id }).then(() => {
                    msg.status = 1
                    this.unreadCount = Math.max(0, this.unreadCount - 1)
                }).catch(() => {})
            }
            this.$alert(msg.content, msg.title, {
                confirmButtonText: '确定',
                dangerouslyUseHTMLString: true
            })
        },
        markAllRead() {
            this.$axios.post('/notice/readAll', {
                userId: Cookies.get('userId')
            }).then(resp => {
                if (resp.data.code === 200) {
                    this.messages.forEach(m => { m.status = 1 })
                    this.unreadCount = 0
                    this.$message.success('已全部标为已读')
                }
            }).catch(() => {
                this.$message.error('操作失败')
            })
        }
    }
}
</script>

<style scoped>
.page-header {
    padding: 20px 0;
    border-bottom: 2px solid #085e03;
    margin-bottom: 20px;
}
.page-header h2 {
    margin: 0;
    color: #303133;
    font-size: 24px;
}
.page-header p {
    margin: 8px 0 0;
    color: #909399;
    font-size: 14px;
}
.toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}
.filter-tabs {
    display: flex;
    gap: 10px;
}
.unread-badge {
    margin-right: 20px;
}
.message-list {
    display: grid;
    gap: 12px;
}
.message-card {
    cursor: pointer;
    border-radius: 8px;
    transition: all 0.2s;
}
.message-card:hover {
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}
.message-card.unread {
    border-left: 3px solid #085e03;
    background: #f0fdf0;
}
.message-item {
    display: flex;
    align-items: flex-start;
    gap: 15px;
}
.message-icon {
    width: 45px;
    height: 45px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 22px;
    flex-shrink: 0;
}
.message-icon.icon-system {
    background: #fce4ec;
    color: #c62828;
}
.message-icon.icon-admin {
    background: #fff3e0;
    color: #e65100;
}
.message-icon.icon-class {
    background: #e8f5e9;
    color: #2e7d32;
}
.message-content {
    flex: 1;
    min-width: 0;
}
.message-header {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 5px;
}
.message-header h3 {
    margin: 0;
    font-size: 16px;
    color: #303133;
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}
.message-body {
    color: #606266;
    font-size: 14px;
    margin: 5px 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}
.message-footer {
    color: #909399;
    font-size: 12px;
    margin: 5px 0 0;
}
.message-footer span {
    margin-right: 15px;
}
.message-status {
    flex-shrink: 0;
    padding-top: 12px;
}
.unread-dot {
    width: 8px;
    height: 8px;
    background: #085e03;
    border-radius: 50%;
    display: inline-block;
}
</style>