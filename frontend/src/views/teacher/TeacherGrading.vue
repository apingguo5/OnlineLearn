<template>
  <div class="teacher-grading">
    <div class="page-header">
      <h2><i class="el-icon-document-checked"></i> 批改与评分</h2>
    </div>

    <!-- 待批改试卷列表 -->
    <el-card shadow="never" class="main-card">
      <div slot="header" class="card-header">
        <span>待批改列表</span>
        <el-select v-model="filterPaperId" placeholder="筛选试卷" clearable style="width:200px">
          <el-option v-for="p in paperList" :key="p.id" :label="p.title" :value="p.id" />
        </el-select>
      </div>
      <el-table :data="pendingList" v-loading="loading" stripe style="width:100%">
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
            <el-button type="primary" size="mini" @click="startGrading(scope.row)">批改</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && pendingList.length === 0" description="暂无待批改记录" />
    </el-card>

    <!-- 批改对话框 -->
    <el-dialog :title="'批改 - ' + gradingStudentName" :visible.sync="showGradingDialog" width="800px" top="5vh" @closed="resetGrading">
      <div class="grading-body">
        <div class="grading-info">
          试卷：{{ gradingPaperTitle }} | 提交时间：{{ gradingSubmitTime }}
        </div>
        <el-divider />
        <div v-for="(q, idx) in gradingQuestions" :key="idx" class="grading-question" :class="{'is-reviewed': q.reviewed}">
          <div class="gq-header">
            <strong>第{{ idx + 1 }}题</strong>
            <el-tag :type="typeTag(q.questionType)" size="mini">{{ typeLabel(q.questionType) }}</el-tag>
            <span class="gq-score">分值：{{ q.score }}分</span>
          </div>
          <div class="gq-stem">{{ q.stem }}</div>
          <div class="gq-correct-answer" v-if="q.correctAnswer">
            <span class="label">参考答案：</span>{{ q.correctAnswer }}
          </div>
          <div class="gq-student-answer">
            <span class="label">学生答案：</span>
            <span :class="{'answer-correct': q.isCorrect, 'answer-wrong': q.isCorrect === false}">
              {{ q.studentAnswer || '未作答' }}
            </span>
            <el-tag v-if="q.isCorrect === true" type="success" size="mini" style="margin-left:8px">正确</el-tag>
            <el-tag v-if="q.isCorrect === false" type="danger" size="mini" style="margin-left:8px">错误</el-tag>
          </div>
          <!-- 主观题需要手动评分 -->
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
            <span class="label">自动评分：</span>
            <span>{{ q.autoScore }}分</span>
          </div>
        </div>
      </div>
      <span slot="footer">
        <el-button @click="showGradingDialog = false">取消</el-button>
        <el-button type="primary" @click="submitGrading" :loading="gradingLoading">提交批改</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getPaperStudentAnswers, reviewAnswer, batchReviewAnswers, getPaperList, getStudentAnswerDetail } from '@/api/teacher/examApi'

export default {
  name: 'TeacherGrading',
  data() {
    return {
      loading: false,
      paperList: [],
      filterPaperId: '',
      pendingList: [],
      showGradingDialog: false,
      gradingLoading: false,
      gradingStudentName: '',
      gradingPaperTitle: '',
      gradingSubmitTime: '',
      gradingRecordId: null,
      gradingQuestions: []
    }
  },
  created() { this.loadPapers(); this.loadPending() },
  methods: {
    typeLabel(t) { return { single: '单选', multiple: '多选', judge: '判断', fill: '填空', essay: '主观' }[t] || t },
    typeTag(t) { return { single: 'primary', multiple: 'success', judge: 'warning', fill: 'info', essay: '' }[t] || '' },
    async loadPapers() {
      try {
        const res = await getPaperList({ page: 1, limit: 999 })
        this.paperList = (res.data && res.data.data) ? res.data.data : []
      } catch (e) { this.paperList = [] }
    },
    async loadPending() {
      this.loading = true
      try {
        const params = {}
        if (this.filterPaperId) params.paperId = this.filterPaperId
        const res = await getPaperStudentAnswers(this.filterPaperId || undefined)
        this.pendingList = (res.data && res.data.data) ? res.data.data : []
      } catch (e) { this.pendingList = [] }
      this.loading = false
    },
    async startGrading(row) {
      this.gradingStudentName = row.studentName
      this.gradingPaperTitle = row.paperTitle
      this.gradingSubmitTime = row.submitTime
      this.gradingRecordId = row.id
      try {
        const res = await getStudentAnswerDetail({ paperId: row.paperId, studentId: row.studentId })
        const detail = res.data || {}
        this.gradingQuestions = (detail.questions || []).map(q => ({
          ...q,
          manualScore: q.manualScore || q.autoScore || 0,
          remark: q.remark || '',
          reviewed: q.reviewed || false
        }))
        if (this.gradingQuestions.length === 0) {
          this.$message.info('未获取到答题详情，请检查数据')
          return
        }
        this.showGradingDialog = true
      } catch (e) {
        this.$message.error('获取答题详情失败')
      }
    },
    async submitGrading() {
      this.gradingLoading = true
      try {
        const reviewList = this.gradingQuestions
          .filter(q => q.reviewed !== true)
          .map(q => ({
            recordId: q.recordId,
            score: q.manualScore !== undefined ? q.manualScore : q.autoScore,
            remark: q.remark || ''
          }))
        if (reviewList.length === 0) {
          this.$message.info('没有需要批改的题目')
          this.gradingLoading = false
          return
        }
        await batchReviewAnswers(reviewList)
        this.$message.success('批改提交成功')
        this.showGradingDialog = false
        this.loadPending()
      } catch (e) {
        this.$message.error('批改提交失败')
      }
      this.gradingLoading = false
    },
    resetGrading() {
      this.gradingQuestions = []
      this.gradingStudentName = ''
      this.gradingPaperTitle = ''
    }
  },
  watch: {
    filterPaperId() { this.loadPending() }
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
</style>