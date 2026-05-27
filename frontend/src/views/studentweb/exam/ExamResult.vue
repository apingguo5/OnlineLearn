<template>
  <div class="exam-result-container">
    <el-button type="text" icon="el-icon-arrow-left" @click="goBack" style="margin-bottom:12px">返回试卷列表</el-button>
    <el-card class="result-card" v-loading="loading">
      <!-- 试卷信息 -->
      <div class="paper-info" v-if="paper">
        <h2>{{ paper.title }}</h2>
        <p class="paper-desc" v-if="paper.description">{{ paper.description }}</p>
        <div class="paper-meta">
          <span>总分：{{ paper.totalScore }}分</span>
          <span v-if="paper.duration"> | 答题时间：{{ paper.duration }}分钟</span>
          <span> | 共 {{ questions.length }} 题</span>
        </div>
      </div>

      <!-- 得分统计 -->
      <div class="score-summary" v-if="scoreSummary && questions.length > 0">
        <el-row :gutter="16">
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-value" style="color:#303133">{{ scoreSummary.totalScore }}</div>
              <div class="stat-label">总分</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-value" style="color:#409EFF">{{ scoreSummary.earnedScore }}</div>
              <div class="stat-label">得分</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-value" style="color:#67C23A">{{ scoreSummary.correctCount }}</div>
              <div class="stat-label">正确题数</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-value" style="color:#F56C6C">{{ scoreSummary.wrongCount }}</div>
              <div class="stat-label">错误题数</div>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 题目逐个显示区域 -->
      <div class="exam-body" v-if="questions.length > 0">
        <!-- 侧边栏题号导航 -->
        <div class="exam-sidebar">
          <div class="sidebar-info">
            <p>共 {{ questions.length }} 题</p>
          </div>
          <el-divider />
          <div class="question-nav">
            <div
              v-for="(q, idx) in questions" :key="idx"
              class="q-nav-item"
              :class="{
                'nav-correct': getNavStatus(q) === 'correct',
                'nav-wrong': getNavStatus(q) === 'wrong',
                'nav-pending': getNavStatus(q) === 'pending',
                'nav-current': currentQuestionIdx === idx
              }"
              @click="goToQuestion(idx)"
            >
              {{ idx + 1 }}
            </div>
          </div>
          <el-divider />
          <div class="legend">
            <span><span class="legend-dot dot-correct"></span> 正确</span>
            <span><span class="legend-dot dot-wrong"></span> 错误</span>
            <span><span class="legend-dot dot-pending"></span> 待批</span>
          </div>
        </div>

        <!-- 答题内容区域 -->
        <div class="exam-content">
          <div
            v-for="(question, idx) in questions"
            :key="question.id || idx"
            class="question-wrapper"
            v-show="currentQuestionIdx === idx"
          >
            <!-- 题目标题栏 -->
            <div class="q-type-bar">
              <el-tag :type="getTagType(question)" size="small">
                {{ getQuestionTypeLabel(question.questionType) }}
              </el-tag>
              <span class="q-score">({{ question.score }}分)</span>
              <span class="q-earned-score" v-if="question.earnedScore !== undefined">得 {{ question.earnedScore }} 分</span>
            </div>
            <div class="q-stem"><strong>第{{ idx + 1 }}题：</strong>{{ question.stem }}</div>

            <!-- 选项显示 -->
            <div class="q-options" v-if="showOptions(question)">
              <div
                v-for="(opt, optIndex) in parseOptions(question.options)"
                :key="optIndex"
                :class="['option-item', getOptionClass(opt, question)]"
              >
                <span class="option-letter">{{ opt.key }}.</span>
                <span class="option-text">{{ opt.value }}</span>
                <i v-if="isCorrectOption(opt, question)" class="el-icon-circle-check correct-icon"></i>
                <i v-if="isWrongOption(opt, question)" class="el-icon-circle-close wrong-icon"></i>
              </div>
            </div>

            <!-- 判断题 -->
            <div v-if="question.questionType === 3" class="q-options">
              <div
                :class="['option-item', getJudgeClass(question, 'true')]"
              >
                <span class="option-letter">A.</span>
                <span class="option-text">正确</span>
                <i v-if="question.studentAnswer === 'true' && question.answer === 'true'" class="el-icon-circle-check correct-icon"></i>
                <i v-if="question.studentAnswer === 'true' && question.answer !== 'true'" class="el-icon-circle-close wrong-icon"></i>
              </div>
              <div
                :class="['option-item', getJudgeClass(question, 'false')]"
              >
                <span class="option-letter">B.</span>
                <span class="option-text">错误</span>
                <i v-if="question.studentAnswer === 'false' && question.answer === 'false'" class="el-icon-circle-check correct-icon"></i>
                <i v-if="question.studentAnswer === 'false' && question.answer !== 'false'" class="el-icon-circle-close wrong-icon"></i>
              </div>
            </div>

            <!-- 填空/文字题 -->
            <div class="answer-section" v-if="question.questionType >= 4">
              <div class="answer-row">
                <span class="answer-label">我的答案：</span>
                <span :class="getAnswerStatus(question) ? 'answer-correct' : 'answer-wrong'">
                  {{ getStudentAnswer(question) || '（未作答）' }}
                </span>
              </div>
              <div class="answer-row" v-if="question.answer">
                <span class="answer-label">参考答案：</span>
                <span class="answer-reference">{{ question.answer }}</span>
              </div>
            </div>

            <!-- 客观题：显示学生答案和正确答案 -->
            <div v-if="question.questionType <= 3 && getStudentAnswer(question)" class="answer-section">
              <div class="answer-row">
                <span class="answer-label">我的答案：</span>
                <span :class="getAnswerStatus(question) ? 'answer-correct' : 'answer-wrong'">
                  {{ formatStudentAnswer(question) }}
                </span>
              </div>
              <div class="answer-row" v-if="question.answer">
                <span class="answer-label">正确答案：</span>
                <span class="answer-reference">{{ formatCorrectAnswer(question) }}</span>
              </div>
            </div>

            <!-- 批改信息 -->
            <div class="review-info" v-if="question.reviewStatus === 1">
              <div class="score-row">
                <span class="label">得分：</span>
                <span class="score-value">{{ question.earnedScore || 0 }}分</span>
              </div>
              <div class="remark-row" v-if="question.remark">
                <span class="label">评语：</span>
                <span>{{ question.remark }}</span>
              </div>
            </div>
            <div class="review-info" v-else-if="question.questionType >= 4">
              <el-tag type="info" size="small">等待批改</el-tag>
            </div>

            <!-- 解析 -->
            <div class="analysis" v-if="question.analysis">
              <el-collapse>
                <el-collapse-item title="查看解析" name="analysis">
                  <div v-html="question.analysis"></div>
                </el-collapse-item>
              </el-collapse>
            </div>
          </div>

          <el-empty v-if="questions.length === 0" description="暂无题目数据" />

          <!-- 导航按钮 -->
          <div class="question-navigation" v-if="questions.length > 0">
            <el-button @click="prevQuestion" :disabled="currentQuestionIdx <= 0">上一题</el-button>
            <span class="nav-progress">第 {{ currentQuestionIdx + 1 }} / {{ questions.length }} 题</span>
            <el-button @click="nextQuestion" :disabled="currentQuestionIdx >= questions.length - 1">下一题</el-button>
          </div>
        </div>
      </div>

      <el-empty v-else-if="!loading" description="暂无答题数据" />
    </el-card>
  </div>
