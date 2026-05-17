<template>
  <div class="student-exam">
    <!-- 试卷选择列表 -->
    <div v-if="!currentPaper">
      <div class="page-header">
        <h2><i class="el-icon-reading"></i> 在线考试</h2>
      </div>
      <el-card shadow="never" class="main-card">
        <el-empty v-if="loading" description="加载中..." />
        <el-table v-else :data="paperList" v-loading="loading" stripe style="width:100%">
          <el-table-column prop="title" label="试卷名称" min-width="200" />
          <el-table-column prop="courseName" label="课程" width="160" />
          <el-table-column prop="totalScore" label="总分" width="70" align="center" />
          <el-table-column prop="duration" label="时长(分钟)" width="100" align="center" />
          <el-table-column label="状态" width="100" align="center">
            <template slot-scope="scope">
              <el-tag :type="statusTag(scope.row)" size="mini">{{ statusLabel(scope.row) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" align="center">
            <template slot-scope="scope">
              <el-button v-if="scope.row.status === 'published'" type="primary" size="mini" @click="startExam(scope.row)">开始答题</el-button>
              <el-button v-else-if="scope.row.recordStatus === 'submitted'" type="success" size="mini" @click="viewResult(scope.row)">查看成绩</el-button>
              <el-button v-else-if="scope.row.recordStatus === 'draft'" type="warning" size="mini" @click="continueExam(scope.row)">继续答题</el-button>
              <span v-else style="color:#909399;font-size:12px">--</span>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination
          @size-change="s=>{limit=s;loadPapers()}"
          @current-change="p=>{page=p;loadPapers()}"
          :current-page="page"
          :page-sizes="[10, 20, 50]"
          :page-size="limit"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          style="margin-top:16px;text-align:right"
        />
      </el-card>
    </div>

    <!-- 答题界面 -->
    <div v-else class="exam-take">
      <div class="exam-header">
        <div class="exam-title">{{ currentPaper.title }}</div>
        <div class="exam-timer" :class="{'timer-warning': timeWarning}">
          <i class="el-icon-time"></i> {{ formattedTime }}
        </div>
        <div class="exam-actions">
          <el-button size="small" @click="saveDraft">暂存</el-button>
          <el-button type="primary" size="small" @click="submitExam" :disabled="submitting">{{ submitting ? '提交中...' : '交卷' }}</el-button>
        </div>
      </div>
      <div class="exam-body">
        <div class="exam-sidebar">
          <div class="sidebar-info">
            <p>总分：{{ totalScore }}分</p>
            <p>共 {{ questions.length }} 题</p>
          </div>
          <el-divider />
          <div class="question-nav">
            <div
              v-for="(q, idx) in questions" :key="idx"
              class="q-nav-item"
              :class="{
                'nav-answered': isAnswered(q),
                'nav-current': currentQuestionIdx === idx,
                'nav-flagged': q.flagged
              }"
              @click="goToQuestion(idx)"
            >
              {{ idx + 1 }}
            </div>
          </div>
          <el-divider />
          <div class="legend">
            <span><span class="legend-dot dot-answered"></span> 已答</span>
            <span><span class="legend-dot dot-unanswered"></span> 未答</span>
          </div>
        </div>
        <div class="exam-content">
          <div class="question-wrapper" v-for="(q, idx) in questions" :key="idx" v-show="currentQuestionIdx === idx">
            <div class="q-type-bar">
              <el-tag :type="typeTag(q.questionType)" size="small">{{ typeLabel(q.questionType) }}</el-tag>
              <span class="q-score">({{ q.score }}分)</span>
            </div>
            <div class="q-stem"><strong>第{{ idx + 1 }}题：</strong>{{ q.stem }}</div>

            <!-- 单选题 -->
            <el-radio-group v-model="q.studentAnswer" v-if="q.questionType === 'single'" class="q-options">
              <el-radio v-for="(opt, oi) in parsedOptions(q)" :key="oi" :label="String.fromCharCode(65 + oi)" class="q-option">
                {{ String.fromCharCode(65 + oi) }}. {{ opt }}
              </el-radio>
            </el-radio-group>

            <!-- 多选题 -->
            <el-checkbox-group v-model="q.studentAnswer" v-if="q.questionType === 'multiple'" class="q-options">
              <el-checkbox v-for="(opt, oi) in parsedOptions(q)" :key="oi" :label="String.fromCharCode(65 + oi)" class="q-option">
                {{ String.fromCharCode(65 + oi) }}. {{ opt }}
              </el-checkbox>
            </el-checkbox-group>

            <!-- 判断题 -->
            <el-radio-group v-model="q.studentAnswer" v-if="q.questionType === 'judge'" class="q-options">
              <el-radio label="true" class="q-option">True. 正确</el-radio>
              <el-radio label="false" class="q-option">False. 错误</el-radio>
            </el-radio-group>

            <!-- 填空题 -->
            <el-input
              v-if="q.questionType === 'fill'"
              type="textarea"
              v-model="q.studentAnswer"
              :rows="2"
              placeholder="请输入答案，多个空用 | 分隔"
              class="q-textarea"
            />

            <!-- 主观题 -->
            <el-input
              v-if="q.questionType === 'essay'"
              type="textarea"
              v-model="q.studentAnswer"
              :rows="6"
              placeholder="请输入你的答案..."
              class="q-textarea"
            />

            <div class="q-footer">
              <el-button size="mini" :type="q.flagged ? 'warning' : 'default'" @click="q.flagged = !q.flagged">
                <i :class="q.flagged ? 'el-icon-star-on' : 'el-icon-star-off'"></i>
                {{ q.flagged ? '取消标记' : '标记' }}
              </el-button>
            </div>
          </div>

          <!-- 导航按钮 -->
          <div class="question-navigation">
            <el-button @click="prevQuestion" :disabled="currentQuestionIdx <= 0">上一题</el-button>
            <span class="nav-progress">第 {{ currentQuestionIdx + 1 }} / {{ questions.length }} 题</span>
            <el-button @click="nextQuestion" :disabled="currentQuestionIdx >= questions.length - 1">下一题</el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getStudentPapers, getPaperDetail, startExam, saveAnswerDraft, submitAnswer } from '@/api/teacher/examApi'

