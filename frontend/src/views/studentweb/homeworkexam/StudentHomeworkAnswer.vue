<template>
  <div class="homework-answer">
    <el-button type="text" icon="el-icon-arrow-left" @click="$router.push('/studenthomeworkexam')" style="margin-bottom:16px">返回作业考试</el-button>
    <el-card shadow="never">
      <div slot="header" class="card-header">
        <div>
          <strong>{{ homework.title }}</strong>
          <el-tag type="warning" size="small" style="margin-left:8px">作业</el-tag>
        </div>
        <span v-if="homework.courseName" style="color:#909399;font-size:13px">{{ homework.courseName }}</span>
      </div>
      <div class="hw-content" v-if="homework.content">
        <p>{{ homework.content }}</p>
      </div>
      <el-divider />
      <div class="answer-section">
        <h4>我的作答</h4>
        <el-input
          type="textarea"
          v-model="answerContent"
          :rows="10"
          placeholder="请输入你的作答内容..."
          style="margin-top:8px"
        />
      </div>
      <div style="text-align:right;margin-top:16px">
        <el-button @click="$router.push('/studenthomeworkexam')">取消</el-button>
        <el-button type="primary" @click="submitAnswer" :loading="submitting">{{ submitting ? '提交中...' : '提交作业' }}</el-button>
      </div>
    </el-card>
  </div>
</template>

<script>
import Cookies from 'js-cookie'
export default {
  name: 'StudentHomeworkAnswer',
  data() {
    return {
      homework: {},
      answerContent: '',
      submitting: false
    }
  },
  created() {
    const q = this.$route.query
    if (q.id) {
      this.homework = {
        id: parseInt(q.id),
        title: q.title || '',
        courseName: q.courseName || '',
        content: q.content || ''
      }
    }
    if (!this.homework.id) {
      this.$message.error('作业数据异常，请重新选择')
    }
  },
  methods: {
    async submitAnswer() {
      if (!this.answerContent.trim()) {
        this.$message.warning('请填写作答内容')
        return
      }
      if (!this.homework.id) {
        this.$message.error('作业数据异常')
        return
      }
      this.submitting = true
      try {
        const res = await this.$post('/study/userdohomework/save', {
          homeworkId: this.homework.id,
          userId: parseInt(Cookies.get('userId')),
          content: this.answerContent
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
.homework-answer { padding: 20px; max-width: 800px; margin: 0 auto; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.card-header strong { font-size: 16px; }
.hw-content { font-size: 14px; line-height: 1.8; color: #606266; white-space: pre-wrap; }
.answer-section h4 { margin: 0 0 4px; font-size: 14px; color: #303133; }
</style>
