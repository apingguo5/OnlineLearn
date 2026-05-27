<template>
    <div class="teacher-assessment">
        <div class="page-header">
            <h2><i class="el-icon-edit-outline"></i> 作业与考试</h2>
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
                    <el-table-column label="允许重交" width="90" align="center">
                        <template slot-scope="scope">
                            <el-switch v-model="scope.row.allowResubmit" @change="toggleResubmit(scope.row)" :active-value="1" :inactive-value="0" />
                        </template>
                    </el-table-column>
                    <el-table-column label="操作" width="120" align="center">
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
        <el-dialog title="发布任务" :visible.sync="showPublishDialog" width="500px" :append-to-body="false">
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
        <el-dialog :title="isEditPaper ? '编辑试卷' : '创建试卷'" :visible.sync="showPaperDialog" width="800px" :append-to-body="false" @closed="resetPaperForm">
            <el-form :model="paperForm" label-width="100px" size="small">
                <el-form-item label="试卷名称" required>
                    <el-input v-model="paperForm.title" placeholder="如：第二章单元测试" />
                </el-form-item>
                <el-form-item label="考试说明">
                    <el-input type="textarea" v-model="paperForm.description" :rows="2" placeholder="考试说明（可选）" />
                </el-form-item>
                <el-form-item label="所属课程" required>
                    <el-select v-model="paperForm.courseId" placeholder="请选择课程" style="width:100%" clearable @change="onCourseChange" @clear="onCourseClear">
                        <el-option v-for="c in courseList" :key="c.id" :label="c.courseName" :value="c.id" />
                    </el-select>
                </el-form-item>
                <el-form-item label="所属章节">
                    <el-select v-model="paperForm.chapterId" placeholder="请先选择课程" style="width:100%" clearable :disabled="!paperForm.courseId">
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
        <el-dialog title="试卷预览" :visible.sync="showPreviewDialog" width="850px" top="3vh" :append-to-body="false" @closed="previewCurrentIdx=0">
            <div class="paper-preview" v-if="previewData.questions && previewData.questions.length > 0">
                <h3>{{ previewData.title }}</h3>
                <p class="paper-desc" v-if="previewData.description">{{ previewData.description }}</p>
                <p class="paper-meta">
                    <span>总分：{{ previewData.totalScore }}分</span>
                    <span v-if="previewData.duration"> | 时长：{{ previewData.duration }}分钟</span>
                    <span> | 题目数：{{ previewData.questionCount || previewData.questions.length }}题</span>
                </p>
                <div class="preview-nav-bar">
                    <div
                        v-for="(q, idx) in previewData.questions" :key="idx"
                        class="preview-nav-dot"
                        :class="{ 'preview-nav-active': previewCurrentIdx === idx }"
                        @click="previewCurrentIdx = idx"
                    >{{ idx + 1 }}</div>
                </div>
                <div v-for="(q, idx) in previewData.questions" :key="idx" v-show="previewCurrentIdx === idx" class="pq-item-single">
                    <div class="pq-header">
                        <strong>{{ idx + 1 }}.</strong>
                        <el-tag :type="typeTag(q.questionType)" size="mini" style="margin:0 8px">{{ typeLabel(q.questionType) }}</el-tag>
                        <span class="pq-score">({{ q.score }}分)</span>
                    </div>
                    <div class="pq-stem">{{ q.stem }}</div>
                    <div v-if="q.questionType === 'single' || q.questionType === 'multiple'" class="pq-options">
                        <div v-for="(opt, oi) in parsedOptions(q)" :key="oi" class="pq-option-single">
                            {{ String.fromCharCode(65 + oi) }}. {{ opt }}
                        </div>
                    </div>
                    <div v-if="q.questionType === 'judge'" class="pq-options">
                        <div class="pq-option-single">正确 / 错误</div>
                    </div>
                    <div v-if="q.answer" class="pq-answer">
                        <span class="answer-label">正确答案：</span>
                        <span class="answer-value">{{ formatAnswer(q) }}</span>
                    </div>
                    <div v-if="q.analysis" class="pq-answer">
                        <span class="answer-label">解析：</span>
                        <span class="answer-value">{{ q.analysis }}</span>
                    </div>
                </div>
                <div class="preview-nav-footer">
                    <el-button size="small" @click="prevPreviewQuestion" :disabled="previewCurrentIdx <= 0">上一题</el-button>
                    <span class="nav-progress">{{ previewCurrentIdx + 1 }} / {{ previewData.questions.length }}</span>
                    <el-button size="small" @click="nextPreviewQuestion" :disabled="previewCurrentIdx >= previewData.questions.length - 1">下一题</el-button>
                </div>
            </div>
            <el-empty v-else description="试卷暂无题目" />
        </el-dialog>

        <!-- 作业详情对话框 -->
        <el-dialog :title="homeworkDetail.title" :visible.sync="showHomeworkDetailDialog" width="850px" top="3vh" :append-to-body="false" v-loading="detailLoading" @closed="detailCurrentIdx=0">
            <div class="homework-detail">
                <div class="detail-meta">
                    <el-tag :type="getTaskType(homeworkDetail.content) === 'exam' ? 'danger' : 'warning'" size="small">
                        {{ getTaskType(homeworkDetail.content) === 'exam' ? '考试' : '作业' }}
                    </el-tag>
                    <span style="margin-left:12px;color:#606266">班级：{{ homeworkDetail.className }}</span>
                    <span style="margin-left:12px;color:#606266">课程：{{ homeworkDetail.courseName }}</span>
                    <span style="margin-left:12px;color:#606266">截止时间：{{ homeworkDetail.commitTime || homeworkDetail.createTime }}</span>
                </div>

                <!-- 简单文本作业 -->
                <div v-if="!hasPaperQuestions" class="simple-homework">
                    <div class="hw-content">
                        <h4>作业内容</h4>
                        <p>{{ getHomeworkTextContent(homeworkDetail.content) }}</p>
                    </div>
                    <div v-if="homeworkDetail.answer" class="hw-answer">
                        <h4>参考答案</h4>
                        <p>{{ homeworkDetail.answer }}</p>
                    </div>
                </div>

                <!-- 关联试卷的作业/考试 - 逐题显示 -->
                <div v-else class="paper-homework-onebyone">
                    <div class="detail-nav-bar">
                        <div
                            v-for="(q, idx) in homeworkDetail.questions" :key="idx"
                            class="detail-nav-dot"
                            :class="{ 'detail-nav-active': detailCurrentIdx === idx }"
                            @click="detailCurrentIdx = idx"
                        >{{ idx + 1 }}</div>
                    </div>
                    <div v-for="(q, idx) in homeworkDetail.questions" :key="idx" v-show="detailCurrentIdx === idx" class="pq-item-single">
                        <div class="pq-header">
                            <strong>{{ idx + 1 }}.</strong>
                            <el-tag :type="typeTag(q.questionType)" size="mini" style="margin:0 8px">{{ typeLabel(q.questionType) }}</el-tag>
                            <span class="pq-score">({{ q.score }}分)</span>
                        </div>
                        <div class="pq-stem">{{ q.stem }}</div>

                        <div v-if="q.questionType === 'single' || q.questionType === 'multiple'" class="pq-options">
                            <div v-for="(opt, oi) in parsedOptions(q)" :key="oi" class="pq-option-single">
                                {{ String.fromCharCode(65 + oi) }}. {{ opt }}
                            </div>
                        </div>
                        <div v-if="q.questionType === 'judge'" class="pq-options">
                            <div class="pq-option-single">正确 / 错误</div>
                        </div>

                        <div class="pq-answer">
                            <div class="answer-item">
                                <span class="answer-label">正确答案：</span>
                                <span class="answer-value">{{ formatAnswer(q) }}</span>
                            </div>
                            <div v-if="q.analysis" class="answer-item">
                                <span class="answer-label">解析：</span>
                                <span class="answer-value">{{ q.analysis }}</span>
                            </div>
                        </div>
                    </div>
                    <div class="detail-nav-footer">
                        <el-button size="small" @click="prevDetailQuestion" :disabled="detailCurrentIdx <= 0">上一题</el-button>
                        <span class="nav-progress">{{ detailCurrentIdx + 1 }} / {{ homeworkDetail.questions.length }}</span>
                        <el-button size="small" @click="nextDetailQuestion" :disabled="detailCurrentIdx >= homeworkDetail.questions.length - 1">下一题</el-button>
                    </div>
                </div>
            </div>
        </el-dialog>
    </div>