</template>

<script>
import { getPaperDetail } from '@/api/teacher/examApi'
import { getStudentAnswerDetail } from '@/api/teacher/examApi'

export default {
  name: 'ExamResult',
  data() {
    return {
      paper: null,
      questions: [],
      loading: false,
      scoreSummary: null,
      currentQuestionIdx: 0
    }
  },
  created() {
    this.loadResult()
  },
  methods: {
    async loadResult() {
      const paperId = parseInt(this.$route.query.paperId)
      if (!paperId) {
        this.$message.error('缺少试卷参数')
        return
      }

      this.loading = true
      try {
        const detailRes = await getPaperDetail(paperId)
        const detailData = (detailRes.data && detailRes.data.resultData) || {}
        if (detailRes.data && detailRes.data.code === 200) {
          this.paper = detailData.paper || { title: detailData.title, description: detailData.description, totalScore: detailData.totalScore, duration: detailData.duration }
          this.questions = (detailData.questions || []).map(q => ({
            ...q,
            questionType: this.normalizeType(q.questionType),
            earnedScore: 0,
            reviewStatus: 0,
            studentAnswer: ''
          }))
        }

        const studentId = parseInt(this.$cookies.get('userId') || localStorage.getItem('userId') || '0')
        const answerRes = await getStudentAnswerDetail({ paperId, studentId })
        const answerData = (answerRes.data && answerRes.data.resultData) || {}
        if (answerRes.data && answerRes.data.code === 200 && answerData) {
          const answerMap = {}
          ;(answerData.records || []).forEach(record => {
            answerMap[record.questionId] = record
          })

          this.questions = this.questions.map(q => {
            const record = answerMap[q.id]
            if (record) {
              return {
                ...q,
                studentAnswer: record.answer,
                earnedScore: record.score,
                reviewStatus: record.reviewStatus,
                remark: record.remark
              }
            }
            return q
          })

          this.calculateSummary()
        }
      } catch (err) {
        console.error('加载答题结果失败:', err)
        this.$message.error('加载答题结果失败')
      } finally {
        this.loading = false
      }
    },

    calculateSummary() {
      const totalQuestions = this.questions.length
      let earnedScore = 0
      let totalScore = 0
      let correctCount = 0
      let wrongCount = 0

      this.questions.forEach(q => {
        totalScore += q.score || 0
        earnedScore += q.earnedScore || 0

        if (q.questionType <= 3) {
          if (q.earnedScore > 0) {
            correctCount++
          } else {
            wrongCount++
          }
        } else if (q.reviewStatus === 1) {
          if (q.earnedScore > 0) {
            correctCount++
          } else {
            wrongCount++
          }
        }
      })

      this.scoreSummary = {
        totalScore,
        earnedScore,
        correctCount,
        wrongCount,
        unanswered: totalQuestions - correctCount - wrongCount
      }
    },

    goToQuestion(idx) { this.currentQuestionIdx = idx },
    prevQuestion() { if (this.currentQuestionIdx > 0) this.currentQuestionIdx-- },
    nextQuestion() { if (this.currentQuestionIdx < this.questions.length - 1) this.currentQuestionIdx++ },

    showOptions(question) {
      return question.questionType === 1 || question.questionType === 2
    },

    getNavStatus(q) {
      if (q.questionType <= 3) {
        return q.earnedScore > 0 ? 'correct' : (q.studentAnswer ? 'wrong' : 'pending')
      }
      if (q.reviewStatus === 1) {
        return q.earnedScore > 0 ? 'correct' : 'wrong'
      }
      return 'pending'
    },

    getQuestionTypeLabel(type) {
      const map = { 1: '单选题', 2: '多选题', 3: '判断题', 4: '填空题', 5: '文字题', single: '单选题', multiple: '多选题', judge: '判断题', fill: '填空题', essay: '文字题' }
      return map[type] || '未知题型'
    },

    normalizeType(type) {
      if (typeof type === 'number') return type
      const map = { single: 1, multiple: 2, judge: 3, fill: 4, essay: 5 }
      return map[type] || 0
    },

    parseOptions(optionsStr) {
      try {
        let raw = optionsStr
        if (typeof optionsStr === 'string') {
          raw = JSON.parse(optionsStr)
        }
        if (!Array.isArray(raw)) return []
        return raw.map((opt, i) => ({
          key: opt.key || opt.label || String.fromCharCode(65 + i),
          value: opt.value || opt.text || ''
        }))
      } catch {
        return []
      }
    },

    getStudentAnswer(question) {
      if (question.studentAnswer === null || question.studentAnswer === undefined) return ''
      if (Array.isArray(question.studentAnswer)) return question.studentAnswer.join(', ')
      return String(question.studentAnswer)
    },

    formatStudentAnswer(q) {
      const ans = q.studentAnswer
      if (!ans) return '（未作答）'
      if (q.questionType === 3) return ans === 'true' ? '正确' : '错误'
      if (typeof ans === 'string') return ans
      if (Array.isArray(ans)) return ans.join(', ')
      return String(ans)
    },

    formatCorrectAnswer(q) {
      if (!q.answer) return '-'
      if (q.questionType === 3) return q.answer === 'true' ? '正确' : '错误'
      return String(q.answer)
    },

    isCorrectOption(opt, question) {
      if (!question.studentAnswer) return false
      const studentSet = this.getStudentAnswerSet(question)
      const correctSet = this.getCorrectAnswerSet(question)
      return correctSet.has(opt.key) && studentSet.has(opt.key)
    },

    isWrongOption(opt, question) {
      if (!question.studentAnswer) return false
      const studentSet = this.getStudentAnswerSet(question)
      const correctSet = this.getCorrectAnswerSet(question)
      return studentSet.has(opt.key) && !correctSet.has(opt.key)
    },

    getStudentAnswerSet(q) {
      const ans = q.studentAnswer
      if (!ans) return new Set()
      if (Array.isArray(ans)) return new Set(ans.map(s => s.trim()))
      return new Set(String(ans).split(',').map(s => s.trim()).filter(Boolean))
    },

    getCorrectAnswerSet(q) {
      if (!q.answer) return new Set()
      return new Set(String(q.answer).split(',').map(s => s.trim()).filter(Boolean))
    },

    getOptionClass(opt, question) {
      if (question.questionType > 2) return ''
      const correctSet = this.getCorrectAnswerSet(question)
      const studentSet = this.getStudentAnswerSet(question)
      if (correctSet.has(opt.key) && studentSet.has(opt.key)) return 'option-correct'
      if (correctSet.has(opt.key) && !studentSet.has(opt.key)) return 'option-missed'
      if (studentSet.has(opt.key) && !correctSet.has(opt.key)) return 'option-wrong'
      return ''
    },

    getJudgeClass(question, value) {
      if (question.studentAnswer === value && question.answer === value) return 'option-correct'
      if (question.studentAnswer === value && question.answer !== value) return 'option-wrong'
      if (question.studentAnswer !== value && question.answer === value) return 'option-missed'
      return ''
    },

    getAnswerStatus(question) {
      if (question.questionType <= 2) {
        const studentSet = this.getStudentAnswerSet(question)
        const correctSet = this.getCorrectAnswerSet(question)
        if (studentSet.size === 0 || correctSet.size === 0) return false
        return studentSet.size === correctSet.size && [...studentSet].every(s => correctSet.has(s))
      }
      if (question.questionType === 3) {
        return question.studentAnswer === question.answer
      }
      return question.reviewStatus === 1 && question.earnedScore > 0
    },

    getTagType(question) {
      const isCorrect = this.getAnswerStatus(question)
      if (question.questionType <= 3) {
        return isCorrect ? 'success' : 'danger'
      }
      if (question.reviewStatus === 1) {
        return question.earnedScore > 0 ? 'success' : 'danger'
      }
      return 'info'
    },

    goBack() {
      this.$router.push({ name: 'StudentExam' })
    }
  }
}
</script>

