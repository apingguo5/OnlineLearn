<template>
    <div class="teacher-assessment">
        <div class="page-header">
            <h2><i class="el-icon-edit-outline"></i> 作业管理</h2>
            <div class="header-tabs">
                <el-radio-group v-model="activeTab" size="medium">
                    <el-radio-button label="publish">发布任务</el-radio-button>
                    <el-radio-button label="paper">组卷管理</el-radio-button>
                </el-radio-group>
            </div>
        </div>

        <!-- ====== 发布任务 ====== -->
        <div v-show="activeTab === 'publish'">
            <el-card shadow="never" class="main-card">
                <div slot="header" class="card-header">
                    <span>发布任务</span>
                    <el-button type="primary" icon="el-icon-plus" size="small" @click="showPublishDialog = true">发布新任务</el-button>
                </div>
                <el-table :data="publishedTasks" v-loading="loading" stripe style="width:100%">
                    <el-table-column prop="title" label="任务名称" min-width="180"></el-table-column>
                    <el-table-column prop="courseName" label="所属课程" min-width="140"></el-table-column>
                    <el-table-column prop="className" label="目标班级" min-width="140"></el-table-column>
                    <el-table-column label="类型" width="70" align="center">
                        <template slot-scope="scope">
                            <el-tag :type="getTaskType(scope.row.content) === 'exam' ? 'danger' : 'warning'" size="small">
                                {{ getTaskType(scope.row.content) === 'exam' ? '考试' : '作业' }}
                            </el-tag>
                        </template>
                    </el-table-column>
                    <el-table-column prop="commitTime" label="截止时间" width="170" align="center">
                        <template slot-scope="scope">{{ scope.row.commitTime || scope.row.createTime }}</template>
                    </el-table-column>
                    <el-table-column label="提交/总人数" width="120" align="center">
                        <template slot-scope="scope">
                            <span>{{ scope.row.submittedCount || 0 }}/{{ scope.row.totalCount || 0 }}</span>
                        </template>
                    </el-table-column>
                    <el-table-column label="操作" width="100" align="center">
                        <template slot-scope="scope">
                            <el-button type="text" icon="el-icon-document" @click="viewTaskDetail(scope.row)">详情</el-button>
                            <el-button type="text" icon="el-icon-delete" style="color:#F56C6C" @click="deleteTask(scope.row)">删除</el-button>
                        </template>
                    </el-table-column>
                </el-table>
            </el-card>
        </div>

        <!-- ====== 组卷管理（从原 TeacherExamPaper 合并至此） ====== -->
        <div v-show="activeTab === 'paper'">
            <el-card shadow="never" class="main-card">
                <div slot="header" class="card-header">
                    <span>组卷管理</span>
                    <el-button type="primary" icon="el-icon-plus" size="small" @click="showPaperDialog = true">创建试卷</el-button>
                </div>
                <el-table :data="paperList" v-loading="paperLoading" stripe style="width:100%">
                    <el-table-column prop="title" label="试卷名称" min-width="200" />
                    <el-table-column prop="courseName" label="所属课程" min-width="140" />
                    <el-table-column label="题目数" width="80" align="center">
                        <template slot-scope="scope">{{ scope.row.questionCount || 0 }}</template>
                    </el-table-column>
                    <el-table-column prop="totalScore" label="总分" width="70" align="center" />
                    <el-table-column prop="duration" label="时长(分钟)" width="100" align="center" />
                    <el-table-column label="状态" width="80" align="center">
                        <template slot-scope="scope">
                            <el-tag :type="scope.row.status === 'published' ? 'success' : 'info'" size="small">
                                {{ scope.row.status === 'published' ? '已发布' : '草稿' }}
                            </el-tag>
                        </template>
                    </el-table-column>
                    <el-table-column prop="createTime" label="创建时间" width="170" align="center" />
                    <el-table-column label="操作" width="220" align="center" fixed="right">
                        <template slot-scope="scope">
                            <el-button type="text" icon="el-icon-view" @click="previewPaper(scope.row)">预览</el-button>
                            <el-button type="text" icon="el-icon-edit" @click="editPaper(scope.row)" v-if="scope.row.status !== 'published'">编辑</el-button>
                            <el-button type="text" icon="el-icon-success" style="color:#67C23A" @click="publishPaper(scope.row)" v-if="scope.row.status !== 'published'">发布</el-button>
                            <el-button type="text" icon="el-icon-delete" style="color:#F56C6C" @click="deletePaper(scope.row)">删除</el-button>
                        </template>
                    </el-table-column>
                </el-table>
                <el-pagination
                    @size-change="s=>{paperLimit=s;loadPapers()}"
                    @current-change="p=>{paperPage=p;loadPapers()}"
                    :current-page="paperPage"
                    :page-sizes="[10, 20, 50]"
                    :page-size="paperLimit"
                    layout="total, sizes, prev, pager, next, jumper"
                    :total="paperTotal"
                    style="margin-top:16px;text-align:right"
                />
            </el-card>
        </div>

        <!-- 发布任务对话框 -->
        <el-dialog title="发布任务" :visible.sync="showPublishDialog" width="500px">
            <el-form :model="publishForm" label-width="100px">
                <el-form-item label="任务名称">
                    <el-input v-model="publishForm.title" placeholder="如：第二章课后作业"></el-input>
                </el-form-item>
                <el-form-item label="任务类型">
                    <el-radio-group v-model="publishForm.type">
                        <el-radio label="homework">作业</el-radio>
                        <el-radio label="exam">考试</el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-form-item label="选择班级">
                    <el-select v-model="publishForm.classId" placeholder="请选择班级" style="width:100%">
                        <el-option v-for="cls in classList" :key="cls.id" :label="cls.className" :value="cls.id"></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="选择试卷">
                    <el-select v-model="publishForm.paperId" placeholder="可选择已有试卷（可选）" clearable style="width:100%">
                        <el-option v-for="p in paperList" :key="p.id" :label="p.title" :value="p.id"></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="截止时间">
                    <el-date-picker v-model="publishForm.deadline" type="datetime" placeholder="选择截止时间" style="width:100%"></el-date-picker>
                </el-form-item>
            </el-form>
            <span slot="footer">
                <el-button @click="showPublishDialog = false">取消</el-button>
                <el-button type="primary" @click="doPublish">发布</el-button>
            </span>
        </el-dialog>

        <!-- 创建/编辑试卷对话框 -->
        <el-dialog :title="isEditPaper ? '编辑试卷' : '创建试卷'" :visible.sync="showPaperDialog" width="800px" @closed="resetPaperForm">
            <el-form :model="paperForm" label-width="100px" size="small">
                <el-form-item label="试卷名称" required>
                    <el-input v-model="paperForm.title" placeholder="如：第二章单元测试" />
                </el-form-item>
                <el-form-item label="考试说明">
                    <el-input type="textarea" v-model="paperForm.description" :rows="2" placeholder="考试说明（可选）" />
                </el-form-item>
                <el-form-item label="所属课程" required>
                    <el-select v-model="paperForm.courseId" placeholder="请选择课程" style="width:100%" @change="onCourseChange">
                        <el-option v-for="c in courseList" :key="c.id" :label="c.courseName" :value="c.id" />
                    </el-select>
                </el-form-item>
                <el-form-item label="所属章节">
                    <el-select v-model="paperForm.chapterId" placeholder="可选" style="width:100%" clearable>
                        <el-option v-for="ch in chapterList" :key="ch.id" :label="ch.chapterName" :value="ch.id" />
                    </el-select>
                </el-form-item>
                <el-form-item label="考试时长(分钟)">
                    <el-input-number v-model="paperForm.duration" :min="0" :max="300" />
                </el-form-item>
                <el-form-item label="总分">
                    <el-input-number v-model="paperForm.totalScore" :min="0" :max="500" />
                </el-form-item>
                <el-form-item label="选择题目">
                    <div class="question-select-area">
                        <div class="qs-toolbar">
                            <el-select v-model="qsFilter.type" placeholder="筛选题型" clearable style="width:130px" size="mini">
                                <el-option label="单选题" value="single" />
                                <el-option label="多选题" value="multiple" />
                                <el-option label="判断题" value="judge" />
                                <el-option label="填空题" value="fill" />
                                <el-option label="主观题" value="essay" />
                            </el-select>
                            <el-button size="mini" @click="loadAvailableQuestions">加载题目</el-button>
                        </div>
                        <el-table :data="availableQuestions" v-loading="qsLoading" max-height="300" stripe style="width:100%" @selection-change="onSelectionChange">
                            <el-table-column type="selection" width="40" />
                            <el-table-column label="题型" width="70">
                                <template slot-scope="s">
                                    <el-tag :type="typeTag(s.row.questionType)" size="mini">{{ typeLabel(s.row.questionType) }}</el-tag>
                                </template>
                            </el-table-column>
                            <el-table-column prop="stem" label="题目内容" min-width="250" show-overflow-tooltip />
                            <el-table-column prop="score" label="分值" width="60" align="center" />
                        </el-table>
                    </div>
                </el-form-item>
            </el-form>
            <span slot="footer">
                <el-button @click="showPaperDialog = false">取消</el-button>
                <el-button type="primary" @click="savePaper" :loading="paperSaving">保存</el-button>
            </span>
        </el-dialog>

        <!-- 预览试卷对话框 -->
        <el-dialog title="试卷预览" :visible.sync="showPreviewDialog" width="700px">
            <div class="paper-preview">
                <h3>{{ previewData.title }}</h3>
                <p class="paper-desc" v-if="previewData.description">{{ previewData.description }}</p>
                <p class="paper-meta">
                    <span>总分：{{ previewData.totalScore }}分</span>
                    <span v-if="previewData.duration"> | 时长：{{ previewData.duration }}分钟</span>
                    <span> | 题目数：{{ previewData.questionCount || 0 }}题</span>
                </p>
                <div class="preview-questions">
                    <div v-for="(q, idx) in previewData.questions" :key="idx" class="pq-item">
                        <div class="pq-header">
                            <strong>{{ idx + 1 }}.</strong>
                            <el-tag :type="typeTag(q.questionType)" size="mini" style="margin:0 8px">{{ typeLabel(q.questionType) }}</el-tag>
                            <span class="pq-score">({{ q.score }}分)</span>
                        </div>
                        <div class="pq-stem">{{ q.stem }}</div>
                    </div>
                </div>
            </div>
        </el-dialog>
    </div>
