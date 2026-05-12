<template>
    <div>
        <div class="page-header">
            <h2><i class="el-icon-notebook-2"></i> 我的笔记</h2>
            <p>记录和整理学习过程中的重要知识点</p>
        </div>

        <div class="toolbar">
            <el-input
                placeholder="搜索笔记..."
                v-model="searchKeyword"
                size="small"
                class="search-input"
                @keyup.enter.native="searchNotes"
            >
                <i slot="prefix" class="el-icon-search"></i>
            </el-input>
            <el-button type="primary" size="small" @click="showAddDialog">
                <i class="el-icon-plus"></i> 新建笔记
            </el-button>
        </div>

        <div class="notes-grid">
            <el-empty v-if="notes.length === 0" description="暂无笔记"></el-empty>
            <el-card v-for="note in filteredNotes" :key="note.id" class="note-card" shadow="hover">
                <div class="note-header">
                    <h3 class="note-title">{{ note.title || '未命名笔记' }}</h3>
                    <el-tag v-if="note.category" size="mini">{{ note.category }}</el-tag>
                </div>
                <p class="note-preview">{{ note.content || '暂无内容' }}</p>
                <div class="note-footer">
                    <span class="note-time"><i class="el-icon-time"></i> {{ note.createTime || '' }}</span>
                    <div class="note-actions">
                        <el-button type="text" size="mini" @click="editNote(note)">编辑</el-button>
                        <el-button type="text" size="mini" style="color: #f56c6c;" @click="confirmDelete(note)">删除</el-button>
                    </div>
                </div>
            </el-card>
        </div>

        <!-- 新建/编辑笔记对话框 -->
        <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="600px">
            <el-form :model="noteForm" label-width="80px">
                <el-form-item label="标题" required>
                    <el-input v-model="noteForm.title" placeholder="请输入笔记标题"></el-input>
                </el-form-item>
                <el-form-item label="分类">
                    <el-input v-model="noteForm.category" placeholder="例如: 数学、英语"></el-input>
                </el-form-item>
                <el-form-item label="内容">
                    <el-input
                        type="textarea"
                        :rows="8"
                        v-model="noteForm.content"
                        placeholder="请输入笔记内容"
                    ></el-input>
                </el-form-item>
            </el-form>
            <span slot="footer">
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button type="primary" @click="saveNote">保存</el-button>
            </span>
        </el-dialog>
    </div>
</template>

<script>
import { getNoteList, saveNote, updateNote, deleteNote } from '../../../api/studentweb/notes'
import Cookies from 'js-cookie'
export default {
    name: "StudentNotes",
    data() {
        return {
            notes: [],
            searchKeyword: '',
            dialogVisible: false,
            isEdit: false,
            editingId: null,
            noteForm: {
                title: '',
                category: '',
                content: ''
            }
        }
    },
    computed: {
        filteredNotes() {
            if (!this.searchKeyword) return this.notes
            const kw = this.searchKeyword.toLowerCase()
            return this.notes.filter(n =>
                (n.title && n.title.toLowerCase().includes(kw)) ||
                (n.content && n.content.toLowerCase().includes(kw))
            )
        },
        dialogTitle() {
            return this.isEdit ? '编辑笔记' : '新建笔记'
        }
    },
    created() {
        this.loadNotes()
    },
    methods: {
        loadNotes() {
            getNoteList({ userId: Cookies.get('userId') }).then(resp => {
                if (resp.data && resp.data.code === 200) {
                    this.notes = resp.data.resultData || []
                }
            }).catch(() => {
                this.$message.error('获取笔记列表失败')
            })
        },
        searchNotes() {
            // computed filter handles this
        },
        showAddDialog() {
            this.isEdit = false
            this.editingId = null
            this.noteForm = { title: '', category: '', content: '' }
            this.dialogVisible = true
        },
        editNote(note) {
            this.isEdit = true
            this.editingId = note.id
            this.noteForm = {
                title: note.title || '',
                category: note.category || '',
                content: note.content || ''
            }
            this.dialogVisible = true
        },
        saveNote() {
            if (!this.noteForm.title.trim()) {
                this.$message.warning('请输入笔记标题')
                return
            }
            const data = {
                ...this.noteForm,
                userId: Cookies.get('userId')
            }
            if (this.isEdit) {
                data.id = this.editingId
                updateNote(data).then(resp => {
                    if (resp.data && resp.data.code === 200) {
                        this.$message.success('笔记已更新')
                        this.dialogVisible = false
                        this.loadNotes()
                    } else {
                        this.$message.error(resp.data.msg || '更新失败')
                    }
                }).catch(() => {
                    this.$message.error('更新笔记失败')
                })
            } else {
                saveNote(data).then(resp => {
                    if (resp.data && resp.data.code === 200) {
                        this.$message.success('笔记已创建')
                        this.dialogVisible = false
                        this.loadNotes()
                    } else {
                        this.$message.error(resp.data.msg || '创建失败')
                    }
                }).catch(() => {
                    this.$message.error('创建笔记失败')
                })
            }
        },
        confirmDelete(note) {
            this.$confirm('确定删除该笔记吗？', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                deleteNote({ id: note.id }).then(resp => {
                    if (resp.data && resp.data.code === 200) {
                        this.$message.success('笔记已删除')
                        this.loadNotes()
                    } else {
                        this.$message.error(resp.data.msg || '删除失败')
                    }
                }).catch(() => {
                    this.$message.error('删除笔记失败')
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
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    gap: 15px;
}
.search-input {
    width: 300px;
}
.notes-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 16px;
}
.note-card {
    border-radius: 8px;
    transition: all 0.2s;
}
.note-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}
.note-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;
}
.note-title {
    margin: 0;
    font-size: 16px;
    color: #303133;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}
.note-preview {
    color: #606266;
    font-size: 14px;
    margin: 10px 0;
    display: -webkit-box;
    -webkit-line-clamp: 3;
    -webkit-box-orient: vertical;
    overflow: hidden;
}
.note-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 10px;
    padding-top: 10px;
    border-top: 1px solid #ebeef5;
}
.note-time {
    font-size: 12px;
    color: #909399;
}
.note-actions {
    display: flex;
    gap: 5px;
}
</style>