<style scoped>
.exam-result-container { padding: 16px; background: #f5f7fa; min-height: 100vh; }
.result-card { border-radius: 8px; }

.paper-info { text-align: center; padding: 16px 0; border-bottom: 1px solid #eee; }
.paper-info h2 { margin: 0 0 6px; font-size: 20px; color: #303133; }
.paper-desc { color: #909399; font-size: 13px; margin-bottom: 6px; }
.paper-meta { display: flex; justify-content: center; gap: 16px; color: #606266; font-size: 13px; }

.score-summary { padding: 16px 0; border-bottom: 1px solid #eee; margin-bottom: 12px; }
.stat-item { text-align: center; padding: 12px; background: #f5f7fa; border-radius: 8px; }
.stat-value { font-size: 24px; font-weight: bold; }
.stat-label { font-size: 12px; color: #909399; margin-top: 4px; }

/* 答题结果布局 */
.exam-body { display: flex; gap: 16px; margin-top: 16px; align-items: flex-start; }
.exam-sidebar {
  width: 130px; flex-shrink: 0; background: #fff; border-radius: 8px;
  padding: 14px 10px; box-shadow: 0 2px 6px rgba(0,0,0,.04);
  position: sticky; top: 16px;
}
.sidebar-info p { margin: 0; text-align: center; font-size: 14px; color: #606266; }
.question-nav { display: flex; flex-wrap: wrap; gap: 6px; }
.q-nav-item {
  width: 30px; height: 30px; display: flex; align-items: center; justify-content: center;
  border-radius: 50%; font-size: 12px; font-weight: 700; cursor: pointer;
  background: #e8eaed; color: #909399; transition: all .15s;
}
.q-nav-item:hover { background: #c6d9f0; }
.q-nav-item.nav-correct { background: #67C23A; color: #fff; }
.q-nav-item.nav-wrong { background: #F56C6C; color: #fff; }
.q-nav-item.nav-pending { background: #e8eaed; color: #909399; }
.q-nav-item.nav-current { border: 2px solid #303133; }
.legend { display: flex; flex-direction: column; gap: 4px; font-size: 12px; color: #909399; }
.legend-dot { display: inline-block; width: 10px; height: 10px; border-radius: 50%; margin-right: 4px; vertical-align: middle; }
.dot-correct { background: #67C23A; }
.dot-wrong { background: #F56C6C; }
.dot-pending { background: #e8eaed; border: 1px solid #dcdfe6; }

.exam-content { flex: 1; min-width: 0; background: #fff; border-radius: 8px; padding: 24px; box-shadow: 0 2px 6px rgba(0,0,0,.04); }
.question-wrapper { min-height: 280px; }
.q-type-bar { display: flex; align-items: center; gap: 8px; margin-bottom: 10px; }
.q-score { color: #909399; font-size: 13px; }
.q-earned-score { color: #409EFF; font-size: 13px; font-weight: 600; margin-left: 8px; }
.q-stem { font-size: 16px; line-height: 1.7; color: #303133; margin-bottom: 20px; }
.q-options { display: flex; flex-direction: column; gap: 8px; margin-bottom: 16px; }
.option-item {
  display: flex; align-items: center; gap: 8px; padding: 10px 14px;
  background: #fafafa; border: 1px solid #e4e7ed; border-radius: 6px; transition: all .15s;
}
.option-letter { font-weight: 600; color: #606266; min-width: 20px; }
.option-text { flex: 1; }
.option-item.option-correct { background: #f0f9eb; border-color: #67C23A; }
.option-item.option-wrong { background: #fef0f0; border-color: #F56C6C; }
.option-item.option-missed { background: #fdf6ec; border-color: #E6A23C; }
.correct-icon { color: #67C23A; font-size: 18px; }
.wrong-icon { color: #F56C6C; font-size: 18px; }

.answer-section { margin: 12px 0; padding: 10px 0; border-top: 1px dashed #e4e7ed; }
.answer-row { margin-bottom: 6px; }
.answer-label { color: #909399; font-size: 13px; }
.answer-correct { color: #67C23A; font-weight: 600; }
.answer-wrong { color: #F56C6C; font-weight: 600; }
.answer-reference { color: #409EFF; font-weight: 500; }

.review-info { margin-top: 12px; padding: 10px 14px; background: #f5f7fa; border-radius: 6px; }
.score-row { margin-bottom: 4px; }
.score-row .label { color: #909399; font-size: 13px; }
.score-value { color: #303133; font-weight: 700; font-size: 15px; }
.remark-row { color: #606266; font-size: 13px; }
.remark-row .label { color: #909399; }

.analysis { margin-top: 12px; }

.question-navigation { display: flex; justify-content: center; align-items: center; gap: 16px; margin-top: 24px; padding-top: 16px; border-top: 1px solid #ebeef5; }
.nav-progress { font-size: 14px; color: #909399; }
</style>
