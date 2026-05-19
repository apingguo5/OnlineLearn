<template>
  <div class="teacher-question-bank">
    <div class="page-header">
      <h2><i class="el-icon-document-copy"></i> 题库管理</h2>
      <el-button type="primary" icon="el-icon-plus" size="small" @click="showCreateDialog = true">录入题目</el-button>
    </div>

    <!-- 筛选栏 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" size="small">
        <el-form-item label="课程">
          <el-select v-model="filter.courseId" placeholder="全部课程" clearable style="width:180px">
            <el-option v-for="c in courseList" :key="c.id" :label="c.courseName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="题型">
          <el-select v-model="filter.questionType" placeholder="全部题型" clearable style="width:120px">
            <el-option label="单选题" value="single" />
            <el-option label="多选题" value="multiple" />
            <el-option label="判断题" value="judge" />
            <el-option label="填空题" value="fill" />
            <el-option label="主观题" value="essay" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadQuestions">查询</el-button>
          <el-button @click="filter={courseId:'',questionType:''};loadQuestions()">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 题目列表 -->
    <el-card shadow="never" class="main-card">
      <el-table :data="questionList" v-loading="loading" stripe style="width:100%">
        <el-table-column prop="stem" label="题目" min-width="300" show-overflow-tooltip />
        <el-table-column label="题型" width="80" align="center">
          <template slot-scope="scope">
            <el-tag :type="typeTag(scope.row.questionType)" size="small">{{ typeLabel(scope.row.questionType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="courseName" label="所属课程" min-width="130" />
        <el-table-column prop="score" label="分值" width="60" align="center" />
        <el-table-column prop="createTime" label="创建时间" width="170" align="center" />
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" icon="el-icon-edit" @click="editQuestion(scope.row)">编辑</el-button>
            <el-button type="text" icon="el-icon-delete" style="color:#F56C6C" @click="deleteQuestion(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        @size-change="s=>{limit=s;loadQuestions()}"
        @current-change="p=>{page=p;loadQuestions()}"
        :current-page="page"
        :page-sizes="[10, 20, 50]"
        :page-size="limit"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        style="margin-top:16px;text-align:right"
      />
    </el-card>

    <!-- 录入/编辑题目对话框 -->
    <el-dialog :title="isEdit ? '编辑题目' : '录入题目'" :visible.sync="showCreateDialog" width="700px" @closed="resetForm">
      <el-form :model="form" label-width="100px" size="small">
        <el-form-item label="所属课程" required>
          <el-select v-model="form.courseId" placeholder="请选择课程" style="width:100%">
            <el-option v-for="c in courseList" :key="c.id" :label="c.courseName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="题型" required>
          <el-select v-model="form.questionType" placeholder="请选择题型" style="width:100%">
            <el-option label="单选题" value="single" />
            <el-option label="多选题" value="multiple" />
            <el-option label="判断题" value="judge" />
            <el-option label="填空题" value="fill" />
            <el-option label="主观题" value="essay" />
          </el-select>
        </el-form-item>
        <el-form-item label="题目内容" required>
          <el-input type="textarea" v-model="form.stem" :rows="3" placeholder="请输入题目内容" />
        </el-form-item>
        <el-form-item label="选项" v-if="form.questionType === 'single' || form.questionType === 'multiple'">
          <div class="options-editor">
            <div v-for="(opt, idx) in form.options" :key="idx" class="option-row">
              <span class="opt-label">{{ opt.label }}.</span>
              <el-input v-model="opt.text" placeholder="选项内容" size="small" style="flex:1" />
              <el-button type="danger" icon="el-icon-delete" circle size="mini" @click="form.options.splice(idx, 1)" v-if="form.options.length > 2" />
            </div>
            <el-button size="mini" icon="el-icon-plus" @click="addOption" v-if="form.options.length < 6">添加选项</el-button>
            <span class="opt-hint">至少填写2个选项，建议填写4个</span>
          </div>
        </el-form-item>
        <el-form-item label="答案" required>
          <template v-if="form.questionType === 'judge'">
            <el-radio-group v-model="form.answer">
              <el-radio label="true">对</el-radio>
              <el-radio label="false">错</el-radio>
            </el-radio-group>
          </template>
          <template v-else-if="form.questionType === 'single'">
            <div class="answer-options">
              <el-radio-group v-model="form.answer">
                <el-radio v-for="o in form.options" :key="o.label" :label="o.label" class="answer-radio">
                  <span class="answer-opt-text">{{ o.label }}. {{ o.text || '(空)' }}</span>
                </el-radio>
              </el-radio-group>
            </div>
          </template>
          <template v-else-if="form.questionType === 'multiple'">
            <div class="answer-options">
              <el-checkbox-group v-model="form.answer">
                <el-checkbox v-for="o in form.options" :key="o.label" :label="o.label" class="answer-checkbox">
                  <span class="answer-opt-text">{{ o.label }}. {{ o.text || '(空)' }}</span>
                </el-checkbox>
              </el-checkbox-group>
              <div class="answer-hint">请勾选所有正确答案，可多选</div>
            </div>
          </template>
          <el-input v-else v-model="form.answer" type="textarea" :rows="2" placeholder="请输入答案" style="width:100%" />
        </el-form-item>
        <el-form-item label="分值">
          <el-input-number v-model="form.score" :min="1" :max="100" />
        </el-form-item>
        <el-form-item label="解析">
          <el-input type="textarea" v-model="form.analysis" :rows="2" placeholder="题目解析（可选）" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="saveQuestion" :loading="saving">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getCourses } from '@/api/teacher/teacherApi'

const TYPE_MAP = { 1: '单选题', 2: '多选题', 3: '判断题', 4: '填空题', 5: '主观题' }
const TYPE_REVERSE = { single: 1, multiple: 2, judge: 3, fill: 4, essay: 5 }
const LABELS = 'ABCDEFGH'

export default {
  name: 'TeacherQuestionBank',
  data() {
    return {
      loading: false,
      questionList: [],
      courseList: [],
      total: 0, page: 1, limit: 10,
      filter: { courseId: '', questionType: '' },
      showCreateDialog: false,
      isEdit: false,
      editId: null,
      form: this.getDefaultForm(),
      saving: false
    }
  },
  created() { this.loadCourses(); this.loadQuestions() },
  methods: {
    // 将数字题型转为字符串前端标识，或直接返回已有字符串
    numToType(n) { const map = { 1:'single', 2:'multiple', 3:'judge', 4:'fill', 5:'essay' }; return map[n] || n },
    typeLabel(t) { return TYPE_MAP[t] || t },
    typeTag(t) {
      // t 可能是数字（来自后端）或字符串（来自前端表单）
      const str = typeof t === 'number' ? this.numToType(t) : t
      return { single: 'primary', multiple: 'success', judge: 'warning', fill: 'info', essay: '' }[str] || ''
    },
    getDefaultForm() {
      return {
        courseId: '', questionType: '', stem: '',
        options: [
          { label: 'A', text: '' },
          { label: 'B', text: '' },
          { label: 'C', text: '' },
          { label: 'D', text: '' }
        ],
        answer: '',
        score: 5,
        analysis: ''
      }
    },
    async loadCourses() {
      try {
        const res = await getCourses()
        this.courseList = (res.data && res.data.data && res.data.data.list) ? res.data.data.list : []
      } catch (e) { this.courseList = [] }
    },
    async loadQuestions() {
      this.loading = true
      try {
        const params = { page: this.page, limit: this.limit, courseId: this.filter.courseId || undefined }
        if (this.filter.questionType) {
          params.questionType = TYPE_REVERSE[this.filter.questionType] || this.filter.questionType
        }
        const res = await this.$post('/study/exam/question/list', params)
        const resultData = res.data && res.data.resultData
        this.questionList = (resultData && resultData.data) ? resultData.data : []
        this.total = (resultData && resultData.total) ? resultData.total : 0
      } catch (e) { this.questionList = []; this.total = 0 }
      this.loading = false
    },
    addOption() {
      if (this.form.options.length < 6) {
        this.form.options.push({ label: LABELS[this.form.options.length], text: '' })
      }
    },
    resetForm() {
      this.form = this.getDefaultForm()
      this.isEdit = false; this.editId = null
    },
    editQuestion(row) {
      this.isEdit = true; this.editId = row.id
      // 解析选项
      let parsedOptions = []
      if (row.options) {
        parsedOptions = typeof row.options === 'string' ? JSON.parse(row.options) : row.options
      } else {
        parsedOptions = [
          { label: 'A', text: '' },
          { label: 'B', text: '' }
        ]
      }
      // 后端返回的数字题型转为前端字符串标识
      const typeStr = typeof row.questionType === 'number' ? this.numToType(row.questionType) : (row.questionType || '')
      // 解析答案：多选题答案需拆分为数组
      let parsedAnswer = row.answer || ''
      if (typeStr === 'multiple' && typeof parsedAnswer === 'string') {
        parsedAnswer = parsedAnswer ? parsedAnswer.split(',').map(s => s.trim()).filter(Boolean) : []
      }
      this.form = {
        courseId: row.courseId || '',
        questionType: typeStr,
        stem: row.stem || '',
        options: parsedOptions,
        answer: parsedAnswer,
        score: row.score || 5,
        analysis: row.analysis || ''
      }
      this.showCreateDialog = true
    },
    async saveQuestion() {
      if (!this.form.courseId) { this.$message.warning('请选择所属课程'); return }
      if (!this.form.questionType) { this.$message.warning('请选择题型'); return }
      if (!this.form.stem) { this.$message.warning('请输入题目内容'); return }
      if ((this.form.questionType === 'single' || this.form.questionType === 'multiple') && !this.form.options.some(o => o.text)) {
        this.$message.warning('请填写选项内容'); return
      }
      if ((this.form.questionType === 'single' || this.form.questionType === 'multiple') && this.form.options.filter(o => o.text).length < 2) {
        this.$message.warning('至少需要填写2个有内容的选项'); return
      }
      // 验证答案
      if (this.form.questionType === 'multiple' && (!Array.isArray(this.form.answer) || this.form.answer.length === 0)) {
        this.$message.warning('请勾选正确答案'); return
      }
      if (this.form.questionType !== 'multiple' && !this.form.answer) {
        this.$message.warning('请填写答案'); return
      }
      this.saving = true
      try {
        const params = {
          courseId: parseInt(this.form.courseId, 10),
          // 前端字符串题型转为后端期望的数字
          questionType: TYPE_REVERSE[this.form.questionType] || this.form.questionType,
          stem: this.form.stem,
          options: JSON.stringify(this.form.options),
          answer: Array.isArray(this.form.answer) ? this.form.answer.join(',') : this.form.answer,
          score: this.form.score,
          analysis: this.form.analysis,
        }
        if (this.isEdit) {
          params.id = this.editId
          await this.$post('/study/exam/question/update', params)
          this.$message.success('更新成功')
        } else {
          params.creatorId = parseInt(this.$cookies.get('userId') || '0')
          await this.$post('/study/exam/question/save', params)
          this.$message.success('录入成功')
        }
        this.showCreateDialog = false
        this.loadQuestions()
      } catch (e) { this.$message.error('保存失败') }
      this.saving = false
    },
    deleteQuestion(row) {
      this.$confirm('确定删除该题目吗？', '提示', { type: 'warning' })
        .then(async () => { try { await this.$post('/study/exam/question/delete', { id: row.id }); this.$message.success('删除成功'); this.loadQuestions() } catch (e) { this.$message.error('删除失败') } })
        .catch(() => {})
    }
  },
  watch: {
    'filter.courseId'() { this.loadQuestions() },
    // 选择题型变化时重置答案字段类型（多选题需用数组）
    'form.questionType'(type) {
      if (type === 'multiple') {
        // 多选题答案必须为数组
        if (!Array.isArray(this.form.answer)) {
          this.form.answer = []
        }
      } else if (type === 'single' || type === 'judge') {
        // 单选/判断题答案重置为空字符串
        this.form.answer = ''
      } else if (type === 'fill' || type === 'essay') {
        this.form.answer = ''
      }
    }
  }
}
</script>

<style scoped>
.teacher-question-bank { padding: 24px; background: #f5f7fa; min-height: 100vh; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { margin: 0; font-size: 22px; color: #303133; }
.page-header h2 i { margin-right: 8px; color: #409EFF; }
.filter-card { margin-bottom: 16px; border-radius: 8px; }
.main-card { border-radius: 8px; }
.options-editor { }
.option-row { display: flex; align-items: center; gap: 8px; margin-bottom: 6px; }
.opt-label { font-weight: bold; width: 20px; }
.opt-hint { margin-left: 12px; font-size: 12px; color: #909399; }
.answer-options { padding: 4px 0; }
.answer-radio, .answer-checkbox { display: flex; align-items: center; margin-bottom: 6px; }
.answer-opt-text { margin-left: 4px; font-size: 14px; color: #303133; }
.answer-hint { margin-top: 6px; font-size: 12px; color: #909399; }
</style>