</template>

<script>
import * as teacherApi from '@/api/teacher/teacherApi'
import { getPaperList, createPaper, updatePaper, deletePaper, publishPaper, getPaperDetail, previewPaper } from '@/api/teacher/examApi'
import { getQuestionList } from '@/api/teacher/examApi'

// 题型映射：后端使用整数，前端使用字符串
const TYPE_MAP = { 1: '单选题', 2: '多选题', 3: '判断题', 4: '填空题', 5: '主观题' }
const TYPE_REVERSE = { single: 1, multiple: 2, judge: 3, fill: 4, essay: 5 }

export default {
    name: "TeacherAssessment",
    data() {
        return {
            activeTab: 'publish',
            loading: false,
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
            previewData: { questions: [] },
            previewCurrentIdx: 0,
            // 作业详情
            showHomeworkDetailDialog: false,
            homeworkDetail: {},
            detailCurrentIdx: 0,
            detailLoading: false
        }
    },
    computed: {
        hasPaperQuestions() {
            return this.homeworkDetail.questions && this.homeworkDetail.questions.length > 0
        }
    },
    created() {
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
            const str = typeof type === 'number' ? this.numToType(type) : type
            return TYPE_MAP[type] || (str && TYPE_MAP[str]) || str || type
        },
        typeTag(type) {
            const str = typeof type === 'number' ? this.numToType(type) : type
            const map = { single: 'primary', multiple: 'success', judge: 'warning', fill: 'info', essay: '' }
            return map[str] || ''
        },
        numToType(n) {
            const map = { 1: 'single', 2: 'multiple', 3: 'judge', 4: 'fill', 5: 'essay' }
            return map[n] || n
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
        async doPublish() {
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
            try {
                await teacherApi.publishTask(params)
                // 如果关联了试卷，自动发布试卷（确保学生能获取到题目）
                if (this.publishForm.paperId) {
                    try { await publishPaper(this.publishForm.paperId) } catch (e) {}
                }
                this.$message.success('发布成功')
                this.showPublishDialog = false
                this.publishForm = { title: '', type: 'homework', classId: null, paperId: null, deadline: null }
                this.loadTasks()
            } catch (e) { this.$message.error('发布失败') }
        },
        async viewTaskDetail(row) {
            this.showHomeworkDetailDialog = true
            this.detailLoading = true
            this.homeworkDetail = { ...row, questions: [] }
            this.detailCurrentIdx = 0
            
            try {
                const content = row.content || ''
                let contentData = null
                try { contentData = JSON.parse(content) } catch (e) {}

                let paperId = null
                if (contentData && contentData.paperRef) {
                    paperId = contentData.paperRef
                } else if (content && content.indexOf('paperRef:') === 0) {
                    paperId = content.replace('paperRef:', '').trim()
                }

                if (paperId) {
                    paperId = parseInt(paperId, 10)
                    const res = await previewPaper(paperId)
                    const paper = (res.data && res.data.resultData) ? res.data.resultData : {}
                    this.homeworkDetail.questions = (paper.questions || []).map(q => ({
                        ...q,
                        questionType: this.numToType(q.questionType)
                    }))
                }
            } catch (e) {
                console.error('加载作业详情失败:', e)
            }
            
            this.detailLoading = false
        },
        getHomeworkTextContent(content) {
            if (!content) return ''
            try {
                const data = JSON.parse(content)
                if (data && data.type === 'exam') return '（考试类型，请查看题目列表）'
                if (data && data.paperRef) return '（关联试卷，请查看题目列表）'
                return content
            } catch (e) {
                return content
            }
        },
        parsedOptions(q) {
            if (!q.options) return []
            if (typeof q.options === 'string') {
                try {
                    const parsed = JSON.parse(q.options)
                    return parsed.map(o => o.text || o)
                } catch (e) {
                    return q.options.split(',')
                }
            }
            return Array.isArray(q.options) ? q.options.map(o => o.text || o) : []
        },
        formatAnswer(q) {
            if (!q.answer) return '-'
            if (q.questionType === 'single' || q.questionType === 'multiple') {
                return q.answer.split(',').map(a => a.trim()).join('、')
            }
            if (q.questionType === 'judge') {
                return q.answer === 'true' ? '正确' : '错误'
            }
            return q.answer
        },
        // 预览导航
        prevPreviewQuestion() {
            if (this.previewCurrentIdx > 0) this.previewCurrentIdx--
        },
        nextPreviewQuestion() {
            if (this.previewCurrentIdx < (this.previewData.questions || []).length - 1) this.previewCurrentIdx++
        },
        // 作业详情导航
        prevDetailQuestion() {
            if (this.detailCurrentIdx > 0) this.detailCurrentIdx--
        },
        nextDetailQuestion() {
            if (this.detailCurrentIdx < (this.homeworkDetail.questions || []).length - 1) this.detailCurrentIdx++
        },
        deleteTask(row) {
            this.$confirm('确定删除该任务吗？', '提示', { type: 'warning' })
                .then(() => teacherApi.deleteTask({ id: row.id }).then(() => {
                    this.$message.success('删除成功')
                    this.loadTasks()
                })).catch(() => {})
        },
        // ====== 允许再次提交 ======
        async toggleResubmit(row) {
            try {
                const res = await this.$post('/study/homework/save', {
                    id: row.id,
                    allowResubmit: row.allowResubmit
                })
                if (res.data && res.data.code === 200) {
                    this.$message.success(row.allowResubmit === 1 ? '已允许再次提交' : '已禁止再次提交')
                } else {
                    // 回滚开关状态
                    row.allowResubmit = row.allowResubmit === 1 ? 0 : 1
                    this.$message.error('操作失败，请重试')
                }
            } catch (e) {
                row.allowResubmit = row.allowResubmit === 1 ? 0 : 1
                this.$message.error('操作失败，请重试')
            }
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
        onCourseChange(val) {
            this.paperForm.chapterId = null
            if (val) {
                this.loadChapters(val)
                this.loadAvailableQuestions()
            } else {
                this.chapterList = []
            }
        },
        onCourseClear() {
            this.paperForm.chapterId = null
            this.chapterList = []
            this.availableQuestions = []
        },
        async loadAvailableQuestions() {
            const cid = this.paperForm.courseId
            if (!cid || cid === '' || cid === 0) { this.$message.warning('请先选择课程'); return }
            this.qsLoading = true
            try {
                const params = { page: 1, limit: 999, courseId: parseInt(cid) || cid, questionType: this.qsFilter.type ? (TYPE_REVERSE[this.qsFilter.type] || this.qsFilter.type) : undefined }
                const res = await getQuestionList(params)
                const resultData = res.data && res.data.resultData
                this.availableQuestions = (resultData && resultData.data) ? resultData.data : []
                if (this.availableQuestions.length === 0) {
                    console.warn('[loadAvailableQuestions] 没有找到题目, params:', params, 'response:', res.data)
                }
            } catch (e) {
                console.error('[loadAvailableQuestions] 加载失败:', e)
                this.availableQuestions = []
                this.$message.error('加载题目失败: ' + (e.message || ''))
            }
            this.qsLoading = false
        },
        onSelectionChange(rows) { this.selectedQuestionIds = rows.map(r => r.id) },
        resetPaperForm() {
            this.paperForm = { title: '', description: '', courseId: '', chapterId: '', duration: 60, totalScore: 100, questionIds: [] }
            this.isEditPaper = false; this.editPaperId = null
            this.availableQuestions = []; this.selectedQuestionIds = []; this.chapterList = []
        },
        async previewPaper(row) {
            try {
                const res = await previewPaper(row.id)
                const data = (res.data && res.data.resultData) ? res.data.resultData : {}
                this.previewData = data.questions ? data : { ...row, questions: data.questions || [], questionCount: data.questionCount || (data.questions ? data.questions.length : 0) }
                if (!this.previewData.questions) this.previewData.questions = []
                this.previewCurrentIdx = 0
                this.showPreviewDialog = true
            } catch (e) {
                this.previewData = { ...row, questions: [] }
                this.previewCurrentIdx = 0
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
            if (this.selectedQuestionIds.length === 0) {
                this.$confirm('未选择任何题目，确定要保存空试卷吗？', '提示', { type: 'warning' }).catch(() => { this.paperSaving = false; return })
            }
            this.paperSaving = true
            try {
                const params = {
                    ...this.paperForm,
                    questionIds: this.selectedQuestionIds
                }
                console.log('[savePaper] 发送参数:', { title: params.title, courseId: params.courseId, questionIds: params.questionIds })
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
            } catch (e) { this.$message.error('保存失败: ' + (e.message || '')) }
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
.main-card { border-radius: 8px; margin-bottom: 16px; }
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
.pq-item-single { background: white; border: 1px solid #e4e7ed; border-radius: 8px; padding: 20px; min-height: 280px; }
.pq-header { margin-bottom: 4px; }
.pq-score { font-size: 12px; color: #909399; }
.pq-stem { font-size: 14px; color: #303133; line-height: 1.5; margin-bottom: 12px; }
.pq-options { margin: 8px 0; padding-left: 20px; }
.pq-option { margin: 4px 0; color: #606266; }
.pq-option-single { padding: 10px 16px; background: #fafafa; border: 1px solid #e4e7ed; border-radius: 6px; margin: 6px 0; color: #606266; }
.pq-answer { margin-top: 16px; padding-top: 12px; border-top: 1px dashed #e4e7ed; }
.answer-item { margin: 4px 0; }
.answer-label { color: #909399; font-size: 13px; }
.answer-value { color: #67C23A; font-weight: 500; }
.homework-detail .detail-meta { margin-bottom: 16px; padding-bottom: 12px; border-bottom: 1px solid #e4e7ed; }
.simple-homework h4 { margin: 0 0 8px; color: #303133; }
.simple-homework p { color: #606266; line-height: 1.6; }
.hw-answer { margin-top: 16px; padding-top: 12px; border-top: 1px dashed #e4e7ed; }
.hw-answer h4 { margin: 0 0 8px; color: #303133; }
.hw-answer p { color: #67C23A; line-height: 1.6; }

/* 预览导航条 */
.preview-nav-bar, .detail-nav-bar { display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 16px; padding-bottom: 12px; border-bottom: 1px solid #e4e7ed; }
.preview-nav-dot, .detail-nav-dot {
  width: 30px; height: 30px; display: flex; align-items: center; justify-content: center;
  border-radius: 50%; font-size: 13px; cursor: pointer; font-weight: 600;
  background: #f0f2f5; color: #909399; border: 2px solid transparent; transition: all 0.2s;
}
.preview-nav-dot:hover, .detail-nav-dot:hover { background: #d9ecff; color: #409EFF; }
.preview-nav-active, .detail-nav-active { border-color: #303133; background: #409EFF; color: white; }
.preview-nav-footer, .detail-nav-footer { display: flex; justify-content: center; align-items: center; gap: 16px; margin-top: 20px; padding-top: 14px; border-top: 1px solid #e4e7ed; }
.nav-progress { font-size: 14px; color: #909399; }
.paper-homework-onebyone { margin-top: 12px; }
</style>