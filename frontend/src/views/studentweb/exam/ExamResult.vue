<template>
  <div class="exam-result-container">
    <el-card class="result-card">
      <div slot="header" class="result-header">
        <span>答题结果</span>
        <el-button type="primary" size="small" @click="goBack">返回试卷列表</el-button>
      </div>

      <div v-loading="loading">
        <!-- 试卷信息 -->
        <div class="paper-info" v-if="paper">
          <h2>{{ paper.title }}</h2>
          <p class="paper-desc">{{ paper.description }}</p>
          <div class="paper-meta">
            <span>总分：{{ paper.totalScore }}分</span>
            <span>答题时间：{{ paper.duration }}分钟</span>
          </div>
        </div>

        <!-- 得分统计 -->
        <div class="score-summary" v-if="scoreSummary">
          <el-row :gutter="20">
            <el-col :span="6">
              <div class="stat-item">
                <div class="stat-value">{{ scoreSummary.totalScore }}</div>
                <div class="stat-label">总分</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-item">
                <div class="stat-value">{{ scoreSummary.earnedScore }}</div>
                <div class="stat-label">得分</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-item">
                <div class="stat-value">{{ scoreSummary.correctCount }}</div>
                <div class="stat-label">正确题数</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-item">
                <div class="stat-value">{{ scoreSummary.wrongCount }}</div>
                <div class="stat-label">错误题数</div>
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- 题目列表与结果 -->
        <div class="question-list" v-if="questions.length > 0">
          <div
            v-for="(question, index) in questions"
            :key="question.id"
            :class="['question-item', getResultClass(question)]"
          >
            <div class="question-header">
              <span class="question-number">第{{ index + 1 }}题</span>
              <el-tag :type="getTagType(question)" size="small">
                {{ getQuestionTypeLabel(question.questionType) }}
              </el-tag>
              <el-tag v-if="question.score" size="small" type="warning">
                {{ question.score }}分
              </el-tag>
            </div>

            <div class="question-stem" v-html="question.stem"></div>

            <!-- 选项显示 -->
            <div class="options" v-if="question.options">
              <div
                v-for="(opt, optIndex) in parseOptions(question.options)"
                :key="optIndex"
                :class="['option-item', getOptionClass(opt, question)]"
              >
                <span class="option-key">{{ opt.key }}.</span>
                <span class="option-value">{{ opt.value }}</span>
                <i v-if="isCorrectOption(opt, question)" class="el-icon-circle-check correct-icon"></i>
                <i v-if="isWrongOption(opt, question)" class="el-icon-circle-close wrong-icon"></i>
              </div>
            </div>

            <!-- 填空/文字题显示答案 -->
            <div class="answer-section" v-if="question.questionType >= 4">
              <div class="answer-row">
                <label>我的答案：</label>
                <span :class="getAnswerStatus(question) ? 'correct' : 'wrong'">
                  {{ getStudentAnswer(question) || '未作答' }}
                </span>
              </div>
              <div class="answer-row" v-if="question.answer">
                <label>参考答案：</label>
                <span>{{ question.answer }}</span>
              </div>
            </div>

            <!-- 批改信息 -->
            <div class="review-info" v-if="question.reviewStatus === 1">
              <div class="score-row">
                <label>得分：</label>
                <span class="score-value">{{ question.earnedScore || 0 }}分</span>
              </div>
              <div class="remark-row" v-if="question.remark">
                <label>评语：</label>
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
        </div>
      </div>
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
      scoreSummary: null
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
        // 加载试卷详情（含题目）
        const detailRes = await getPaperDetail(paperId)
        if (detailRes.code === 0) {
          this.paper = detailRes.data.paper || {}
          this.questions = (detailRes.data.questions || []).map(q => ({
            ...q,
            earnedScore: 0,
            reviewStatus: 0,
            studentAnswer: ''
          }))
        }

        // 加载学生的答题记录
        const studentId = parseInt(localStorage.getItem('userId') || '0')
        const answerRes = await getStudentAnswerDetail({ paperId, studentId })
        if (answerRes.code === 0 && answerRes.data) {
          const answerMap = {}
          ;(answerRes.data.records || []).forEach(record => {
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

          // 计算统计
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

        // 客观题判断对错
        if (q.questionType <= 3) {
          if (q.earnedScore > 0) {
            correctCount++
          } else {
            wrongCount++
          }
        } else if (q.reviewStatus === 1) {
          // 主观题已批改
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

    getQuestionTypeLabel(type) {
      const map = { 1: '单选题', 2: '多选题', 3: '判断题', 4: '填空题', 5: '文字题' }
      return map[type] || '未知题型'
    },

    parseOptions(optionsStr) {
      try {
        if (typeof optionsStr === 'string') {
          return JSON.parse(optionsStr)
        }
        return optionsStr || []
      } catch {
        return []
      }
    },

    getStudentAnswer(question) {
      return question.studentAnswer || ''
    },

    isCorrectOption(opt, question) {
      if (!question.studentAnswer) return false
      if (question.questionType === 1) {
        return opt.key === question.answer && question.studentAnswer === question.answer
      }
      if (question.questionType === 2) {
        return question.answer && question.answer.split(',').includes(opt.key) && question.studentAnswer && question.studentAnswer.split(',').includes(opt.key)
      }
      if (question.questionType === 3) {
        return opt.key === question.answer && question.studentAnswer === question.answer
      }
      return false
    },

    isWrongOption(opt, question) {
      if (!question.studentAnswer) return false
      if (question.questionType === 1) {
        return question.studentAnswer === opt.key && question.studentAnswer !== question.answer
      }
      if (question.questionType === 2) {
        return question.studentAnswer && question.studentAnswer.split(',').includes(opt.key) && !(question.answer && question.answer.split(',').includes(opt.key))
      }
      if (question.questionType === 3) {
        return question.studentAnswer === opt.key && question.studentAnswer !== question.answer
      }
      return false
    },

    getOptionClass(opt, question) {
      if (question.questionType > 3) return ''
      const isCorrectOpt = question.answer && question.answer.split(',').includes(opt.key)
      const isSelected = question.studentAnswer && question.studentAnswer.split(',').includes(opt.key)

      if (isCorrectOpt) return 'option-correct'
      if (isSelected && !isCorrectOpt) return 'option-wrong'
      return ''
    },

    getAnswerStatus(question) {
      if (question.questionType <= 3) {
        return question.studentAnswer === question.answer
      }
      return question.reviewStatus === 1 && question.earnedScore > 0
    },

    getResultClass(question) {
      if (question.questionType <= 3) {
        return question.studentAnswer === question.answer ? 'question-correct' : 'question-wrong'
      }
      if (question.reviewStatus === 1) {
        return question.earnedScore > 0 ? 'question-correct' : 'question-wrong'
      }
      return ''
    },

    getTagType(question) {
      const isCorrect = this.getAnswerStatus(question)
      if (question.questionType <= 3) {
        return isCorrect ? 'success' : 'danger'
      }
      if (question.reviewStatus === 1) {
        return isCorrect ? 'success' : 'danger'
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
.exam-result-container {
  padding: 20px;
  max-width: 900px;
  margin: 0 auto;
}

.result-card {
  min-height: 500px;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.paper-info {
  text-align: center;
  padding: 20px 0;
  border-bottom: 1px solid #eee;
}

.paper-info h2 {
  margin: 0 0 10px;
  font-size: 22px;
  color: #303133;
}

.paper-desc {
  color: #909399;
  font-size: 14px;
  margin-bottom: 10px;
}

.paper-meta {
  display: flex;
  justify-content: center;
  gap: 20px;
  color: #606266;
  font-size: 14px;
}

.score-summary {
  padding: 20px 0;
  border-bottom: 1px solid #eee;
  margin-bottom: 20px;
}

.stat-item {
  text-align: center;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #409eff;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}

.question-list {
  padding: 10px 0;
}

.question-item {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 15px;
  transition: box-shadow 0.3s;
}

.question-item:hover {
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
}

.question-item.question-correct {
  border-left: 4px solid #67c23a;
}

.question-item.question-wrong {
  border-left: 4px solid #f56c6c;
}

.question-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 15px;
}

.question-number {
  font-weight: bold;
  font-size: 16px;
  color: #303133;
}

.question-stem {
  font-size: 15px;
  line-height: 1.6;
  color: #303133;
  margin-bottom: 15px;
}

.options {
  margin-bottom: 15px;
}

.option-item {
  display: flex;
  align-items: center;
  padding: 10px 15px;
  margin-bottom: 8px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  transition: background-color 0.2s;
  position: relative;
}

.option-item.option-correct {
  background-color: #f0f9eb;
  border-color: #67c23a;
}

.option-item.option-wrong {
  background-color: #fef0f0;
  border-color: #f56c6c;
}

.option-key {
  font-weight: bold;
  margin-right: 10px;
  min-width: 20px;
}

.correct-icon {
  color: #67c23a;
  font-size: 18px;
  margin-left: auto;
}

.wrong-icon {
  color: #f56c6c;
  font-size: 18px;
  margin-left: auto;
}

.answer-section {
  background: #f5f7fa;
  padding: 15px;
  border-radius: 6px;
  margin-bottom: 15px;
}

.answer-row {
  margin-bottom: 5px;
}

.answer-row label {
  font-weight: bold;
  color: #606266;
  margin-right: 10px;
}

.correct {
  color: #67c23a;
  font-weight: bold;
}

.wrong {
  color: #f56c6c;
  font-weight: bold;
}

.review-info {
  background: #fdf6ec;
