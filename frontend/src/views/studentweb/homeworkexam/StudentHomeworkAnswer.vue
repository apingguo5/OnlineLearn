<template>
  <div class="homework-answer">
    <el-button type="text" icon="el-icon-arrow-left" @click="$router.push('/studenthomeworkexam')" style="margin-bottom:16px">返回作业考试</el-button>
    
    <el-card shadow="never" v-loading="loading">
      <div slot="header" class="card-header">
        <div>
          <strong>{{ homework.title }}</strong>
          <el-tag :type="homework._type === 'exam' ? 'danger' : 'warning'" size="small" style="margin-left:8px">
            {{ homework._type === 'exam' ? '考试' : '作业' }}
          </el-tag>
        </div>
        <span v-if="homework.courseName" style="color:#909399;font-size:13px">{{ homework.courseName }}</span>
      </div>

      <!-- 倒计时（考试模式） -->
      <div v-if="homework._type === 'exam' && remainingTime > 0" class="exam-timer" :class="{'timer-warning': timeWarning}">
        <i class="el-icon-time"></i> 剩余时间：{{ formattedTime }}
      </div>

      <!-- 简单文本作业 -->
      <div v-if="homework._type === 'homework' && !hasQuestions" class="hw-content">
        <p v-if="displayContent">{{ displayContent }}</p>
        <p v-else style="color:#909399">请在下方输入你的答案</p>
        <el-divider />
        <div class="answer-section">
          <h4>我的作答</h4>
          <el-input
            type="textarea"
            v-model="simpleAnswer"
            :rows="10"
            placeholder="请输入你的作答内容..."
            style="margin-top:8px"
          />
        </div>
      </div>

      <!-- 带题目列表的作业/考试 -->
      <div v-else-if="questions.length > 0">
        <div class="exam-body">
          <!-- 侧边栏题目标题 -->
          <div class="exam-sidebar">
            <div class="sidebar-info">
              <p v-if="homework._type === 'exam'">总分：{{ totalScore }}分</p>
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

          <!-- 答题区域 -->
          <div class="exam-content">
            <div class="question-wrapper" v-for="(q, idx) in questions" :key="idx" v-show="currentQuestionIdx === idx">
              <div class="q-type-bar">
                <el-tag :type="typeTag(q.questionType)" size="small">{{ typeLabel(q.questionType) }}</el-tag>
                <span class="q-score">({{ q.score || 5 }}分)</span>
              </div>
              <div class="q-stem"><strong>第{{ idx + 1 }}题：</strong>{{ q.stem }}</div>

              <!-- 单选题 -->
              <el-radio-group v-model="q.studentAnswer" v-if="q.questionType === 'single'" class="q-options">
                <el-radio v-for="(opt, oi) in parsedOptions(q)" :key="oi" :label="String.fromCharCode(65 + oi)" class="q-option">
                  {{ String.fromCharCode(65 + oi) }}. {{ opt }}
                </el-radio>
              </el-radio-group>

              <!-- 多选题 -->
              <div v-if="q.questionType === 'multiple'" class="q-options">
                <el-checkbox v-for="(opt, oi) in parsedOptions(q)" :key="oi" v-model="multipleAnswers[q.id + '_' + oi]" @change="handleMultipleChange(q)" class="q-option">
                  {{ String.fromCharCode(65 + oi) }}. {{ opt }}
                </el-checkbox>
              </div>

              <!-- 判断题 -->
              <el-radio-group v-model="q.studentAnswer" v-if="q.questionType === 'judge'" class="q-options">
                <el-radio label="true" class="q-option">正确</el-radio>
                <el-radio label="false" class="q-option">错误</el-radio>
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

      <!-- 无题目提示 -->
      <div v-else class="empty-tip">
        <el-empty description="暂无题目，请确认作业是否正确关联试卷" />
      </div>

      <!-- 提交按钮 -->
      <div style="text-align:right;margin-top:16px">
        <el-button @click="$router.push('/studenthomeworkexam')">取消</el-button>
        <el-button type="primary" @click="submitAnswer" :loading="submitting">{{ submitting ? '提交中...' : '提交作业' }}</el-button>
      </div>
    </el-card>
  </div>
</template>

<script>
import Cookies from 'js-cookie'
import { getPaperDetail } from '@/api/teacher/examApi'
import { get } from '@/api/request'

