<template>
  <div class="hw-answer-page">
    <el-button type="text" icon="el-icon-arrow-left" @click="$router.push('/studenthomeworkexam')" class="back-btn">返回作业考试</el-button>
    
    <el-card shadow="never" class="hw-card" v-loading="loading">
      <div slot="header" class="hw-header">
        <div class="hw-title-row">
          <strong class="hw-title">{{ homework.title }}</strong>
          <el-tag :type="homework._type === 'exam' ? 'danger' : 'warning'" size="small">{{ homework._type === 'exam' ? '考试' : '作业' }}</el-tag>
        </div>
        <span v-if="homework.courseName" class="hw-course">{{ homework.courseName }}</span>
      </div>

      <!-- 倒计时 -->
      <div v-if="homework._type === 'exam' && remainingTime > 0" class="countdown-bar" :class="{'countdown-warn': timeWarning}">
        <i class="el-icon-time"></i> 剩余时间：{{ formattedTime }}
      </div>

      <!-- ========== 简单文本作业（无题目列表） ========== -->
      <div v-if="!paperLoaded && !loading" class="simple-area">
        <div v-if="displayContent" class="simple-text">{{ displayContent }}</div>
        <p v-else class="simple-hint">请在下方输入你的答案</p>
        <el-divider />
        <h4>我的作答</h4>
        <el-input type="textarea" v-model="simpleAnswer" :rows="10" placeholder="请输入你的作答内容..." />
      </div>

      <!-- ========== 带题目的作业/考试 ========== -->
      <div v-if="paperLoaded">
        <div v-if="questions.length > 0" class="exam-layout">
          <!-- 左侧题号导航 -->
          <div class="exam-nav-panel">
            <p class="nav-info"><b>{{ questions.length }}</b> 题</p>
            <el-divider />
            <div class="nav-dots">
              <div
                v-for="(q, idx) in questions" :key="idx"
                class="nav-dot"
                :class="{
                  'dot-done': isAnswered(q),
                  'dot-curr': currentIdx === idx,
                  'dot-mark': q.flagged
                }"
                @click="goTo(idx)"
              >{{ idx + 1 }}</div>
            </div>
            <el-divider />
            <div class="nav-legend">
              <span><span class="leg-dot leg-blue"></span> 已答</span>
              <span><span class="leg-dot leg-gray"></span> 未答</span>
            </div>
          </div>

          <!-- 右侧当前题目 -->
          <div class="exam-main">
            <div class="question-single" v-for="(q, idx) in questions" :key="idx" v-show="currentIdx === idx">
              <div class="qs-head">
                <el-tag :type="typeTag(q.questionType)" size="small">{{ typeLabel(q.questionType) }}</el-tag>
                <span class="qs-score">({{ q.score || 5 }}分)</span>
              </div>
              <div class="qs-stem"><b>第{{ idx + 1 }}题.</b> {{ q.stem }}</div>

              <!-- 单选 -->
              <el-radio-group v-if="q.questionType === 'single'" v-model="q.studentAnswer" class="qs-opts">
                <el-radio v-for="(opt, oi) in parsedOptions(q)" :key="oi" :label="opLetter(oi)" class="qs-opt-item">
                  {{ opLetter(oi) }}. {{ opt }}
                </el-radio>
              </el-radio-group>

              <!-- 多选 -->
              <el-checkbox-group v-if="q.questionType === 'multiple'" v-model="q.studentAnswer" class="qs-opts">
                <el-checkbox v-for="(opt, oi) in parsedOptions(q)" :key="oi" :label="opLetter(oi)" class="qs-opt-item">
                  {{ opLetter(oi) }}. {{ opt }}
                </el-checkbox>
              </el-checkbox-group>

              <!-- 判断 -->
              <el-radio-group v-if="q.questionType === 'judge'" v-model="q.studentAnswer" class="qs-opts">
                <el-radio label="true" class="qs-opt-item">正确</el-radio>
                <el-radio label="false" class="qs-opt-item">错误</el-radio>
              </el-radio-group>

              <!-- 填空 -->
              <el-input v-if="q.questionType === 'fill'" type="textarea" v-model="q.studentAnswer" :rows="2" placeholder="请输入答案，多个空用 | 分隔" class="qs-textarea" />

              <!-- 主观 -->
              <el-input v-if="q.questionType === 'essay'" type="textarea" v-model="q.studentAnswer" :rows="6" placeholder="请输入你的答案..." class="qs-textarea" />

              <div class="qs-foot">
                <el-button size="mini" :type="q.flagged ? 'warning' : 'default'" @click="q.flagged=!q.flagged">
                  <i :class="q.flagged?'el-icon-star-on':'el-icon-star-off'"></i> {{ q.flagged?'取消标记':'标记' }}
                </el-button>
              </div>
            </div>

            <!-- 上下题导航 -->
            <div class="exam-nav-bar">
              <el-button @click="prevQ" :disabled="currentIdx<=0">上一题</el-button>
              <span class="nav-page">第 {{ currentIdx+1 }} / {{ questions.length }} 题</span>
              <el-button @click="nextQ" :disabled="currentIdx>=questions.length-1">下一题</el-button>
            </div>
          </div>
        </div>

        <!-- 已加载试卷但题目为0 -->
        <el-empty v-else description="该试卷暂无题目，请联系老师确认试卷内容" />
      </div>

      <!-- 无题目且无文本 -->
      <el-empty v-if="!paperLoaded && !loading && !displayContent" description="暂无题目，请确认作业是否正确关联试卷" />

      <!-- 提交按钮 -->
      <div class="submit-bar">
        <el-button @click="$router.push('/studenthomeworkexam')">取消</el-button>
        <el-button type="primary" @click="doSubmit" :loading="submitting">{{ submitting?'提交中...':'提交作业' }}</el-button>
      </div>

      <!-- DEBUG: 数据诊断面板 -->
      <el-collapse v-if="!loading" style="margin-top:12px">
        <el-collapse-item title="🔍 数据诊断 (点击展开查看)" name="debug">
          <div style="font-size:12px;color:#909399;font-family:monospace;line-height:1.8">
            <p><b>homework.id:</b> {{ homework.id }}</p>
            <p><b>homework._type:</b> {{ homework._type }}</p>
            <p><b>homework.content (raw):</b> {{ homework.content ? homework.content.substring(0,300) : '(空)' }}</p>
            <p><b>API homework content:</b> {{ apiDebug.content ? apiDebug.content.substring(0,300) : '(空)' }}</p>
            <p><b>extracted paperId:</b> {{ apiDebug.paperId }}</p>
            <p><b>paperLoaded:</b> {{ paperLoaded }}</p>
            <p><b>questions.length:</b> {{ questions.length }}</p>
            <p><b>paper.questionCount:</b> {{ apiDebug.questionCount }}</p>
            <p><b>paper.questions (raw前200字符):</b> {{ apiDebug.questionsRaw }}</p>
            <p><b>paper raw data keys:</b> {{ apiDebug.paperKeys }}</p>
            <p><b>paper totalScore:</b> {{ totalScore }}</p>
            <p><b>displayContent:</b> {{ displayContent ? displayContent.substring(0,100) : '(空)' }}</p>
            <p><b>error:</b> {{ apiDebug.error }}</p>
          </div>
        </el-collapse-item>
      </el-collapse>
    </el-card>
  </div>