export default {
  name: 'StudentExam',
  data() {
    return {
      loading: false,
      paperList: [],
      total: 0, page: 1, limit: 10,
      // Exam state
      currentPaper: null,
      questions: [],
      currentQuestionIdx: 0,
      timer: null,
      remainingTime: 0,
      totalScore: 0,
      submitting: false,
      editPaperId: null
    }
  },
  computed: {
    formattedTime() {
      const m = Math.floor(this.remainingTime / 60)
      const s = this.remainingTime % 60
      return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
    },
    timeWarning() {
      return this.remainingTime <= 300
    }
  },
  created() { this.loadPapers() },
  beforeDestroy() { this.clearTimer() },
  methods: {
    typeLabel(t) { return { single: '单选', multiple: '多选', judge: '判断', fill: '填空', essay: '主观' }[t] || t },
    typeTag(t) { return { single: 'primary', multiple: 'success', judge: 'warning', fill: 'info', essay: '' }[t] || '' },
    statusTag(row) {
      if (row.recordStatus === 'submitted') return 'success'
      if (row.recordStatus === 'draft') return 'warning'
      return 'primary'
    },
    statusLabel(row) {
      if (row.recordStatus === 'submitted') return '已提交'
      if (row.recordStatus === 'draft') return '未完成'
      return '待答题'
    },
    parsedOptions(q) {
      if (!q.options) return []
      if (typeof q.options === 'string') {
        try { return JSON.parse(q.options) } catch (e) { return q.options.split(',') }
      }
      return Array.isArray(q.options) ? q.options : []
    },
    isAnswered(q) {
      if (!q.studentAnswer) return false
      if (Array.isArray(q.studentAnswer) && q.studentAnswer.length === 0) return false
      return true
    },
    async loadPapers() {
      this.loading = true
      try {
        const res = await getStudentPapers({ page: this.page, limit: this.limit })
        this.paperList = (res.data && res.data.data) ? res.data.data : []
        this.total = (res.data && res.data.total) ? res.data.total : 0
      } catch (e) { this.paperList = [] }
      this.loading = false
    },
    async startExam(row) {
      this.loading = true
      try {
        const res = await startExam({ paperId: row.id, studentId: this.$cookies.get('userId') })
        if (res.code !== 0) { this.$message.error(res.msg || '开始考试失败'); return }
        this.editPaperId = row.id
        await this.loadExamQuestions(row.id)
        this.remainingTime = (row.duration || 60) * 60
        this.startTimer()
      } catch (e) { this.$message.error('开始考试失败') }
      this.loading = false
    },
    async continueExam(row) {
      try {
        this.editPaperId = row.id
        await this.loadExamQuestions(row.id, true)
        this.remainingTime = (row.duration || 60) * 60
        this.startTimer()
      } catch (e) { this.$message.error('加载答题记录失败') }
    },
    async loadExamQuestions(paperId, loadRecord) {
      try {
        const res = await getPaperDetail(paperId)
        this.currentPaper = res.data || {}
        this.totalScore = this.currentPaper.totalScore || 0
        this.questions = (this.currentPaper.questions || []).map(q => ({
          ...q,
          studentAnswer: loadRecord ? (q.studentAnswer || (q.questionType === 'multiple' ? [] : '')) : (q.questionType === 'multiple' ? [] : ''),
          flagged: false
        }))
        this.currentQuestionIdx = 0
      } catch (e) { this.$message.error('加载试卷失败') }
    },
    startTimer() {
      this.clearTimer()
      this.timer = setInterval(() => {
        if (this.remainingTime <= 0) {
          this.submitExam(true)
          return
        }
        this.remainingTime--
      }, 1000)
    },
    clearTimer() {
      if (this.timer) { clearInterval(this.timer); this.timer = null }
    },
    goToQuestion(idx) { this.currentQuestionIdx = idx },
    prevQuestion() { if (this.currentQuestionIdx > 0) this.currentQuestionIdx-- },
    nextQuestion() { if (this.currentQuestionIdx < this.questions.length - 1) this.currentQuestionIdx++ },
    async saveDraft() {
      try {
        const answers = this.questions.map(q => ({
          questionId: q.id,
          answer: Array.isArray(q.studentAnswer) ? q.studentAnswer.join(',') : (q.studentAnswer || '')
        }))
        await saveAnswerDraft({ paperId: this.editPaperId, studentId: this.$cookies.get('userId'), answers })
        this.$message.success('已暂存')
      } catch (e) { this.$message.error('暂存失败') }
    },
    async submitExam(auto = false) {
      if (!auto) {
        const hasUnanswered = this.questions.some(q => !this.isAnswered(q))
        if (hasUnanswered) {
          try {
            await this.$confirm('还有未完成的题目，确定要交卷吗？', '提示', { type: 'warning' })
          } catch (e) { return }
        } else {
          try {
            await this.$confirm('确定要提交试卷吗？提交后无法修改。', '提示', { type: 'warning' })
          } catch (e) { return }
        }
      }
      this.submitting = true
      this.clearTimer()
      try {
        const answers = this.questions.map(q => ({
          questionId: q.id,
          answer: Array.isArray(q.studentAnswer) ? q.studentAnswer.join(',') : (q.studentAnswer || '')
        }))
        await submitAnswer({ paperId: this.editPaperId, studentId: this.$cookies.get('userId'), answers })
        this.$message.success('交卷成功！')
        this.currentPaper = null
        this.questions = []
        this.loadPapers()
      } catch (e) { this.$message.error('交卷失败'); this.startTimer() }
      this.submitting = false
    },
    viewResult(row) {
      this.$router.push({ name: 'ExamResult', query: { paperId: row.id } })
    }
  }
}
</script>

