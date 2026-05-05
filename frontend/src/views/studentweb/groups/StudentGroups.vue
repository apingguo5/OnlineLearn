<template>
    <div>
        <div class="page-header">
            <h2><i class="el-icon-s-grid"></i> 群组</h2>
            <p>查看和管理您加入的学习群组</p>
        </div>

        <div class="toolbar">
            <el-button type="primary" @click="showCreateDialog = true">
                <i class="el-icon-plus"></i> 创建群组
            </el-button>
            <el-button @click="loadGroups">
                <i class="el-icon-refresh"></i> 刷新
            </el-button>
        </div>

        <div class="group-list">
            <el-empty v-if="groups.length === 0" description="暂无群组数据"></el-empty>
            <el-card v-for="group in groups" :key="group.id" class="group-card" shadow="hover">
                <div class="group-item">
                    <div class="group-avatar">
                        <i class="el-icon-s-grid"></i>
                    </div>
                    <div class="group-info">
                        <h3>{{ group.groupName || '未命名群组' }}</h3>
                        <p><i class="el-icon-user"></i> 成员：{{ group.memberCount || 0 }}人</p>
                        <p class="group-desc">{{ group.description || '暂无描述' }}</p>
                    </div>
                    <div class="group-actions">
                        <el-button type="success" size="small" @click="enterGroup(group)">进入</el-button>
                        <el-button type="danger" size="small" @click="leaveGroup(group)">退出</el-button>
                    </div>
                </div>
            </el-card>
        </div>

        <el-dialog title="创建群组" :visible.sync="showCreateDialog" width="400px">
            <el-form :model="newGroup">
                <el-form-item label="群组名称">
                    <el-input v-model="newGroup.groupName" placeholder="请输入群组名称"></el-input>
                </el-form-item>
                <el-form-item label="群组描述">
                    <el-input type="textarea" v-model="newGroup.description" placeholder="请输入群组描述"></el-input>
                </el-form-item>
            </el-form>
            <span slot="footer">
                <el-button @click="showCreateDialog = false">取消</el-button>
                <el-button type="primary" @click="createGroup">创建</el-button>
            </span>
        </el-dialog>
    </div>
</template>

<script>
import Cookies from 'js-cookie'
export default {
    name: "StudentGroups",
    data() {
        return {
            groups: [],
            showCreateDialog: false,
            newGroup: {
                groupName: '',
                description: ''
            }
        }
    },
    created() {
        this.loadGroups()
    },
    methods: {
        loadGroups() {
            this.$axios.get('/group/list', {
                params: { userId: Cookies.get('userId') }
            }).then(resp => {
                if (resp.data.code === 200) {
                    this.groups = resp.data.resultData || []
                }
            }).catch(() => {
                this.groups = []
            })
        },
        enterGroup(group) {
            this.$message.success('进入群组：' + (group.groupName || group.id))
        },
        leaveGroup(group) {
            this.$confirm('确定退出该群组吗？', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                this.$axios.post('/group/leave', {
                    groupId: group.id,
                    userId: Cookies.get('userId')
                }).then(resp => {
                    if (resp.data.code === 200) {
                        this.$message.success('已退出群组')
                        this.loadGroups()
                    }
                }).catch(() => {
                    this.$message.error('退出群组失败')
                })
            }).catch(() => {})
        },
        createGroup() {
            if (!this.newGroup.groupName.trim()) {
                this.$message.error('请输入群组名称')
                return
            }
            this.$axios.post('/group/create', {
                ...this.newGroup,
                creatorId: Cookies.get('userId')
            }).then(resp => {
                if (resp.data.code === 200) {
                    this.$message.success('群组创建成功')
                    this.showCreateDialog = false
                    this.newGroup = { groupName: '', description: '' }
                    this.loadGroups()
                }
            }).catch(() => {
                this.$message.error('创建群组失败')
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
    margin-bottom: 20px;
}
.group-list {
    display: grid;
    gap: 15px;
}
.group-card {
    border-radius: 8px;
}
.group-item {
    display: flex;
    align-items: center;
    gap: 20px;
}
.group-avatar {
    width: 60px;
    height: 60px;
    background: #e8f5e9;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 30px;
    color: #085e03;
    flex-shrink: 0;
}
.group-info {
    flex: 1;
}
.group-info h3 {
    margin: 0 0 5px;
    font-size: 18px;
    color: #303133;
}
.group-info p {
    margin: 2px 0;
    color: #909399;
    font-size: 13px;
}
.group-desc {
    color: #606266 !important;
}
.group-actions {
    display: flex;
    gap: 10px;
    flex-shrink: 0;
}
</style>