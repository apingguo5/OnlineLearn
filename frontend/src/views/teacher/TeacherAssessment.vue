<template>
    <div class="teacher-assessment">
        <div class="page-header">
            <h2><i class="el-icon-edit-outline"></i> 作业与考试</h2>
            <div class="header-tabs">
                <el-radio-group v-model="activeTab" size="medium">
                    <el-radio-button label="bank">题库管理</el-radio-button>
                    <el-radio-button label="publish">发布任务</el-radio-button>
                    <el-radio-button label="grading">批改与反馈</el-radio-button>
                </el-radio-group>
            </div>
        </div>

        <!-- ====== 题库管理 ====== -->
        <div v-show="activeTab === 'bank'">
            <el-card shadow="never" class="main-card">
                <div slot="header" class="card-header">
                    <span>题库管理</span>
                    <div>
                        <el-button type="primary" icon="el-icon-plus" size="small" @click="showAddQuestion = true">录入题目</el-button>
                        <el-button icon="el-icon-upload2" size="small" @click="showBatchImport = true">批量导入</el-button>
                    </div>
                </div>
                <el-table :data="questionBank" v-loading="loading" stripe style="width:100%">
                    <el-table-column prop="content" label="题目内容" min-width="300" show-overflow-tooltip></el-table-column>
                    <el-table-column prop="type" label="题型" width="100" align="center">
                        <template slot-scope="scope">
                            <el-tag :type="typeTag(scope.row.type)" size="small">{{ typeLabel(scope.row.type) }}</el-tag>
                        </template>
                    </el-table-column>
                    <el-table-column prop="difficulty" label="难度" width="80" align="center">
                        <template slot-scope="scope">
                            <span v-for="i in (scope.row.difficulty || 3)" :key="i" style="color:#E6A23C">★</span>
                        </template>
                    </el-table-column>
                    <el-table-column label="操作" width="120" align="center">
                        <template slot-scope="scope">
                            <el-button type="text" icon="el-icon-edit" @click="editQuestion(scope.row)">编辑</el-button>
                            <el-button type="text" icon="el-icon-delete" style="color:#F56C6C" @click="deleteQuestion(scope.row)">删除</el-button>
                        </template>
                    </el-table-column>
                </el-table>
            </el-card>
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
                    <el-table-column prop="className" label="目标班级" min-width="140"></el-table-column>
                    <el-table-column prop="type" label="类型" width="80" align="center">
                        <template slot-scope="scope">
                            <el-tag :type="scope.row.type === 'homework' ? 'warning' : 'danger'" size="small">
                                {{ scope.row.type === 'homework' ? '作业' : '考试' }}
                            </el-tag>
                        </template>
                    </el-table-column>
                    <el-table-column prop="deadline" label="截止时间" width="170" align="center"></el-table-column>
                    <el-table-column prop="submittedCount" label="提交/总人数" width="130" align="center">
                        <template slot-scope="scope">
                            <span>{{ scope.row.submittedCount || 0 }}/{{ scope.row.totalCount || 0 }}</span>
                        </template>
                    </el-table-column>
                    <el-table-column label="操作" width="100" align="center">
                        <template slot-scope="scope">
                            <el-button type="text" icon="el-icon-document" @click="viewTaskDetail(scope.row)">详情</el-button>
                        </template>
                    </el-table-column>
                </el-table>
            </el-card>
        </div>

        <!-- ====== 批改与反馈 ====== -->
        <div v-show="activeTab === 'grading'">
            <el-card shadow="never" class="main-card">
                <div slot="header" class="card-header">
                    <span>批改与反馈</span>
                    <el-select v-model="gradingFilter" placeholder="筛选任务" style="width:200px">
                        <el-option label="全部" value="all"></el-option>
                        <el-option v-for="t in publishedTasks" :key="t.id" :label="t.title" :value="t.id"></el-option>
                    </el-select>
                </div>
                <div class="grading-empty" v-if="pendingGrading.length === 0">
                    <i class="el-icon-document-checked" style="font-size:48px;color:#67C23A"></i>
                    <p>暂无待批改的作业</p>
                </div>
                <el-table :data="pendingGrading" v-else v-loading="loading" stripe style="width:100%">
                    <el-table-column prop="studentName" label="学生" min-width="100"></el-table-column>
                    <el-table-column prop="taskTitle" label="作业" min-width="180"></el-table-column>
                    <el-table-column prop="submitTime" label="提交时间" width="170" align="center"></el-table-column>
                    <el-table-column prop="autoScore" label="客观题得分" width="100" align="center">
                        <template slot-scope="scope">
                            <span :style="{color: scope.row.autoScore >= 60 ? '#67C23A' : '#F56C6C'}">{{ scope.row.autoScore || 0 }}分</span>
                        </template>
                    </el-table-column>
                    <el-table-column label="操作" width="120" align="center">
                        <template slot-scope="scope">
                            <el-button type="primary" size="mini" @click="doGrading(scope.row)">批改</el-button>
                        </template>
                    </el-table-column>
                </el-table>
            </el-card>
        </div>

        <!-- 录入题目对话框 -->
        <el-dialog title="录入题目" :visible.sync="showAddQuestion" width="600px">
            <el-form :model="questionForm" label-width="80px">
                <el-form-item label="题型">
                    <el-radio-group v-model="questionForm.type">
                        <el-radio label="single">单选题</el-radio>
                        <el-radio label="multiple">多选题</el-radio>
                        <el-radio label="fill">填空题</el-radio>
                        <el-radio label="subjective">主观题</el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-form-item label="所属课程">
                    <el-select v-model="questionForm.subjectId" placeholder="请选择" style="width:100%">
                        <el-option v-for="c in subjectList" :key="c.id" :label="c.courseName" :value="c.id"></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="题目内容">
                    <el-input type="textarea" v-model="questionForm.content" :rows="3" placeholder="请输入题目内容"></el-input>
                </el-form-item>
                <el-form-item label="选项" v-if="questionForm.type === 'single' || questionForm.type === 'multiple'">
                    <div class="option-item" v-for="(opt, idx) in questionForm.options" :key="idx">
                        <span class="option-label">{{ String.fromCharCode(65 + idx) }}.</span>
                        <el-input v-model="questionForm.options[idx]" size="small" style="width:300px"></el-input>
                        <el-button type="text" size="small" icon="el-icon-delete" @click="questionForm.options.splice(idx, 1)" v-if="questionForm.options.length > 2"></el-button>
                    </div>
                    <el-button type="text" icon="el-icon-plus" size="small" @click="questionForm.options.push('')">添加选项</el-button>
                </el-form-item>
                <el-form-item label="答案">
                    <el-input type="textarea" v-model="questionForm.answer" :rows="2" placeholder="请输入答案"></el-input>
                </el-form-item>
                <el-form-item label="难度">
                    <el-rate v-model="questionForm.difficulty" :max="5"></el-rate>
                </el-form-item>
            </el-form>
            <span slot="footer">
                <el-button @click="showAddQuestion = false">取消</el-button>
                <el-button type="primary" @click="saveQuestion">保存</el-button>
            </span>
        </el-dialog>

        <!-- 批量导入对话框 -->
        <el-dialog title="批量导入题目" :visible.sync="showBatchImport" width="500px">
            <p>支持 JSON 格式批量导入题目</p>
            <el-input type="textarea" v-model="batchImportJson" :rows="10" placeholder='[
  {"type":"single","content":"题目内容","options":["A选项","B选项","C选项","D选项"],"answer":"A","difficulty":3}
]'></el-input>
            <span slot="footer">
                <el-button @click="showBatchImport = false">取消</el-button>
                <el-button type="primary" @click="doBatchImport">导入</el-button>
            </span>
        </el-dialog>

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
                <el-form-item label="截止时间">
                    <el-date-picker v-model="publishForm.deadline" type="datetime" placeholder="选择截止时间" style="width:100%"></el-date-picker>
                </el-form-item>
                <el-form-item label="选择题目">
                    <el-select v-model="publishForm.questionIds" multiple placeholder="请选择题目" style="width:100%">
                        <el-option v-for="q in questionBank" :key="q.id" :label="(q.content || '').substring(0, 40)" :value="q.id"></el-option>
                    </el-select>
                </el-form-item>
            </el-form>
            <span slot="footer">
                <el-button @click="showPublishDialog = false">取消</el-button>
                <el-button type="primary" @click="doPublish">发布</el-button>
            </span>
        </el-dialog>

        <!-- 批改对话框 -->
        <el-dialog :title="'批改 - ' + gradingStudentName" :visible.sync="showGradingDialog" width="600px">
            <div class="grading-body">
                <p class="grading-question" v-for="(q, idx) in gradingQuestions" :key="idx">
                    <strong>{{ idx + 1 }}. {{ q.content }}</strong>
                    <span class="grading-answer">学生答案：{{ q.studentAnswer || '未作答' }}</span>
                    <span class="grading-correct" v-if="q.autoGraded">
                        <el-tag :type="q.correct ? 'success' : 'danger'" size="mini">{{ q.correct ? '正确' : '错误' }}</el-tag>
                    </span>
                    <el-input type="textarea" v-model="q.teacherFeedback" :rows="2" placeholder="教师评语（可选）" v-if="!q.autoGraded || !q.correct"></el-input>
                </p>
            </div>
            <span slot="footer">
                <el-button @click="showGradingDialog = false">取消</el-button>
                <el-button type="primary" @click="submitGrading">提交批改</el-button>
            </span>
        </el-dialog>
    </div>
