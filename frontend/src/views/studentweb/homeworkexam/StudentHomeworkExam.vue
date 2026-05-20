<template>
  <div class="homework-exam">
    <div class="page-header">
      <h2><i class="el-icon-edit-outline"></i> 作业考试</h2>
    </div>

    <el-tabs v-model="activeTab" type="border-card">
      <el-tab-pane label="待完成" name="pending">
        <el-card shadow="never" v-loading="pendingLoading">
          <el-empty v-if="!pendingLoading && pendingList.length === 0" description="暂没有需要完成的作业或考试" />
          <el-table v-else :data="pendingList" stripe style="width:100%">
            <el-table-column prop="title" label="名称" min-width="200" />
            <el-table-column prop="courseName" label="所属课程" width="140" />
            <el-table-column label="类型" width="70" align="center">
              <template slot-scope="scope">
                <el-tag :type="scope.row._type === 'exam' ? 'danger' : 'warning'" size="small">
                  {{ scope.row._type === 'exam' ? '考试' : '作业' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="截止时间" width="170" align="center">
              <template slot-scope="scope">{{ scope.row.commitTime || scope.row.endTime || scope.row.createTime }}</template>
            </el-table-column>
            <el-table-column label="操作" width="130" align="center">
              <template slot-scope="scope">
                <el-button type="primary" size="mini" @click="startItem(scope.row)">
                  {{ scope.row._type === 'exam' && scope.row.recordStatus === 'draft' ? '继续答题' : '开始答题' }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="已完成" name="completed">
        <el-card shadow="never" v-loading="completedLoading">
          <el-empty v-if="!completedLoading && completedList.length === 0" description="暂无已完成记录" />
          <el-table v-else :data="completedList" stripe style="width:100%">
            <el-table-column prop="title" label="名称" min-width="200" />
            <el-table-column prop="courseName" label="所属课程" width="140" />
            <el-table-column label="类型" width="70" align="center">
              <template slot-scope="scope">
                <el-tag :type="scope.row._type === 'exam' ? 'danger' : 'warning'" size="small">
                  {{ scope.row._type === 'exam' ? '考试' : '作业' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100" align="center">
              <template slot-scope="scope">
                <el-tag v-if="scope.row.mode === '2'" type="danger" size="small">已打回</el-tag>
                <el-tag v-else-if="scope.row.mode === '1'" type="success" size="small">已批改</el-tag>
                <el-tag v-else type="warning" size="small">待批改</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="完成时间" width="170" align="center">
              <template slot-scope="scope">{{ scope.row.completionTime || scope.row.createTime }}</template>
            </el-table-column>
            <el-table-column label="得分/评语" width="120" align="center">
              <template slot-scope="scope">
                <span v-if="scope.row.score" style="color:#67C23A;font-weight:600">{{ scope.row.score }}分</span>
                <span v-else-if="scope.row.remark" style="color:#909399;font-size:12px">{{ scope.row.remark }}</span>
                <span v-else style="color:#909399">-</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" align="center">
              <template slot-scope="scope">
                <el-button v-if="scope.row.mode === '2'" type="warning" size="mini" @click="redoHomework(scope.row)">重新答题</el-button>
                <el-button v-else type="text" size="mini" @click="viewDetail(scope.row)">查看</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import Cookies from 'js-cookie'
import { getStudentPapers } from '@/api/teacher/examApi'

export default {
  name: 'StudentHomeworkExam',
  data() {
    return {
      activeTab: 'pending',
      pendingLoading: false,
      completedLoading: false,
      pendingList: [],
      completedList: []
    }
  },
  created() {
    this.loadPending()
    this.loadCompleted()
  },
  methods: {
    async loadPending() {
      this.pendingLoading = true
      const userId = parseInt(Cookies.get('userId'))
      const classId = parseInt(Cookies.get('classId'))
      if (!classId) {
        this.pendingList = []
        this.pendingLoading = false
        return
      }
      try {
        const [hwRes, examRes] = await Promise.all([
          this.$post('/study/homework/findNotDoHomework', { userId, classId, page: 1, pageSize: 50 }),
          getStudentPapers({ classId, page: 1, pageSize: 50 })
        ])
        const hwData = hwRes.data && hwRes.data.resultData
        const hwList = (hwData && hwData.data) ? hwData.data : []
        const examData = examRes.data && examRes.data.resultData
        const examList = (examData && examData.data) ? examData.data : []
        this.pendingList = [
          ...hwList.map(i => ({ ...i, _type: 'homework' })),
          ...examList.map(i => ({ ...i, _type: 'exam' }))
        ]
      } catch (e) { this.pendingList = [] }
      this.pendingLoading = false
    },
    async loadCompleted() {
      this.completedLoading = true
      const userId = parseInt(Cookies.get('userId'))
      try {
        const [hwRes, examRes] = await Promise.all([
          this.$post('/study/userdohomework/list', { userId, page: 1, pageSize: 50 }),
          Promise.resolve({ data: { resultData: { data: [] } } })
        ])
        const hwData = hwRes.data && hwRes.data.resultData
        const hwList = (hwData && hwData.data) ? hwData.data : []
        const examData = examRes.data && examRes.data.resultData
        const examList = (examData && examData.data) ? examData.data : []
        this.completedList = [
          ...hwList.map(i => ({ ...i, _type: 'homework' })),
          ...examList.map(i => ({ ...i, _type: 'exam' }))
        ]
      } catch (e) { this.completedList = [] }
      this.completedLoading = false
    },
    startItem(row) {
      if (row._type === 'exam') {
        this.$router.push({ name: 'StudentExam', query: { paperId: row.id } })
      } else {
        this.$router.push({ path: '/studenthomeworkanswer', query: { id: row.id, title: row.title, courseName: row.courseName, content: row.content || '' } })
      }
    },
    redoHomework(row) {
      this.$router.push({ path: '/studenthomeworkanswer', query: { id: row.homeworkId, title: row.title, courseName: row.courseName, content: '' } })
    },
    viewDetail(row) {
      if (row._type === 'exam') {
        this.$router.push({ name: 'ExamResult', query: { paperId: row.paperId || row.id, studentId: Cookies.get('userId') } })
      } else {
        this.$message.info('作业批改后可查看详情')
      }
    }
  }
}
</script>

<style scoped>
.homework-exam { padding: 20px; }
.page-header { margin-bottom: 20px; }
.page-header h2 { font-size: 20px; color: #303133; margin: 0; }
.page-header h2 i { margin-right: 8px; color: #409EFF; }
</style>
