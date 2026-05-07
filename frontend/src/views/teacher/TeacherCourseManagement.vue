<template>
    <div class="teacher-course-mgmt">
        <div class="page-header">
            <h2><i class="el-icon-reading"></i> 课程管理</h2>
            <div class="header-actions">
                <el-button type="primary" icon="el-icon-plus" @click="showCreateDialog = true">创建课程</el-button>
            </div>
        </div>

        <!-- 课程列表 -->
        <el-card shadow="never" class="main-card">
            <el-table :data="courseList" style="width: 100%" v-loading="loading" stripe>
                <el-table-column prop="subjectName" label="课程名称" min-width="160">
                    <template slot-scope="scope">
                        <div class="course-cell">
                            <div class="course-avatar" :style="{ background: scope.row.color }">
                                {{ (scope.row.subjectName || '课').charAt(0) }}
                            </div>
                            <span class="course-name-text">{{ scope.row.subjectName }}</span>
                        </div>
                    </template>
                </el-table-column>
                <el-table-column prop="classCount" label="班级数" width="100" align="center">
                    <template slot-scope="scope">
                        <el-tag size="medium">{{ scope.row.classCount || 0 }}个</el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="章节目录" width="200" align="center">
                    <template slot-scope="scope">
                        <el-button type="text" icon="el-icon-s-management" @click="manageChapters(scope.row)">
                            管理章节
                        </el-button>
                    </template>
                </el-table-column>
                <el-table-column label="创建时间" prop="createTime" width="170" align="center"></el-table-column>
                <el-table-column label="操作" width="180" align="center" fixed="right">
                    <template slot-scope="scope">
                        <el-button type="text" icon="el-icon-edit" @click="editCourse(scope.row)">编辑</el-button>
                        <el-button type="text" icon="el-icon-delete" style="color:#F56C6C" @click="deleteCourse(scope.row)">删除</el-button>
                    </template>
                </el-table-column>
            </el-table>
        </el-card>

        <!-- 创建/编辑课程对话框 -->
        <el-dialog :title="dialogTitle" :visible.sync="showCreateDialog" width="500px" :close-on-click-modal="false">
            <el-form :model="courseForm" :rules="courseRules" ref="courseForm" label-width="100px">
                <el-form-item label="课程名称" prop="subjectName">
                    <el-input v-model="courseForm.subjectName" placeholder="请输入课程名称"></el-input>
                </el-form-item>
                <el-form-item label="课程简介" prop="description">
                    <el-input type="textarea" v-model="courseForm.description" :rows="4" placeholder="请输入课程简介"></el-input>
                </el-form-item>
                <el-form-item label="教学目标" prop="goal">
                    <el-input type="textarea" v-model="courseForm.goal" :rows="3" placeholder="请输入教学目标（可选）"></el-input>
                </el-form-item>
            </el-form>
            <span slot="footer">
                <el-button @click="showCreateDialog = false">取消</el-button>
                <el-button type="primary" @click="saveCourse" :loading="saving">保存</el-button>
            </span>
        </el-dialog>

        <!-- 章节管理抽屉 -->
        <el-drawer :title="'章节管理 - ' + currentCourseName" :visible.sync="showChapterDrawer" size="50%">
            <div class="chapter-drawer-body">
                <div class="drawer-header-actions">
                    <el-button type="primary" icon="el-icon-plus" size="small" @click="showAddChapter = true">新增章节</el-button>
                </div>
                <div v-loading="chapterLoading">
                    <div class="chapter-item" v-for="(ch, idx) in chapterList" :key="ch.id">
                        <div class="chapter-header">
                            <span class="chapter-index">第{{ idx + 1 }}章</span>
                            <span class="chapter-name" v-if="ch.editMode">
                                <el-input v-model="ch.editName" size="small" style="width:200px"></el-input>
                                <el-button type="primary" size="mini" @click="saveChapterName(ch)">保存</el-button>
                                <el-button size="mini" @click="ch.editMode = false">取消</el-button>
                            </span>
                            <span class="chapter-name" v-else>{{ ch.chapterName || ch.name }}</span>
                            <div class="chapter-actions" v-if="!ch.editMode">
                                <el-button type="text" icon="el-icon-edit" @click="startEditChapter(ch)"></el-button>
                                <el-button type="text" icon="el-icon-delete" style="color:#F56C6C" @click="deleteChapterItem(ch)"></el-button>
                            </div>
                        </div>
                        <div class="content-list">
                            <div class="content-item" v-for="(ct, cidx) in (ch.contents || [])" :key="ct.id">
                                <span class="content-index">{{ cidx + 1 }}.</span>
                                <el-tag size="mini" :type="ct.contentType === 'video' ? 'success' : 'primary'" class="content-type-tag">
                                    {{ ct.contentType === 'video' ? '视频' : '阅读' }}
                                </el-tag>
                                <span class="content-title">{{ ct.contentTitle || ct.title }}</span>
                                <el-button type="text" icon="el-icon-delete" size="mini" style="color:#F56C6C"
                                    @click="deleteContentItem(ch, ct)"></el-button>
                            </div>
                            <div class="content-empty" v-if="!ch.contents || ch.contents.length === 0">
                                暂无内容，请添加
                            </div>
                        </div>
                        <div class="add-content-btn">
                            <el-button type="text" icon="el-icon-plus" @click="showAddContent(ch)">添加内容</el-button>
                        </div>
                    </div>
                    <div class="empty-hint" v-if="chapterList.length === 0">
                        <i class="el-icon-document"></i>
                        <p>暂无章节，点击上方按钮创建</p>
                    </div>
                </div>
            </div>
        </el-drawer>

        <!-- 新增章节对话框 -->
        <el-dialog title="新增章节" :visible.sync="showAddChapter" width="400px">
            <el-form :model="chapterForm">
                <el-form-item label="章节名称">
                    <el-input v-model="chapterForm.name" placeholder="请输入章节名称"></el-input>
                </el-form-item>
            </el-form>
            <span slot="footer">
                <el-button @click="showAddChapter = false">取消</el-button>
                <el-button type="primary" @click="doAddChapter">确定</el-button>
            </span>
        </el-dialog>

        <!-- 添加内容对话框 -->
        <el-dialog title="添加章节内容" :visible.sync="showAddContentDialog" width="500px">
            <el-form :model="contentForm" label-width="100px">
                <el-form-item label="内容类型">
                    <el-radio-group v-model="contentForm.contentType">
                        <el-radio label="video">视频</el-radio>
                        <el-radio label="reading">文字阅读</el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-form-item label="标题">
                    <el-input v-model="contentForm.title" placeholder="请输入内容标题"></el-input>
                </el-form-item>
                <el-form-item label="内容" v-if="contentForm.contentType === 'reading'">
                    <el-input type="textarea" v-model="contentForm.content" :rows="4" placeholder="请输入学习内容"></el-input>
                </el-form-item>
                <el-form-item label="选择视频" v-if="contentForm.contentType === 'video'">
                    <el-select v-model="contentForm.videoId" placeholder="请选择视频" style="width:100%">
                        <el-option v-for="v in videoList" :key="v.id" :label="v.name || v.title" :value="v.id"></el-option>
                    </el-select>
                </el-form-item>
            </el-form>
            <span slot="footer">
                <el-button @click="showAddContentDialog = false">取消</el-button>
                <el-button type="primary" @click="doAddContent">确定</el-button>
            </span>
        </el-dialog>
    </div>