</template>

<script>
import * as teacherApi from '@/api/teacher/teacherApi'

export default {
    name: "TeacherAssessment",
    data() {
        return {
            activeTab: 'bank',
            loading: false,
            subjectList: [],
            classList: [],
            // 题库
            questionBank: [],
            showAddQuestion: false,
            showBatchImport: false,
            batchImportJson: '',
            questionForm: {
                type: 'single',
                subjectId: null,
                content: '',
                options: ['', '', '', ''],
                answer: '',
                difficulty: 3
            },
            // 发布任务
            publishedTasks: [],
            showPublishDialog: false,
            publishForm: {
                title: '',
                type: 'homework',
                classId: null,
                deadline: null,
                questionIds: []
            },
            // 批改
            gradingFilter: 'all',
            pendingGrading: [],
            showGradingDialog: false,
            gradingStudentName: '',
            gradingQuestions: []
        }
    },
    created() {
        this.loadSubjects()
        this.loadClasses()
        this.loadQuestions()
        this.loadTasks()
        this.loadGrading()
    },
    methods: {
        typeLabel(type) {
            const map = { single: '单选题', multiple: '多选题', fill: '填空题', subjective: '主观题' }
            return map[type] || type
        },
        typeTag(type) {
            const map = { single: 'primary', multiple: 'success', fill: 'warning', subjective: 'info' }
            return map[type] || ''
        },
        async loadSubjects() {
            try {
                const res = await teacherApi.getCourses()
                this.subjectList = (res.data && res.data.data && res.data.data.list) ? res.data.data.list : []
            } catch (e) {}
        },
        async loadClasses() {
            try {
                const res = await teacherApi.getMyClasses({})
                this.classList = (res.data && res.data.list) ? res.data.list : []
            } catch (e) {}
        },
        async loadQuestions() {
            this.loading = true
            try {
                const res = await teacherApi.getQuestionBank({})
                this.questionBank = (res.data && res.data.list) ? res.data.list : []
            } catch (e) { this.questionBank = [] }
            this.loading = false
        },
        async loadTasks() {
            try {
                const res = await teacherApi.getPublishedTasks({})
                this.publishedTasks = (res.data && res.data.list) ? res.data.list : []
            } catch (e) { this.publishedTasks = [] }
        },
        async loadGrading() {
            try {
                const res = await teacherApi.getPendingGrading({})
                this.pendingGrading = (res.data && res.data.list) ? res.data.list : []
            } catch (e) { this.pendingGrading = [] }
        },
        saveQuestion() {
            // 字段映射：前端表单字段 → 后端 QuestionVo 字段
            const typeMap = { single: 1, multiple: 2, fill: 3, subjective: 4 }
            const params = {
                questionType: typeMap[this.questionForm.type] || 1,
                courseId: this.questionForm.subjectId,
                stem: this.questionForm.content,
                options: JSON.stringify(this.questionForm.options || []),
                answer: this.questionForm.answer,
                difficulty: this.questionForm.difficulty,
                creatorId: this.$store ? this.$store.state.userId : undefined
            }
            teacherApi.createQuestion(params).then(() => {
                this.$message.success('保存成功')
                this.showAddQuestion = false
                this.questionForm = { type: 'single', subjectId: null, content: '', options: ['', '', '', ''], answer: '', difficulty: 3 }
                this.loadQuestions()
            }).catch(() => this.$message.error('保存失败'))
        },
        editQuestion(row) {
            this.questionForm = { ...row, options: row.options || ['', '', '', ''] }
            this.showAddQuestion = true
        },
        deleteQuestion(row) {
            this.$confirm('确定删除该题目吗？', '提示', { type: 'warning' })
                .then(() => teacherApi.deleteQuestion({ id: row.id }).then(() => {
                    this.$message.success('删除成功')
                    this.loadQuestions()
                })).catch(() => {})
        },
        doBatchImport() {
            try {
                const questions = JSON.parse(this.batchImportJson)
                teacherApi.batchImportQuestions({ questions }).then(() => {
                    this.$message.success('导入成功')
                    this.showBatchImport = false
                    this.batchImportJson = ''
                    this.loadQuestions()
                }).catch(() => this.$message.error('导入失败'))
            } catch (e) {
                this.$message.error('JSON 格式错误')
            }
        },
        doPublish() {
            if (!this.publishForm.title || !this.publishForm.classId || !this.publishForm.deadline) {
                this.$message.warning('请填写完整信息')
                return
            }
            teacherApi.publishTask(this.publishForm).then(() => {
                this.$message.success('发布成功')
                this.showPublishDialog = false
                this.publishForm = { title: '', type: 'homework', classId: null, deadline: null, questionIds: [] }
                this.loadTasks()
            }).catch(() => this.$message.error('发布失败'))
        },
        viewTaskDetail(row) {
            this.$router.push('/teacher-grading')
            this.activeTab = 'grading'
        },
        doGrading(row) {
            this.gradingStudentName = row.studentName
            this.gradingQuestions = row.questions || [
                { content: '题目内容示例', studentAnswer: '学生答案', autoGraded: false, correct: false, teacherFeedback: '' }
            ]
            this.showGradingDialog = true
        },
        submitGrading() {
            teacherApi.submitGrading({ gradingData: this.gradingQuestions }).then(() => {
                this.$message.success('批改完成')
                this.showGradingDialog = false
                this.loadGrading()
            }).catch(() => this.$message.error('提交失败'))
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
.option-item {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 6px;
}
.option-label { font-weight: 600; min-width: 20px; }
.grading-empty {
    text-align: center;
    padding: 48px 0;
}
.grading-empty p { margin: 12px 0 0; color: #909399; }
.grading-body {
    padding: 8px 0;
}
.grading-question {
    background: #fafafa;
    border: 1px solid #e4e7ed;
    border-radius: 6px;
    padding: 12px 16px;
    margin-bottom: 12px;
}
.grading-answer {
    display: block;
    margin: 8px 0;
    color: #606266;
    font-size: 14px;
}
.grading-correct { margin-left: 8px; }
</style>