</template>

<script>
import * as teacherApi from '@/api/teacher/teacherApi'
import { getPaperList, createPaper, updatePaper, deletePaper, publishPaper, getPaperDetail } from '@/api/teacher/examApi'
import { getQuestionList } from '@/api/teacher/examApi'

export default {
    name: "TeacherAssessment",
    data() {
        return {
            activeTab: 'publish',
            loading: false,
            subjectList: [],
            courseList: [],
            classList: [],
            // 发布任务
            publishedTasks: [],
            showPublishDialog: false,
            publishForm: {
                title: '',
                type: 'homework',
                classId: null,
                paperId: null,
                deadline: null
            },
            // 组卷管理
            paperLoading: false,
            paperList: [],
            paperTotal: 0, paperPage: 1, paperLimit: 10,
            showPaperDialog: false,
            showPreviewDialog: false,
            isEditPaper: false,
            editPaperId: null,
            chapterList: [],
            paperForm: {
                title: '', description: '', courseId: '', chapterId: '',
                duration: 60, totalScore: 100, questionIds: []
            },
            paperSaving: false,
            qsFilter: { type: '' },
            qsLoading: false,
            availableQuestions: [],
            selectedQuestionIds: [],
            previewData: { questions: [] }
        }
    },
    created() {
        this.loadSubjects()
        this.loadCourses()
        this.loadClasses()
        this.loadTasks()
        this.loadPapers()
    },
    methods: {
        getTaskType(content) {
            if (!content) return 'homework'
            try {
                const data = JSON.parse(content)
                if (data && data.type) return data.type
            } catch (e) {}
            if (typeof content === 'string' && content.indexOf('paperRef:') === 0) return 'exam'
            return 'homework'
        },
        typeLabel(type) {
            const map = { single: '单选', multiple: '多选', judge: '判断', fill: '填空', essay: '主观' }
            return map[type] || type
        },
        typeTag(type) {
            const map = { single: 'primary', multiple: 'success', judge: 'warning', fill: 'info', essay: '' }
            return map[type] || ''
        },
        async loadSubjects() {
            try {
                const res = await teacherApi.getCourses()
                this.subjectList = (res.data && res.data.data && res.data.data.list) ? res.data.data.list : []
            } catch (e) {}
        },
        async loadCourses() {
            try {
                const res = await teacherApi.getCourses()
                this.courseList = (res.data && res.data.data && res.data.data.list) ? res.data.data.list : []
            } catch (e) { this.courseList = [] }
        },
        async loadClasses() {
            try {
                const res = await teacherApi.getMyClasses({})
                const resultData = res.data && res.data.resultData
                this.classList = Array.isArray(resultData) ? resultData : []
            } catch (e) {}
        },
        async loadTasks() {
            this.loading = true
            try {
                const params = { page: 1, pageSize: 50, userId: this.$cookies.get('userId'), roleId: this.$cookies.get('roleId') }
                const res = await teacherApi.getPublishedTasks(params)
                const resultData = res.data && res.data.resultData
                this.publishedTasks = (resultData && resultData.data) ? resultData.data : []
            } catch (e) { this.publishedTasks = [] }
            this.loading = false
        },
        // ===== 发布任务 =====
        doPublish() {
            if (!this.publishForm.title || !this.publishForm.classId || !this.publishForm.deadline) {
                this.$message.warning('请填写完整信息')
                return
            }
            const dt = new Date(this.publishForm.deadline)
            const pad = n => String(n).padStart(2, '0')
            const commitTime = dt.getFullYear() + '-' + pad(dt.getMonth()+1) + '-' + pad(dt.getDate()) + ' ' + pad(dt.getHours()) + ':' + pad(dt.getMinutes()) + ':' + pad(dt.getSeconds())
            const contentData = { type: this.publishForm.type || 'homework' }
            if (this.publishForm.paperId) {
                contentData.paperRef = this.publishForm.paperId
            }
            const params = {
                title: this.publishForm.title,
                content: JSON.stringify(contentData),
                classId: this.publishForm.classId,
                commitTime: commitTime,
                creator: this.$cookies.get('userId')
            }
            teacherApi.publishTask(params).then(() => {
                this.$message.success('发布成功')
                this.showPublishDialog = false
                this.publishForm = { title: '', type: 'homework', classId: null, paperId: null, deadline: null }
                this.loadTasks()
            }).catch(() => this.$message.error('发布失败'))
        },
        viewTaskDetail(row) {
            this.$router.push('/teachergrading')
        },
        deleteTask(row) {
            this.$confirm('确定删除该任务吗？', '提示', { type: 'warning' })
                .then(() => teacherApi.deleteTask({ id: row.id }).then(() => {
                    this.$message.success('删除成功')
                    this.loadTasks()
                })).catch(() => {})
        },
        // ===== 组卷管理 =====
        async loadChapters(courseId) {
            if (!courseId) { this.chapterList = []; return }
            try {
                const res = await this.$post('/study/teacher/course/chapters', { courseId })
                const resultData = res.data && res.data.resultData
                this.chapterList = Array.isArray(resultData) ? resultData : []
            } catch (e) { this.chapterList = [] }
        },
        async loadPapers() {
            this.paperLoading = true
            try {
                const params = { page: this.paperPage, limit: this.paperLimit }
                const res = await getPaperList(params)
                const resultData = res.data && res.data.resultData
                this.paperList = (resultData && resultData.data) ? resultData.data : []
                this.paperTotal = (resultData && resultData.total) ? resultData.total : 0
            } catch (e) { this.paperList = []; this.paperTotal = 0 }
            this.paperLoading = false
        },
        onCourseChange(val) { this.loadChapters(val); this.loadAvailableQuestions() },
        async loadAvailableQuestions() {
            if (!this.paperForm.courseId) { this.$message.warning('请先选择课程'); return }
            this.qsLoading = true
            try {
                const params = { page: 1, limit: 999, courseId: this.paperForm.courseId, questionType: this.qsFilter.type || undefined }
                const res = await getQuestionList(params)
                const resultData = res.data && res.data.resultData
                this.availableQuestions = (resultData && resultData.data) ? resultData.data : []
            } catch (e) { this.availableQuestions = [] }
            this.qsLoading = false
        },
        onSelectionChange(rows) { this.selectedQuestionIds = rows.map(r => r.id) },
        resetPaperForm() {
            this.paperForm = { title: '', description: '', courseId: '', chapterId: '', duration: 60, totalScore: 100, questionIds: [] }
            this.isEditPaper = false; this.editPaperId = null
            this.availableQuestions = []; this.selectedQuestionIds = []
        },
        async previewPaper(row) {
            try {
                const res = await getPaperDetail(row.id)
                this.previewData = (res.data && res.data.resultData) ? res.data.resultData : row
                if (!this.previewData.questions) this.previewData.questions = []
                this.showPreviewDialog = true
            } catch (e) {
                this.previewData = row
                this.showPreviewDialog = true
            }
        },
        editPaper(row) {
            this.isEditPaper = true; this.editPaperId = row.id
            this.paperForm = {
                title: row.title || '',
                description: row.description || '',
                courseId: row.courseId || '',
                chapterId: row.chapterId || '',
                duration: row.duration || 60,
                totalScore: row.totalScore || 100,
                questionIds: row.questionIds || []
            }
            if (row.courseId) { this.loadChapters(row.courseId); this.loadAvailableQuestions() }
            this.showPaperDialog = true
        },
        async savePaper() {
            if (!this.paperForm.title) { this.$message.warning('请输入试卷名称'); return }
            if (!this.paperForm.courseId) { this.$message.warning('请选择课程'); return }
            this.paperSaving = true
            try {
                const params = {
                    ...this.paperForm,
                    questionIds: this.selectedQuestionIds
                }
                if (this.isEditPaper) {
                    params.id = this.editPaperId
                    await updatePaper(params)
                    this.$message.success('更新成功')
                } else {
                    params.creatorId = this.$cookies.get('userId')
                    await createPaper(params)
                    this.$message.success('创建成功')
                }
                this.showPaperDialog = false
                this.loadPapers()
            } catch (e) { this.$message.error('保存失败') }
            this.paperSaving = false
        },
        async publishPaper(row) {
            this.$confirm('发布后学生即可看到试卷，确定发布吗？', '提示', { type: 'warning' })
                .then(async () => {
                    try { await publishPaper(row.id); this.$message.success('发布成功'); this.loadPapers() }
                    catch (e) { this.$message.error('发布失败') }
                }).catch(() => {})
        },
        deletePaper(row) {
            this.$confirm('确定删除该试卷吗？', '提示', { type: 'warning' })
                .then(async () => { try { await deletePaper(row.id); this.$message.success('删除成功'); this.loadPapers() } catch (e) { this.$message.error('删除失败') } })
                .catch(() => {})
        }
    }
}
</script>

<style scoped>
.teacher-assessment {
    padding: 24px;
    background: #f5f7fa;
    min-height: 100vh;
}
.page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    flex-wrap: wrap;
    gap: 12px;
}
.page-header h2 { margin: 0; font-size: 22px; color: #303133; }
.page-header h2 i { margin-right: 8px; color: #409EFF; }
.main-card { border-radius: 8px; }
.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}
.card-header span { font-weight: 600; }
.question-select-area { border: 1px solid #e4e7ed; border-radius: 4px; padding: 8px; }
.qs-toolbar { margin-bottom: 8px; display: flex; gap: 8px; }
.paper-preview h3 { margin: 0 0 8px; }
.paper-desc { color: #909399; margin-bottom: 8px; }
.paper-meta { color: #606266; font-size: 14px; margin-bottom: 16px; }
.pq-item { background: #fafafa; border: 1px solid #e4e7ed; border-radius: 6px; padding: 12px; margin-bottom: 8px; }
.pq-header { margin-bottom: 4px; }
.pq-score { font-size: 12px; color: #909399; }
.pq-stem { font-size: 14px; color: #303133; line-height: 1.5; }
</style>