</template>

<script>
import * as teacherApi from '@/api/teacher/teacherApi'

export default {
    name: "TeacherCourseManagement",
    data() {
        return {
            loading: false,
            saving: false,
            courseList: [],
            showCreateDialog: false,
            dialogTitle: '创建课程',
            isEdit: false,
            editCourseId: null,
            courseForm: {
                subjectName: '',
                description: '',
                goal: ''
            },
            courseRules: {
                subjectName: [{ required: true, message: '请输入课程名称', trigger: 'blur' }]
            },
            // 章节管理
            showChapterDrawer: false,
            currentCourseId: null,
            currentCourseName: '',
            chapterLoading: false,
            chapterList: [],
            showAddChapter: false,
            chapterForm: { name: '' },
            // 内容管理
            showAddContentDialog: false,
            currentChapterId: null,
            contentForm: {
                contentType: 'video',
                title: '',
                content: '',
                videoId: null
            },
            videoList: []
        }
    },
    created() {
        this.loadCourses()
    },
    methods: {
        async loadCourses() {
            this.loading = true
            try {
                const res = await teacherApi.getMySubjects({})
                if (res.data && res.data.list) {
                    const colors = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#9B59B6', '#1ABC9C']
                    this.courseList = res.data.list.map((c, idx) => ({
                        ...c,
                        color: colors[idx % colors.length]
                    }))
                } else {
                    this.courseList = []
                }
            } catch (e) {
                console.error('加载课程失败', e)
                this.courseList = []
            }
            this.loading = false
        },
        saveCourse() {
            this.$refs.courseForm.validate(async (valid) => {
                if (!valid) return
                this.saving = true
                try {
                    if (this.isEdit) {
                        // 编辑逻辑
                        await teacherApi.createSubject(this.courseForm)
                    } else {
                        await teacherApi.createSubject(this.courseForm)
                    }
                    this.$message.success(this.isEdit ? '更新成功' : '创建成功')
                    this.showCreateDialog = false
                    this.resetForm()
                    this.loadCourses()
                } catch (e) {
                    this.$message.error('操作失败')
                }
                this.saving = false
            })
        },
        editCourse(row) {
            this.isEdit = true
            this.editCourseId = row.id
            this.dialogTitle = '编辑课程'
            this.courseForm = {
                subjectName: row.subjectName,
                description: row.description || '',
                goal: row.goal || ''
            }
            this.showCreateDialog = true
        },
        deleteCourse(row) {
            this.$confirm(`确定删除课程"${row.subjectName}"吗？`, '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(async () => {
                try {
                    await teacherApi.deleteSubject({ id: row.id })
                    this.$message.success('删除成功')
                    this.loadCourses()
                } catch (e) {
                    this.$message.error('删除失败')
                }
            }).catch(() => {})
        },
        resetForm() {
            this.courseForm = { subjectName: '', description: '', goal: '' }
            this.isEdit = false
            this.editCourseId = null
            this.dialogTitle = '创建课程'
        },
        // 章节管理
        async manageChapters(row) {
            this.currentCourseId = row.id
            this.currentCourseName = row.subjectName
            this.showChapterDrawer = true
            await this.loadChapters()
        },
        async loadChapters() {
            this.chapterLoading = true
            try {
                const res = await teacherApi.getChapters({ subjectId: this.currentCourseId })
                if (res.data && res.data.list) {
                    this.chapterList = res.data.list.map(ch => ({
                        ...ch,
                        editMode: false,
                        editName: ch.chapterName || ch.name,
                        contents: ch.contents || []
                    }))
                } else {
                    this.chapterList = []
                }
                // 加载每个章节的内容
                for (const ch of this.chapterList) {
                    try {
                        const ctRes = await teacherApi.getContents({ chapterId: ch.id })
                        ch.contents = ctRes.data && ctRes.data.list ? ctRes.data.list : []
                    } catch (e) {
                        ch.contents = []
                    }
                }
            } catch (e) {
                console.error('加载章节失败', e)
                this.chapterList = []
            }
            this.chapterLoading = false
        },
        startEditChapter(ch) {
            ch.editMode = true
            ch.editName = ch.chapterName || ch.name
        },
        async saveChapterName(ch) {
            try {
                await teacherApi.updateChapter({ id: ch.id, chapterName: ch.editName })
                ch.chapterName = ch.editName
                ch.name = ch.editName
                ch.editMode = false
                this.$message.success('更新成功')
            } catch (e) {
                this.$message.error('更新失败')
            }
        },
        async deleteChapterItem(ch) {
            this.$confirm(`确定删除章节"${ch.chapterName || ch.name}"吗？`, '提示', { type: 'warning' })
                .then(async () => {
                    try {
                        await teacherApi.deleteChapter({ id: ch.id })
                        this.$message.success('删除成功')
                        this.loadChapters()
                    } catch (e) {
                        this.$message.error('删除失败')
                    }
                }).catch(() => {})
        },
        doAddChapter() {
            if (!this.chapterForm.name) {
                this.$message.warning('请输入章节名称')
                return
            }
            teacherApi.addChapter({
                subjectId: this.currentCourseId,
                chapterName: this.chapterForm.name
            }).then(() => {
                this.$message.success('添加成功')
                this.showAddChapter = false
                this.chapterForm.name = ''
                this.loadChapters()
            }).catch(() => {
                this.$message.error('添加失败')
            })
        },
        showAddContent(ch) {
            this.currentChapterId = ch.id
            this.contentForm = { contentType: 'video', title: '', content: '', videoId: null }
            this.showAddContentDialog = true
            // 加载视频列表
            teacherApi.getVideoList().then(res => {
                this.videoList = res.data && res.data.list ? res.data.list : []
            }).catch(() => {})
        },
        doAddContent() {
            if (!this.contentForm.title) {
                this.$message.warning('请输入内容标题')
                return
            }
            teacherApi.addContent({
                chapterId: this.currentChapterId,
                contentType: this.contentForm.contentType,
                title: this.contentForm.title,
                content: this.contentForm.contentType === 'reading' ? this.contentForm.content : '',
                videoId: this.contentForm.contentType === 'video' ? this.contentForm.videoId : null
            }).then(() => {
                this.$message.success('添加成功')
                this.showAddContentDialog = false
                this.loadChapters()
            }).catch(() => {
                this.$message.error('添加失败')
            })
        },
        deleteContentItem(ch, ct) {
            this.$confirm('确定删除该内容吗？', '提示', { type: 'warning' })
                .then(async () => {
                    try {
                        await teacherApi.deleteContent({ id: ct.id })
                        this.$message.success('删除成功')
                        this.loadChapters()
                    } catch (e) {
                        this.$message.error('删除失败')
                    }
                }).catch(() => {})
        }
    }
}
</script>

