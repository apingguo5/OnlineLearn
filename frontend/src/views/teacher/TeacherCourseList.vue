<template>
  <div class="course-list-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2><i class="el-icon-reading"></i> 课程管理</h2>
        <span class="header-subtitle">管理你的所有课程大纲与资料</span>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="showCreateDialog = true" icon="el-icon-plus">
          新建课程
        </el-button>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="filter-bar">
      <el-input
        v-model="searchQuery"
        placeholder="搜索课程名称..."
        prefix-icon="el-icon-search"
        clearable
        @input="filterCourses"
      />
    </div>

    <!-- 课程卡片网格 -->
    <div v-if="filteredCourses.length > 0" class="course-grid">
      <div
        v-for="course in filteredCourses"
        :key="course.id"
        class="course-card"
        @click="enterCourseEditor(course)"
      >
        <!-- 课程封面 -->
        <div class="card-cover" :style="{ background: getCourseColor(course.id) }">
          <img v-if="course.coverUrl" :src="getCoverUrl(course)" class="cover-image" @error="e => e.target.style.display='none'" />
          <div class="cover-icon" v-show="!course.coverUrl">{{ (course.courseName || '').charAt(0).toUpperCase() }}</div>
        </div>

        <!-- 课程信息 -->
        <div class="card-body">
          <h3 class="course-name" :title="course.courseName">{{ course.courseName }}</h3>
          <p class="course-desc" :title="course.description || ''">
            {{ course.description || '暂无简介' }}
          </p>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="empty-state">
      <i class="el-icon-reading empty-icon"></i>
      <p v-if="!loading">还没有课程，点击上方按钮创建你的第一门课程</p>
      <p v-else><i class="el-icon-loading"></i> 加载中...</p>
    </div>

    <!-- 新建课程对话框 -->
    <el-dialog
      title="新建课程"
      :visible.sync="showCreateDialog"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="createForm" label-width="100px" :rules="createRules" ref="createForm">
        <el-form-item label="课程名称" prop="courseName">
          <el-input v-model="createForm.courseName" placeholder="输入课程名称" maxlength="100" />
        </el-form-item>
        <el-form-item label="课程简介" prop="description">
          <el-input
            v-model="createForm.description"
            type="textarea"
            :rows="3"
            placeholder="输入课程简介（可选）"
            maxlength="500"
          />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" native-type="button" @click="createCourse" :loading="creating">创建</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getMyCourses, createCourse as apiCreateCourse } from '@/api/teacher/teacherApi'

export default {
  name: 'TeacherCourseList',
  data() {
    return {
      courses: [],
      filteredCourses: [],
      searchQuery: '',
      showCreateDialog: false,
      creating: false,
      loading: false,
      createForm: {
        courseName: '',
        description: ''
      },
      createRules: {
        courseName: [
          { required: true, message: '课程名称不能为空', trigger: 'blur' },
          { max: 100, message: '名称不超过100字符', trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    currentUserId() {
      const raw = this.$store?.state?.user?.id || localStorage.getItem('userId')
      const id = Number(raw)
      if (!id || id <= 0) {
        console.warn('[TeacherCourseList] 未能获取有效 userId, raw:', raw)
        return 1
      }
      return id
    }
  },
  created() {
    this.loadCourses()
  },
  methods: {
    async loadCourses() {
      this.loading = true
      try {
        const resp = await getMyCourses({ userId: this.currentUserId })
        // 后端返回 { code: 200, resultData: [...] }
        const body = resp.data
        if (body && body.code === 200) {
          this.courses = body.resultData || []
        } else {
          this.courses = []
        }
        this.filterCourses()
      } catch (e) {
        console.error('加载课程失败:', e)
        this.courses = []
        this.filteredCourses = []
        this.$message.error('加载课程列表失败')
      } finally {
        this.loading = false
      }
    },
    filterCourses() {
      let list = [...this.courses]
      if (this.searchQuery) {
        const q = this.searchQuery.toLowerCase()
        list = list.filter(c => (c.courseName || '').toLowerCase().includes(q))
      }
      // 按更新时间排序
      list.sort((a, b) => {
        const ta = a.updateTime || a.createTime || ''
        const tb = b.updateTime || b.createTime || ''
        return String(tb).localeCompare(String(ta))
      })
      this.filteredCourses = list
    },
    getCoverUrl(course) {
      if (!course.coverUrl) return ''
      if (course.coverUrl.startsWith('http://') || course.coverUrl.startsWith('https://')) {
        return course.coverUrl
      }
      if (course.coverUrl.startsWith('./')) {
        return this.$store.state.baseApi + '/resource/' + course.coverUrl.substring(2)
      }
      return this.$store.state.baseApi + course.coverUrl
    },
    enterCourseEditor(course) {
      this.$router.push({
        name: 'TeacherCourseManagement',
        params: { courseId: course.id },
        query: { courseName: course.courseName }
      })
    },
    getCourseColor(id) {
      const colors = [
        'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
        'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
        'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
        'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
        'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
        'linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%)',
        'linear-gradient(135deg, #fccb90 0%, #d57eeb 100%)',
        'linear-gradient(135deg, #e0c3fc 0%, #8ec5fc 100%)'
      ]
      let hash = 0
      for (let i = 0; i < String(id).length; i++) {
        hash = ((hash << 5) - hash) + String(id).charCodeAt(i)
      }
      return colors[Math.abs(hash) % colors.length]
    },
    async createCourse() {
      try {
        await this.$refs.createForm.validate()
      } catch {
        return
      }
      this.creating = true
      try {
        const resp = await apiCreateCourse({
          courseName: this.createForm.courseName,
          description: this.createForm.description,
          userId: this.currentUserId
        })
        const body = resp.data
        if (body && body.code === 200) {
          this.$message.success('课程创建成功')
          this.showCreateDialog = false
          this.createForm = { courseName: '', description: '' }
          await this.loadCourses()
        } else {
          this.$message.error(body?.resultData || '创建失败')
        }
      } catch (e) {
        console.error('创建失败:', e)
        this.$message.error('创建失败: ' + (e.msg || e.message || '未知错误'))
      } finally {
        this.creating = false
      }
    }
  }
}
</script>

<style scoped>
.course-list-page {
  padding: 24px;
  height: 100%;
  overflow-y: auto;
  background: #f5f7fa;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-left h2 {
  margin: 0 0 4px 0;
  font-size: 22px;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-left h2 i {
  color: #409eff;
}

.header-subtitle {
  font-size: 13px;
  color: #909399;
}

.header-right {
  display: flex;
  gap: 10px;
}

.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.filter-bar .el-input {
  width: 280px;
}

.course-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

.course-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  transition: transform 0.2s, box-shadow 0.2s;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  border: 1px solid #ebeef5;
}

.course-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.card-cover {
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  color: #fff;
}

.cover-icon {
  font-size: 48px;
  font-weight: bold;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.card-body {
  padding: 16px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.course-name {
  margin: 0 0 6px 0;
  font-size: 16px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.course-desc {
  margin: 0 0 12px 0;
  font-size: 13px;
  color: #909399;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  min-height: 36px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 0;
  color: #c0c4cc;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}
</style>