</template>

<script>
import Cookies from 'js-cookie'
import { getPaperDetail } from '@/api/teacher/examApi'
import { get } from '@/api/request'

const TYPE_NUM = { 1:'single', 2:'multiple', 3:'judge', 4:'fill', 5:'essay' }

export default {
  name: 'StudentHomeworkAnswer',
  data() {
    return {
      homework: { id: null, title: '', courseName: '', content: '', _type: 'homework' },
      questions: [],
      currentIdx: 0,
      simpleAnswer: '',
      displayContent: '',
      paperLoaded: false,
      loading: false,
      submitting: false,
      remainingTime: 0,
      timer: null,
      totalScore: 0,
      apiDebug: { content: '', paperId: null, paperKeys: '', error: '', questionCount: '', questionsRaw: '' }
    }
  },
  computed: {
    formattedTime() {
      const h = Math.floor(this.remainingTime/3600), m = Math.floor((this.remainingTime%3600)/60), s = this.remainingTime%60
      return `${String(h).padStart(2,'0')}:${String(m).padStart(2,'0')}:${String(s).padStart(2,'0')}`
    },
    timeWarning() { return this.remainingTime <= 300 }
  },
  created() {
    const q = this.$route.query
    if (q.id) {
      this.homework.id = parseInt(q.id)
      this.homework.title = q.title || ''
      this.homework.courseName = q.courseName || ''
      this.homework.content = q.content || ''
      this.homework._type = q._type || 'homework'
    }
    if (!this.homework.id) { this.$message.error('作业数据异常'); return }
    this.loadDetail()
  },
  beforeDestroy() { this.clearTimer() },
  methods: {
    opLetter(i) { return String.fromCharCode(65+i) },
    typeLabel(t) { return {single:'单选',multiple:'多选',judge:'判断',fill:'填空',essay:'主观'}[t]||t },
    typeTag(t) { return {single:'primary',multiple:'success',judge:'warning',fill:'info',essay:''}[t]||'' },
    numToType(n) { return TYPE_NUM[n]||n },
    parsedOptions(q) {
      if (!q || !q.options) return []
      let raw = q.options
      if (typeof raw === 'string') { try { raw = JSON.parse(raw) } catch(e) { raw = raw.split(',') } }
      if (!Array.isArray(raw)) return []
      return raw.map(o => {
        if (o === null || o === undefined) return ''
        if (typeof o === 'string') return o
        if (typeof o === 'object') return o.text||o.label||o.value||''
        return String(o)
      })
    },
    isAnswered(q) {
      if (q.questionType === 'multiple') return Array.isArray(q.studentAnswer) && q.studentAnswer.length>0
      return !!q.studentAnswer && !(Array.isArray(q.studentAnswer)&&q.studentAnswer.length===0)
    },
    goTo(i) { this.currentIdx = i },
    prevQ() { if(this.currentIdx>0) this.currentIdx-- },
    nextQ() { if(this.currentIdx<this.questions.length-1) this.currentIdx++ },
    clearTimer() { if (this.timer) { clearInterval(this.timer); this.timer = null } },
    startTimer() { this.clearTimer(); this.timer = setInterval(()=>{ if(this.remainingTime<=0){this.doSubmit(true);return} this.remainingTime-- },1000) },
    async loadDetail() {
      this.loading = true
      this.apiDebug = { content: '', paperId: null, paperKeys: '', error: '', questionCount: '', questionsRaw: '' }
      try {
        // Step 1: 从 query 的 content 先尝试提取 paperRef
        let paperId = this.extractPaperId(this.homework.content)
        this.apiDebug.paperId = paperId

        // Step 2: 调用 API 获取作业详情
        let apiHw = null
        try {
          const res = await get(`/study/homework/info/${this.homework.id}`)
          apiHw = (res.data && res.data.resultData) ? res.data.resultData : {}
          if (apiHw.content) {
            this.apiDebug.content = apiHw.content
            if (!paperId) {
              this.homework.content = apiHw.content
              paperId = this.extractPaperId(apiHw.content)
              this.apiDebug.paperId = paperId
            }
          }
          if (apiHw.title) this.homework.title = apiHw.title
          if (apiHw.content) this.homework.content = apiHw.content
        } catch (apiErr) {
          this.apiDebug.error = 'API /info 失败: ' + (apiErr.message || '')
        }

        if (paperId) {
          paperId = parseInt(paperId, 10)
          this.apiDebug.paperId = paperId
          const pr = await getPaperDetail(paperId)
          const paper = (pr.data && pr.data.resultData) ? pr.data.resultData : {}
          this.apiDebug.paperKeys = paper ? Object.keys(paper).join(', ') : '(paper为空)'
          this.apiDebug.questionCount = paper.questionCount
          this.apiDebug.questionsRaw = paper.questions ? JSON.stringify(paper.questions).substring(0, 200) : '(空)'
          this.totalScore = paper.totalScore || 0
          if (this.homework._type === 'exam') {
            this.remainingTime = (paper.duration || 60) * 60
            this.startTimer()
          }
          this.questions = (paper.questions || []).map(q => ({
            ...q,
            questionType: this.numToType(q.questionType),
            studentAnswer: (q.questionType === 2 || q.questionType === 'multiple') ? [] : '',
            flagged: false
          }))
          this.paperLoaded = true
        } else {
          const content = this.homework.content || ''
          let cd = null
          try { cd = JSON.parse(content) } catch (e) {}
          if (cd && cd.text) this.displayContent = cd.text
          else if (content && content.indexOf('paperRef:') !== 0) this.displayContent = content
        }
      } catch (e) {
        this.apiDebug.error = (e.message || String(e))
      }
      this.loading = false
    },
    extractPaperId(content) {
      if (!content) return null
      let cd = null
      try { cd = JSON.parse(content) } catch (e) {}
      if (cd && cd.paperRef) return cd.paperRef
      if (content && content.indexOf('paperRef:') === 0) return content.replace('paperRef:', '').trim()
      return null
    },
    async doSubmit(auto=false) {
      if (!auto) {
        if (!this.paperLoaded) { if(!this.simpleAnswer.trim()){this.$message.warning('请填写作答内容');return} }
        else { const un = this.questions.some(q=>!this.isAnswered(q)); if(un){ try{await this.$confirm('还有未完成的题目，确定提交？','提示',{type:'warning'})}catch(e){return} } }
      }
      this.submitting = true; this.clearTimer()
      try {
        let content
        if (!this.paperLoaded) content = this.simpleAnswer
        else content = JSON.stringify(this.questions.map(q=>({questionId:q.id,answer:Array.isArray(q.studentAnswer)?q.studentAnswer.join(','):(q.studentAnswer||'')})))
        const res = await this.$post('/study/userdohomework/save', {homeworkId:this.homework.id,userId:parseInt(Cookies.get('userId')),content})
        if (res.data&&res.data.code===200) { this.$message.success('提交成功'); this.$router.push('/studenthomeworkexam') }
        else this.$message.error('提交失败')
      } catch(e) { this.$message.error('提交失败') }
      this.submitting = false
    }
  }
}
</script>