<style scoped>
.teacher-course-mgmt {
    padding: 24px;
    background: #f5f7fa;
    min-height: 100vh;
}

.page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}

.page-header h2 {
    margin: 0;
    font-size: 22px;
    color: #303133;
}

.page-header h2 i {
    margin-right: 8px;
    color: #409EFF;
}

.main-card {
    border-radius: 8px;
}

.course-cell {
    display: flex;
    align-items: center;
    gap: 10px;
}

.course-avatar {
    width: 36px;
    height: 36px;
    border-radius: 6px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-weight: 700;
    font-size: 16px;
}

.course-name-text {
    font-weight: 600;
    color: #303133;
}

/* 章节管理 */
.chapter-drawer-body {
    padding: 0 20px 20px;
}

.drawer-header-actions {
    margin-bottom: 16px;
}

.chapter-item {
    background: #fafafa;
    border: 1px solid #e4e7ed;
    border-radius: 6px;
    margin-bottom: 12px;
    padding: 12px 16px;
}

.chapter-header {
    display: flex;
    align-items: center;
    gap: 8px;
    flex-wrap: wrap;
}

.chapter-index {
    font-weight: 600;
    color: #409EFF;
    background: #ecf5ff;
    padding: 2px 10px;
    border-radius: 4px;
    font-size: 13px;
    white-space: nowrap;
}

.chapter-name {
    flex: 1;
    font-size: 15px;
    font-weight: 500;
    color: #303133;
}

.chapter-actions {
    display: flex;
    gap: 4px;
}

.content-list {
    margin-left: 30px;
    margin-top: 10px;
}

.content-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 6px 8px;
    border-bottom: 1px dashed #ebeef5;
}

.content-item:last-child {
    border-bottom: none;
}

.content-index {
    color: #909399;
    font-weight: 600;
    min-width: 20px;
}

.content-type-tag {
    min-width: 50px;
    text-align: center;
}

.content-title {
    flex: 1;
    color: #303133;
    font-size: 14px;
}

.content-empty {
    margin-left: 30px;
    margin-top: 8px;
    color: #c0c4cc;
    font-size: 13px;
    font-style: italic;
}

.add-content-btn {
    margin-left: 30px;
    margin-top: 6px;
}

.empty-hint {
    text-align: center;
    padding: 40px 0;
    color: #909399;
}

.empty-hint i {
    font-size: 28px;
}

.empty-hint p {
    margin: 8px 0 0;
}
</style>