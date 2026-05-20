<template>
  <div class="teacher-grading">
    <div class="page-header">
      <h2><i class="el-icon-document-checked"></i> 批改与评分</h2>
    </div>

    <el-tabs v-model="activeTab" type="border-card">
      <!-- ========== Tab 1: 试卷批改 ========== -->
      <el-tab-pane label="试卷批改" name="exam">
        <el-card shadow="never" class="main-card">
          <div slot="header" class="card-header">
            <span>待批改列表</span>
            <el-select v-model="filterPaperId" placeholder="筛选试卷" clearable style="width:200px">
              <el-option v-for="p in paperList" :key="p.id" :label="p.title" :value="p.id" />
            </el-select>
          </div>
          <el-table :data="examPendingList" v-loading="examLoading" stripe style="width:100%">
            <el-table-column prop="studentName" label="学生姓名" width="120" />
            <el-table-column prop="paperTitle" label="试卷名称" min-width="200" />
            <el-table-column prop="className" label="班级" width="140" />
            <el-table-column prop="submitTime" label="提交时间" width="170" align="center" />
            <el-table-column label="客观题得分" width="100" align="center">
              <template slot-scope="scope">
                <span :style="{color: scope.row.autoScore !== null && scope.row.autoScore !== undefined ? '#67C23A' : '#909399'}">
                  {{ scope.row.autoScore !== null && scope.row.autoScore !== undefined ? scope.row.autoScore + '分' : '待阅' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="80" align="center">
              <template slot-scope="scope">
                <el-tag :type="scope.row.reviewStatus === 'pending' ? 'warning' : 'success'" size="mini">
                  {{ scope.row.reviewStatus === 'pending' ? '待批' : '已批' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" align="center">
              <template slot-scope="scope">
                <el-button type="primary" size="mini" @click="startExamGrading(scope.row)">批改</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!examLoading && examPendingList.length === 0" description="暂无待批改记录" />
        </el-card>
      </el-tab-pane>

      <!-- ========== Tab 2: 作业批改 ========== -->
      <el-tab-pane label="作业批改" name="homework">
        <!-- 步骤1: 选择作业 -->
        <el-card shadow="never" class="main-card" v-if="!selectedHomeworkId">
          <div slot="header" class="card-header">
            <span>已发布的作业</span>
          </div>
          <el-table :data="homeworkList" v-loading="hwLoading" stripe style="width:100%">
            <el-table-column prop="title" label="作业标题" min-width="200" />
            <el-table-column prop="courseName" label="课程" width="140" />
            <el-table-column prop="className" label="班级" width="120" />
            <el-table-column prop="commitTime" label="截止时间" width="170" align="center" />
            <el-table-column label="操作" width="120" align="center">
              <template slot-scope="scope">
                <el-button type="primary" size="mini" @click="selectHomework(scope.row)">查看提交</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!hwLoading && homeworkList.length === 0" description="暂无已发布的作业" />
        </el-card>

        <!-- 步骤2: 查看该作业的学生提交列表 -->
        <el-card shadow="never" class="main-card" v-if="selectedHomeworkId">
          <div slot="header" class="card-header">
            <span>
              <el-button type="text" icon="el-icon-arrow-left" @click="backToHomeworkList" style="margin-right:8px">返回</el-button>
              作业提交列表 - {{ selectedHomeworkTitle }}
            </span>
          </div>
          <el-table :data="submissionList" v-loading="subLoading" stripe style="width:100%">
            <el-table-column prop="studentName" label="学生" width="120" />
            <el-table-column label="状态" width="100" align="center">
              <template slot-scope="scope">
                <el-tag v-if="scope.row.mode === '2'" type="danger" size="mini">已打回</el-tag>
                <el-tag v-else-if="scope.row.mode === '1'" type="success" size="mini">已批改</el-tag>
                <el-tag v-else type="warning" size="mini">待批改</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="completionTime" label="提交时间" width="170" align="center" />
            <el-table-column label="得分" width="80" align="center">
              <template slot-scope="scope">{{ scope.row.score || '-' }}</template>
            </el-table-column>
            <el-table-column label="操作" width="160" align="center">
              <template slot-scope="scope">
                <el-button type="primary" size="mini" @click="openHomeworkGrading(scope.row)">批改</el-button>
                <el-button type="text" size="mini" @click="viewHomeworkReply(scope.row)">查看</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!subLoading && submissionList.length === 0" description="暂无学生提交" />
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- ========== 试卷批改对话框 ========== -->
    <el-dialog :title="'批改 - ' + examGradingStudentName" :visible.sync="showExamGradingDialog" width="800px" top="5vh" @closed="resetExamGrading">
      <div class="grading-body">
        <div class="grading-info">试卷：{{ examGradingPaperTitle }} | 提交时间：{{ examGradingSubmitTime }}</div>
        <el-divider />
        <div v-for="(q, idx) in examGradingQuestions" :key="idx" class="grading-question" :class="{'is-reviewed': q.reviewed}">
          <div class="gq-header">
            <strong>第{{ idx + 1 }}题</strong>
            <el-tag :type="typeTag(q.questionType)" size="mini">{{ typeLabel(q.questionType) }}</el-tag>
            <span class="gq-score">分值：{{ q.score }}分</span>
          </div>
          <div class="gq-stem">{{ q.stem }}</div>
          <div class="gq-correct-answer" v-if="q.correctAnswer"><span class="label">参考答案：</span>{{ q.correctAnswer }}</div>
          <div class="gq-student-answer">
            <span class="label">学生答案：</span>
            <span :class="{'answer-correct': q.isCorrect, 'answer-wrong': q.isCorrect === false}">{{ q.studentAnswer || '未作答' }}</span>
            <el-tag v-if="q.isCorrect === true" type="success" size="mini" style="margin-left:8px">正确</el-tag>
            <el-tag v-if="q.isCorrect === false" type="danger" size="mini" style="margin-left:8px">错误</el-tag>
          </div>
          <div class="gq-review" v-if="q.questionType === 'essay' || q.questionType === 'fill' || q.isCorrect === undefined">
            <el-row :gutter="16">
              <el-col :span="8">
                <el-input-number v-model="q.manualScore" :min="0" :max="q.score" size="small" :precision="1" />
                <span style="margin-left:4px;color:#909399">/ {{ q.score }}分</span>
              </el-col>
              <el-col :span="16">
                <el-input v-model="q.remark" placeholder="评语（可选）" size="small" style="width:100%" />
              </el-col>
            </el-row>
          </div>
          <div class="gq-auto-result" v-if="q.questionType !== 'essay' && q.isCorrect !== undefined">
            <span class="label">自动评分：</span><span>{{ q.autoScore }}分</span>
          </div>
        </div>
      </div>
      <span slot="footer">
        <el-button @click="showExamGradingDialog = false">取消</el-button>
        <el-button type="primary" @click="submitExamGrading" :loading="examGradingLoading">提交批改</el-button>
      </span>
    </el-dialog>

    <!-- ========== 作业批改对话框 ========== -->
    <el-dialog :title="'批改作业 - ' + hwGradingStudentName" :visible.sync="showHomeworkGradingDialog" width="700px" top="5vh" @closed="resetHomeworkGrading">
      <div class="grading-body">
        <div class="grading-info">作业：{{ hwGradingTitle }} | 提交时间：{{ hwGradingSubmitTime }}</div>
        <el-divider />
        <div class="student-answer-box">
          <h4>学生作答内容</h4>
          <div class="answer-content">{{ hwGradingReply || '（无作答内容）' }}</div>
        </div>
        <el-divider />
        <el-form label-width="80px">
          <el-form-item label="得分">
            <el-input-number v-model="hwGradingScore" :min="0" :max="100" :precision="1" />
          </el-form-item>
          <el-form-item label="评语">
            <el-input type="textarea" v-model="hwGradingRemark" :rows="3" placeholder="可选评语" />
          </el-form-item>
        </el-form>
      </div>
      <span slot="footer" style="display:flex;justify-content:space-between">
        <div>
          <el-button type="danger" icon="el-icon-refresh-left" @click="rejectHomework" :loading="gradingSubmitting">打回重做</el-button>
        </div>
        <div>
          <el-button @click="showHomeworkGradingDialog = false">取消</el-button>
          <el-button type="success" icon="el-icon-check" @click="approveHomework" :loading="gradingSubmitting">通过批改</el-button>
        </div>
      </span>
    </el-dialog>

    <!-- 查看作业作答对话框 -->
    <el-dialog title="查看作业作答" :visible.sync="showViewDialog" width="600px" top="5vh">
      <div class="grading-info">
        学生：{{ viewStudentName }} | 作业：{{ viewHomeworkTitle }} | 提交时间：{{ viewSubmitTime }}
      </div>
      <el-divider />
      <div class="student-answer-box">
        <h4>作答内容</h4>
        <div class="answer-content">{{ viewReply || '（无作答内容）' }}</div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getPaperStudentAnswers, reviewAnswer, batchReviewAnswers, getPaperList, getStudentAnswerDetail } from '@/api/teacher/examApi'
import { getPublishedTasks } from '@/api/teacher/teacherApi'
import Cookies from 'js-cookie'

export default {
  name: 'TeacherGrading',
  data() {
    return {
      activeTab: 'exam',
      // ===== 试卷批改 =====
      examLoading: false,
      paperList: [],
      filterPaperId: '',
      examPendingList: [],
      showExamGradingDialog: false,
      examGradingLoading: false,
      examGradingStudentName: '',
      examGradingPaperTitle: '',
      examGradingSubmitTime: '',
      examGradingRecordId: null,
      examGradingQuestions: [],
      // ===== 作业批改 =====
      hwLoading: false,
      homeworkList: [],
      selectedHomeworkId: null,
      selectedHomeworkTitle: '',
      subLoading: false,
      submissionList: [],
      showHomeworkGradingDialog: false,
      gradingSubmitting: false,
      hwGradingRecordId: null,
      hwGradingStudentName: '',
      hwGradingTitle: '',
      hwGradingSubmitTime: '',
      hwGradingReply: '',
      hwGradingScore: 0,
      hwGradingRemark: '',
      showViewDialog: false,
      viewStudentName: '',
      viewHomeworkTitle: '',
      viewSubmitTime: '',
      viewReply: ''
    }
  },
  created() {
    this.loadPapers()
    this.loadExamPending()
    this.loadHomeworkList()
  },
  methods: {
    typeLabel(t) { return { single: '单选', multiple: '多选', judge: '判断', fill: '填空', essay: '主观' }[t] || t },
    typeTag(t) { return { single: 'primary', multiple: 'success', judge: 'warning', fill: 'info', essay: '' }[t] || '' },
    // ---------- 试卷批改 ----------
    async loadPapers() {
      try {
        const res = await getPaperList({ page: 1, limit: 999 })
        this.paperList = (res.data && res.data.data) ? res.data.data : []
      } catch (e) { this.paperList = [] }
    },
    async loadExamPending() {
      this.examLoading = true
      try {
        const params = {}
        if (this.filterPaperId) params.paperId = this.filterPaperId
        const res = await getPaperStudentAnswers(this.filterPaperId || undefined)
        this.examPendingList = (res.data && res.data.data) ? res.data.data : []
      } catch (e) { this.examPendingList = [] }
      this.examLoading = false
    },
    async startExamGrading(row) {
      this.examGradingStudentName = row.studentName
      this.examGradingPaperTitle = row.paperTitle
      this.examGradingSubmitTime = row.submitTime
      this.examGradingRecordId = row.id
      try {
        const res = await getStudentAnswerDetail({ paperId: row.paperId, studentId: row.studentId })
        const detail = res.data || {}
        this.examGradingQuestions = (detail.questions || []).map(q => ({
          ...q, manualScore: q.manualScore || q.autoScore || 0, remark: q.remark || '', reviewed: q.reviewed || false
        }))
        if (this.examGradingQuestions.length === 0) { this.$message.info('未获取到答题详情'); return }
        this.showExamGradingDialog = true
      } catch (e) { this.$message.error('获取答题详情失败') }
    },
    async submitExamGrading() {
      this.examGradingLoading = true
      try {
        const reviewList = this.examGradingQuestions.filter(q => q.reviewed !== true).map(q => ({
          recordId: q.recordId, score: q.manualScore !== undefined ? q.manualScore : q.autoScore, remark: q.remark || ''
        }))
        if (reviewList.length === 0) { this.$message.info('没有需要批改的题目'); this.examGradingLoading = false; return }
        await batchReviewAnswers(reviewList)
        this.$message.success('批改提交成功')
        this.showExamGradingDialog = false
        this.loadExamPending()
      } catch (e) { this.$message.error('批改提交失败') }
      this.examGradingLoading = false
    },
    resetExamGrading() { this.examGradingQuestions = []; this.examGradingStudentName = '' },
    // ---------- 作业批改 ----------
    async loadHomeworkList() {
      this.hwLoading = true
      const userId = parseInt(Cookies.get('userId'))
      try {
        const res = await getPublishedTasks({ page: 1, pageSize: 50, roleId: 2, userId })
        const data = res.data && res.data.resultData
        this.homeworkList = (data && data.data) ? data.data : []
      } catch (e) { this.homeworkList = [] }
      this.hwLoading = false
    },
    selectHomework(row) {
      this.selectedHomeworkId = row.id
      this.selectedHomeworkTitle = row.title
      this.loadSubmissions(row.id)
    },
    backToHomeworkList() {
      this.selectedHomeworkId = null
      this.selectedHomeworkTitle = ''
      this.submissionList = []
    },
    async loadSubmissions(homeworkId) {
      this.subLoading = true
      try {
        const res = await this.$post('/study/userdohomework/byHomework', { homeworkId })
        this.submissionList = (res.data && res.data.resultData) ? res.data.resultData : []
      } catch (e) { this.submissionList = [] }
      this.subLoading = false
    },
    openHomeworkGrading(row) {
      this.hwGradingRecordId = row.recordId
      this.hwGradingStudentName = row.studentName
      this.hwGradingTitle = row.homeworkTitle
      this.hwGradingSubmitTime = row.completionTime
      this.hwGradingReply = row.reply || ''
      this.hwGradingScore = row.score ? parseFloat(row.score) : 0
      this.hwGradingRemark = row.remark || ''
      this.showHomeworkGradingDialog = true
    },
    async approveHomework() {
      this.gradingSubmitting = true
      try {
        await this.$post('/study/userdohomework/grade', {
          recordId: this.hwGradingRecordId, mode: '1', score: this.hwGradingScore, remark: this.hwGradingRemark
        })
        this.$message.success('批改通过')
        this.showHomeworkGradingDialog = false
        this.loadSubmissions(this.selectedHomeworkId)
      } catch (e) { this.$message.error('操作失败') }
      this.gradingSubmitting = false
    },
    async rejectHomework() {
      this.gradingSubmitting = true
      try {
        await this.$post('/study/userdohomework/grade', {
          recordId: this.hwGradingRecordId, mode: '2', score: 0, remark: this.hwGradingRemark || '被打回，请重新提交'
        })
        this.$message.success('已打回，学生可重新提交')
        this.showHomeworkGradingDialog = false
        this.loadSubmissions(this.selectedHomeworkId)
      } catch (e) { this.$message.error('操作失败') }
      this.gradingSubmitting = false
    },
    resetHomeworkGrading() {
      this.hwGradingRecordId = null
      this.hwGradingReply = ''
      this.hwGradingScore = 0
      this.hwGradingRemark = ''
    },
    viewHomeworkReply(row) {
      this.viewStudentName = row.studentName
      this.viewHomeworkTitle = row.homeworkTitle
      this.viewSubmitTime = row.completionTime
      this.viewReply = row.reply || ''
      this.showViewDialog = true
    }
  },
  watch: {
    filterPaperId() { this.loadExamPending() }
  }
}
</script>

<style scoped>
.teacher-grading { padding: 24px; background: #f5f7fa; min-height: 100vh; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { margin: 0; font-size: 22px; color: #303133; }
.page-header h2 i { margin-right: 8px; color: #409EFF; }
.main-card { border-radius: 8px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.grading-body { max-height: 65vh; overflow-y: auto; }
.grading-info { color: #606266; font-size: 14px; }
.grading-question { background: #fafafa; border: 1px solid #e4e7ed; border-radius: 6px; padding: 12px 16px; margin-bottom: 12px; }
.grading-question.is-reviewed { border-color: #67C23A; background: #f0f9eb; }
.gq-header { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.gq-score { font-size: 12px; color: #909399; margin-left: auto; }
.gq-stem { font-size: 14px; color: #303133; line-height: 1.6; margin-bottom: 8px; }
.label { font-weight: 600; color: #606266; }
.gq-correct-answer, .gq-student-answer { margin-bottom: 6px; font-size: 14px; }
.answer-correct { color: #67C23A; }
.answer-wrong { color: #F56C6C; }
.gq-review { margin-top: 8px; padding-top: 8px; border-top: 1px dashed #e4e7ed; }
.gq-auto-result { font-size: 14px; margin-top: 4px; }
.student-answer-box { background: #fafafa; border-radius: 6px; padding: 12px 16px; }
.student-answer-box h4 { margin: 0 0 8px; font-size: 14px; color: #303133; }
.answer-content { font-size: 14px; line-height: 1.8; color: #606266; white-space: pre-wrap; }
</style>