<style scoped>
.student-exam { padding: 24px; background: #f5f7fa; min-height: 100vh; }
.page-header { margin-bottom: 20px; }
.page-header h2 { margin: 0; font-size: 22px; color: #303133; }
.page-header h2 i { margin-right: 8px; color: #409EFF; }
.main-card { border-radius: 8px; }

/* Exam taking */
.exam-take { display: flex; flex-direction: column; height: calc(100vh - 48px); }
.exam-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 12px 24px; background: white; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  margin-bottom: 16px; position: sticky; top: 0; z-index: 10;
}
.exam-title { font-size: 18px; font-weight: 600; color: #303133; }
.exam-timer { font-size: 20px; font-weight: 700; color: #409EFF; }
.exam-timer.timer-warning { color: #F56C6C; animation: pulse 1s infinite; }
@keyframes pulse { 0%,100%{opacity:1} 50%{opacity:0.5} }
.exam-actions { display: flex; gap: 8px; }
.exam-body { display: flex; gap: 16px; flex: 1; overflow: hidden; }
.exam-sidebar { width: 120px; background: white; border-radius: 8px; padding: 16px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.sidebar-info p { margin: 0 0 8px; font-size: 14px; color: #606266; }
.question-nav { display: flex; flex-wrap: wrap; gap: 6px; }
.q-nav-item {
  width: 32px; height: 32px; display: flex; align-items: center; justify-content: center;
  border-radius: 50%; font-size: 13px; cursor: pointer;
  background: #f0f2f5; color: #606266; transition: all 0.2s;
}
.q-nav-item:hover { background: #d9ecff; }
.q-nav-item.nav-answered { background: #409EFF; color: white; }
.q-nav-item.nav-current { border: 2px solid #303133; font-weight: 700; }
.q-nav-item.nav-flagged { border: 2px solid #E6A23C; }
.legend { display: flex; flex-direction: column; gap: 4px; font-size: 12px; color: #909399; }
.legend-dot { display: inline-block; width: 10px; height: 10px; border-radius: 50%; margin-right: 4px; }
.dot-answered { background: #409EFF; }
.dot-unanswered { background: #f0f2f5; border: 1px solid #dcdfe6; }

.exam-content { flex: 1; background: white; border-radius: 8px; padding: 24px; overflow-y: auto; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.question-wrapper { }
.q-type-bar { margin-bottom: 12px; }
.q-score { font-size: 14px; color: #909399; margin-left: 8px; }
.q-stem { font-size: 16px; line-height: 1.6; margin-bottom: 20px; color: #303133; }
.q-options { display: flex; flex-direction: column; gap: 12px; }
.q-option { padding: 10px 16px; background: #fafafa; border: 1px solid #e4e7ed; border-radius: 6px; transition: all 0.2s; }
.q-option:hover { border-color: #409EFF; background: #ecf5ff; }
.q-textarea { margin-bottom: 16px; }
.q-footer { margin-top: 16px; display: flex; justify-content: flex-end; }
.question-navigation { display: flex; justify-content: center; align-items: center; gap: 16px; margin-top: 24px; padding-top: 16px; border-top: 1px solid #e4e7ed; }
.nav-progress { font-size: 14px; color: #909399; }
</style>