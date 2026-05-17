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
            <el-select v-model="form.answer" placeholder="选择正确答案" style="width:100%">
              <el-option v-for="o in form.options" :key="o.label" :label="o.label" :value="o.label" />
            </el-select>
          </template>
          <template v-else-if="form.questionType === 'multiple'">
            <el-checkbox-group v-model="form.answer">
              <el-checkbox v-for="o in form.options" :key="o.label" :label="o.label">{{ o.text }}</el-checkbox>
            </el-checkbox-group>
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

const TYPE_MAP = { single: '单选题', multiple: '多选题', judge: '判断题', fill: '填空题', essay: '主观题' }

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
      form: {
        courseId: '', questionType: '', stem: '', options: [
          { label: 'A', text: '' }, { label: 'B', text: '' }
        ],
        answer: '', score: 5, analysis: ''
      },
      saving: false
    }
  },
  created() { this.loadCourses(); this.loadQuestions() },
  methods: {
    typeLabel(t) { return TYPE_MAP[t] || t },
    typeTag(t) { return { single: 'primary', multiple: 'success', judge: 'warning', fill: 'info', essay: '' }[t] || '' },
    async loadCourses() {
      try {
        const res = await getCourses()
        // getCourses 调 GET /study/teacher/dashboard/subjects
        // 返回格式: { code: 0, data: { list: [ ... ] } }
        this.courseList = (res.data && res.data.data && res.data.data.list) ? res.data.data.list : []
      } catch (e) { this.courseList = [] }
    },
    async loadQuestions() {
      this.loading = true
      try {
        const params = { page: this.page, limit: this.limit, courseId: this.filter.courseId || undefined, questionType: this.filter.questionType || undefined }
        const res = await this.$post('/study/exam/question/list', params)
        this.questionList = (res.data && res.data.data) ? res.data.data : []
        this.total = (res.data && res.data.total) ? res.data.total : 0
      } catch (e) { this.questionList = []; this.total = 0 }
      this.loading = false
    },
    addOption() {
      const labels = 'ABCDEF'
      if (this.form.options.length < 6) {
        this.form.options.push({ label: labels[this.form.options.length], text: '' })
      }
    },
    resetForm() {
      this.form = {
        courseId: '', questionType: '', stem: '', options: [
          { label: 'A', text: '' }, { label: 'B', text: '' }
        ],
        answer: '', score: 5, analysis: ''
      }
      this.isEdit = false; this.editId = null
    },
    editQuestion(row) {
      this.isEdit = true; this.editId = row.id
      this.form = {
        courseId: row.courseId || '',
        questionType: row.questionType || '',
        stem: row.stem || '',
        options: row.options ? (typeof row.options === 'string' ? JSON.parse(row.options) : row.options) : [{ label: 'A', text: '' }, { label: 'B', text: '' }],
        answer: row.answer || '',
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
      if (!this.form.answer) { this.$message.warning('请填写答案'); return }
      this.saving = true
      try {
        const params = {
          courseId: this.form.courseId,
          questionType: this.form.questionType,
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
          params.userId = this.$cookies.get('userId')
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
    'filter.courseId'() { this.loadQuestions() }
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
</style>