<style scoped>
.hw-answer-page { padding: 16px; background: #f0f2f5; min-height: 100vh; }
.back-btn { margin-bottom: 10px; }
.hw-card { border-radius: 8px; max-width: 1200px; margin: 0 auto; }
.hw-header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 8px; }
.hw-title-row { display: flex; align-items: center; gap: 8px; }
.hw-title { font-size: 16px; }
.hw-course { color: #909399; font-size: 13px; }

.countdown-bar { font-size: 18px; font-weight: 700; color: #409EFF; background: #ecf5ff; border-radius: 6px; padding: 8px 16px; text-align: center; margin-bottom: 12px; }
.countdown-bar.countdown-warn { color: #F56C6C; background: #fef0f0; animation: blink 1s infinite; }
@keyframes blink { 0%,100%{opacity:1} 50%{opacity:.5} }

.simple-area { padding: 12px 0; }
.simple-text { color: #606266; line-height: 1.8; white-space: pre-wrap; margin-bottom: 12px; }
.simple-hint { color: #909399; }

.exam-layout { display: flex; gap: 16px; margin-top: 12px; align-items: flex-start; }
.exam-nav-panel {
  width: 130px; flex-shrink: 0; background: #fff; border-radius: 8px;
  padding: 14px 10px; box-shadow: 0 2px 6px rgba(0,0,0,.06);
  position: sticky; top: 16px;
}
.nav-info { text-align: center; margin: 0; font-size: 14px; color: #606266; }
.nav-dots { display: flex; flex-wrap: wrap; gap: 6px; }
.nav-dot {
  width: 30px; height: 30px; display: flex; align-items: center; justify-content: center;
  border-radius: 50%; font-size: 12px; font-weight: 700; cursor: pointer;
  background: #e8eaed; color: #909399; transition: all .15s;
}
.nav-dot:hover { background: #c6d9f0; }
.nav-dot.dot-done { background: #409EFF; color: #fff; }
.nav-dot.dot-curr { border: 2px solid #303133; }
.nav-dot.dot-mark { border: 2px solid #E6A23C; }
.nav-legend { display: flex; flex-direction: column; gap: 3px; font-size: 11px; color: #909399; }
.leg-dot { display: inline-block; width: 8px; height: 8px; border-radius: 50%; margin-right: 4px; }
.leg-blue { background: #409EFF; }
.leg-gray { background: #e8eaed; }

.exam-main { flex: 1; min-width: 0; background: #fff; border-radius: 8px; padding: 24px; box-shadow: 0 2px 6px rgba(0,0,0,.04); }
.question-single { min-height: 300px; }
.qs-head { margin-bottom: 12px; display: flex; align-items: center; gap: 8px; }
.qs-score { color: #909399; font-size: 13px; }
.qs-stem { font-size: 16px; line-height: 1.7; color: #303133; margin-bottom: 20px; }
.qs-opts { display: flex; flex-direction: column; gap: 10px; }
.qs-opt-item { padding: 10px 14px; background: #fafafa; border: 1px solid #e4e7ed; border-radius: 6px; margin: 0 !important; transition: all .15s; }
.qs-opt-item:hover { border-color: #409EFF; background: #ecf5ff; }
.qs-textarea { margin-bottom: 16px; }
.qs-foot { margin-top: 16px; display: flex; justify-content: flex-end; }
.exam-nav-bar { display: flex; justify-content: center; align-items: center; gap: 20px; margin-top: 24px; padding-top: 16px; border-top: 1px solid #ebeef5; }
.nav-page { color: #909399; font-size: 14px; }
.submit-bar { text-align: right; margin-top: 16px; }
</style>
