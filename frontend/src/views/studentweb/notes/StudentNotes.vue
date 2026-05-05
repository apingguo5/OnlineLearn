<template>
    <div>
        <div class="page-header">
            <h2><i class="el-icon-notebook-2"></i> 我的笔记</h2>
            <p>管理您自己上传的学习笔记</p>
        </div>

        <div class="toolbar">
            <el-button type="primary" @click="$router.push('/markdown')">
                <i class="el-icon-edit"></i> 新建笔记
            </el-button>
            <el-button @click="loadNotes">
                <i class="el-icon-refresh"></i> 刷新
            </el-button>
        </div>

        <div class="notes-list">
            <el-empty v-if="notes.length === 0" description="暂无笔记，点击上方按钮创建"></el-empty>
            <el-card v-for="note in notes" :key="note.id" class="note-card" shadow="hover">
                <div class="note-item">
                    <div class="note-icon">
                        <i class="el-icon-document"></i>
                    </div>
                    <div class="note-content">
                        <h3>{{ note.title || '未命名笔记' }}</h3>
                        <p class="note-desc">{{ note.summary || '暂无描述' }}</p>
                        <p class="note-meta">
                            <span><i class="el-icon-time"></i> {{ note.createTime || '未知时间' }}</span>
                            <span v-if="note.subjectName"><i class="el-icon-reading"></i> {{ note.subjectName }}</span>
                        </p>
                    </div>
                    <div class="note-actions">
                        <el-button type="primary" size="small" @click="viewNote(note)">查看</el-button>
                        <el-button type="danger" size="small" @click="deleteNote(note)">删除</el-button>
                    </div>
                </div>
            </el-card>
        </div>
    </div>
</template>

<script>
import Cookies from 'js-cookie'
export default {
    name: "StudentNotes",
    data() {
        return {
            notes: []
        }
    },
    created() {
        this.loadNotes()
    },
    methods: {
        loadNotes() {
            this.$axios.get('/note/list', {
                params: { userId: Cookies.get('userId') }
            }).then(resp => {
                if (resp.data.code === 200) {
                    this.notes = resp.data.resultData || []
                }
            }).catch(() => {
                this.notes = []
            })
        },
        viewNote(note) {
            this.$router.push({
                path: '/markdown',
                params: { homework: note, type: 'note' }
            })
        },
        deleteNote(note) {
            this.$confirm('确定删除该笔记吗？', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                this.$axios.post('/note/delete', { id: note.id }).then(resp => {
                    if (resp.data.code === 200) {
                        this.$message.success('删除成功')
                        this.loadNotes()
                    }
                }).catch(() => {
                    this.$message.error('删除失败')
                })
            }).catch(() => {})
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
.notes-list {
    display: grid;
    gap: 15px;
}
.note-card {
    border-radius: 8px;
}
.note-item {
    display: flex;
    align-items: flex-start;
    gap: 20px;
}
.note-icon {
    width: 50px;
    height: 50px;
    background: #fff3e0;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    color: #e65100;
    flex-shrink: 0;
}
.note-content {
    flex: 1;
}
.note-content h3 {
    margin: 0 0 5px;
    font-size: 18px;
    color: #303133;
}
.note-desc {
    color: #606266;
    font-size: 14px;
    margin: 5px 0;
}
.note-meta {
    color: #909399;
    font-size: 12px;
    margin: 5px 0 0;
}
.note-meta span {
    margin-right: 15px;
}
.note-actions {
    display: flex;
    gap: 10px;
    flex-shrink: 0;
}
</style>