export default {
  name: 'StudentHomeworkAnswer',
  data() {
    return {
      homework: {
        id: null,
        title: '',
        courseName: '',
        content: '',
        _type: 'homework'
      },
      questions: [],
      currentQuestionIdx: 0,
      simpleAnswer: '',
      loading: false,
      submitting: false,
      remainingTime: 0,
      timer: null,
      totalScore: 0,
      displayContent: '',
      multipleAnswers: {}
    }
  },
  computed: {
    hasQuestions() {
      return this.questions.length > 0
    },
    formattedTime() {
      const h = Math.floor(this.remainingTime / 3600)
      const m = Math.floor((this.remainingTime % 3600) / 60)
      const s = this.remainingTime % 60
      return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
    },
    timeWarning() {
      return this.remainingTime <= 300
    }
  },
  created() {
    const q = this.$route.query
    if (q.id) {
      this.homework.id = parseInt(q.id)
      this.homework.title = q.title || ''
      this.homework.courseName = q.courseName || ''
      this.homework._type = q._type || 'homework'
    }
    if (!this.homework.id) {
      this.$message.error('作业数据异常，请重新选择')
      return
    }
    this.loadHomeworkDetail()
  },
  beforeDestroy() {
    this.clearTimer()
  },
  methods: {
    typeLabel(t) { return { single: '单选', multiple: '多选', judge: '判断', fill: '填空', essay: '主观' }[t] || t },
    typeTag(t) { return { single: 'primary', multiple: 'success', judge: 'warning', fill: 'info', essay: '' }[t] || '' },
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
    isAnswered(q) {
      if (q.questionType === 'multiple') {
        const options = this.parsedOptions(q)
        return options.some((opt, oi) => this.multipleAnswers[q.id + '_' + oi])
      }
      if (!q.studentAnswer) return false
      if (Array.isArray(q.studentAnswer) && q.studentAnswer.length === 0) return false
      return true
    },
    handleMultipleChange(q) {
      const options = this.parsedOptions(q)
      const selected = []
      options.forEach((opt, oi) => {
        if (this.multipleAnswers[q.id + '_' + oi]) {
          selected.push(String.fromCharCode(65 + oi))
        }
      })
      q.studentAnswer = selected
    },
    async loadHomeworkDetail() {
      this.loading = true
      try {
        // 首先从后端获取作业详情
        console.log('开始加载作业详情，作业ID:', this.homework.id)
        const res = await get(`/study/homework/info/${this.homework.id}`)
        console.log('作业详情响应:', res.data)
        
        if (res.data && res.data.code === 200 && res.data.resultData) {
          const hwData = res.data.resultData
          this.homework.content = hwData.content || ''
          this.homework.title = hwData.title || this.homework.title
          console.log('作业content字段:', this.homework.content)
        }
        
        const content = this.homework.content || ''
        let contentData = null
        try {
          contentData = JSON.parse(content)
          console.log('解析后的contentData:', contentData)
        } catch (e) {
          console.log('content不是JSON格式:', e)
        }

        // 如果关联了试卷（无论是考试还是作业类型），加载试卷题目
        let paperId = null
        if (contentData && contentData.paperRef) {
          paperId = contentData.paperRef
          console.log('从JSON中获取到试卷ID:', paperId)
        } else if (content && content.indexOf('paperRef:') === 0) {
          paperId = content.replace('paperRef:', '')
          console.log('从字符串中获取到试卷ID:', paperId)
        }
        
        if (paperId) {
          console.log('开始加载试卷详情，试卷ID:', paperId)
          // 如果是考试类型，设置考试模式
          if ((contentData && contentData.type === 'exam') || (content && content.indexOf('paperRef:') === 0)) {
            this.homework._type = 'exam'
          }
          const paperRes = await getPaperDetail(paperId)
          console.log('试卷详情响应:', paperRes.data)
          const paper = (paperRes.data && paperRes.data.resultData) ? paperRes.data.resultData : {}
          this.totalScore = paper.totalScore || 0
          console.log('试卷题目列表:', paper.questions)
          // 只有考试类型才设置倒计时
          if (this.homework._type === 'exam') {
            this.remainingTime = (paper.duration || 60) * 60
            this.startTimer()
          }
          this.questions = (paper.questions || []).map(q => {
            const question = {
              ...q,
              flagged: false,
              questionType: this.numToType(q.questionType),
              studentAnswer: ''
            }
            // 为多选题初始化 each 选项的状态
            if (question.questionType === 'multiple') {
              const options = this.parsedOptions(question)
              options.forEach((opt, oi) => {
                this.$set(this.multipleAnswers, question.id + '_' + oi, false)
              })
              question.studentAnswer = []
            }
            return question
          })
          console.log('最终题目列表:', this.questions)
        } else {
          console.log('未找到试卷ID，作为简单文本作业处理')
          // 没有关联试卷，显示简单文本内容
          if (contentData && contentData.type) {
            // JSON 格式但没有 paperRef，可能是文本作业
            this.displayContent = contentData.text || ''
          } else {
            this.displayContent = content || ''
          }
        }
      } catch (e) {
        console.error('加载作业详情失败:', e)
        this.$message.error('加载作业详情失败，请重试')
      }
      this.loading = false
    },
    numToType(n) {
      const map = { 1: 'single', 2: 'multiple', 3: 'judge', 4: 'fill', 5: 'essay' }
      return map[n] || n
    },
    startTimer() {
      this.clearTimer()
      this.timer = setInterval(() => {
        if (this.remainingTime <= 0) {
          this.submitAnswer(true)
          return
        }
        this.remainingTime--
      }, 1000)
    },
    clearTimer() {
      if (this.timer) {
        clearInterval(this.timer)
        this.timer = null
      }
    },
    goToQuestion(idx) {
      this.currentQuestionIdx = idx
    },
    prevQuestion() {
      if (this.currentQuestionIdx > 0) this.currentQuestionIdx--
    },
    nextQuestion() {
      if (this.currentQuestionIdx < this.questions.length - 1) this.currentQuestionIdx++
    },
    async submitAnswer(auto = false) {
      // 验证答题内容
      if (this.homework._type === 'homework' && this.questions.length === 0) {
        if (!this.simpleAnswer.trim()) {
          this.$message.warning('请填写作答内容')
          return
        }
      } else {
        const hasUnanswered = this.questions.some(q => !this.isAnswered(q))
        if (!auto && hasUnanswered) {
          try {
            await this.$confirm('还有未完成的题目，确定要提交吗？', '提示', { type: 'warning' })
          } catch (e) {
            return
          }
        }
      }

      if (!this.homework.id) {
        this.$message.error('作业数据异常')
        return
      }

      this.submitting = true
      this.clearTimer()

      try {
        let answerContent = ''
        
        if (this.homework._type === 'homework' && this.questions.length === 0) {
          // 简单文本作业
          answerContent = this.simpleAnswer
        } else {
          // 带题目的作业/考试
          answerContent = JSON.stringify(this.questions.map(q => ({
            questionId: q.id,
            answer: Array.isArray(q.studentAnswer) ? q.studentAnswer.join(',') : (q.studentAnswer || '')
          })))
        }

        const res = await this.$post('/study/userdohomework/save', {
          homeworkId: this.homework.id,
          userId: parseInt(Cookies.get('userId')),
          content: answerContent
        })

        if (res.data && res.data.code === 200) {
          this.$message.success('提交成功')
          this.$router.push('/studenthomeworkexam')
        } else {
          this.$message.error('提交失败，请重试')
        }
      } catch (e) {
        this.$message.error('提交失败，请检查网络')
      }

      this.submitting = false
    }
  }
}
</script>

<style scoped>
.homework-answer { 
  padding: 20px; 
  max-width: 1200px; 
  margin: 0 auto;
}
.card-header { 
  display: flex; 
  justify-content: space-between; 
  align-items: center; 
}
.card-header strong { font-size: 16px; }
.hw-content { 
  font-size: 14px; 
  line-height: 1.8; 
  color: #606266; 
  white-space: pre-wrap; 
}
.answer-section h4 { 
  margin: 0 0 4px; 
  font-size: 14px; 
  color: #303133; 
}
.empty-tip {
  padding: 40px 0;
}

/* 倒计时样式 */
.exam-timer {
  font-size: 18px;
  font-weight: 700;
  color: #409EFF;
  padding: 8px 16px;
  background: #ecf5ff;
  border-radius: 6px;
  margin-bottom: 16px;
  text-align: center;
}
.exam-timer.timer-warning {
  color: #F56C6C;
  background: #fef0f0;
  animation: pulse 1s infinite;
}
@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

/* 答题区域样式 */
.exam-body {
  display: flex;
  gap: 16px;
  margin-top: 16px;
}
.exam-sidebar {
  width: 120px;
  background: #fafafa;
  border-radius: 8px;
  padding: 16px;
  flex-shrink: 0;
}
.sidebar-info p {
  margin: 0 0 8px;
  font-size: 14px;
  color: #606266;
}
.question-nav {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.q-nav-item {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  font-size: 13px;
  cursor: pointer;
  background: #e4e7ed;
  color: #606266;
  transition: all 0.2s;
}
.q-nav-item:hover {
  background: #d9ecff;
}
.q-nav-item.nav-answered {
  background: #409EFF;
  color: white;
}
.q-nav-item.nav-current {
  border: 2px solid #303133;
  font-weight: 700;
}
.q-nav-item.nav-flagged {
  border: 2px solid #E6A23C;
}
.legend {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 12px;
  color: #909399;
}
.legend-dot {
  display: inline-block;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  margin-right: 4px;
}
.dot-answered {
  background: #409EFF;
}
.dot-unanswered {
  background: #e4e7ed;
}

.exam-content {
  flex: 1;
  background: white;
  border-radius: 8px;
  padding: 24px;
  border: 1px solid #e4e7ed;
}
.question-wrapper {
  min-height: 300px;
}
.q-type-bar {
  margin-bottom: 12px;
}
.q-score {
  font-size: 14px;
  color: #909399;
  margin-left: 8px;
}
.q-stem {
  font-size: 16px;
  line-height: 1.6;
  margin-bottom: 20px;
  color: #303133;
}
.q-options {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.q-option {
  padding: 12px 16px;
  background: #fafafa;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  transition: all 0.2s;
}
.q-option:hover {
  border-color: #409EFF;
  background: #ecf5ff;
}
.q-textarea {
  margin-bottom: 16px;
}
.q-footer {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
.question-navigation {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #e4e7ed;
}
.nav-progress {
  font-size: 14px;
  color: #909399;
}
</style>
