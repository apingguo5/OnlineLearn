<template>
  <div class="student-courses">
    <div class="page-header">
      <h1 class="page-title">我的课程</h1>
      <span class="course-count">共 {{ courseList.length }} 门课程</span>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <i class="el-icon-loading"></i>
      <span>加载中...</span>
    </div>

    <!-- 课程列表 -->
    <div v-else-if="courseList.length > 0" class="course-grid">
      <div
        v-for="(course, index) in courseList"
        :key="index"
        class="course-card"
        @click="goToCourse(course)"
      >
        <!-- 卡片头部：课程名称 + 学期 -->
        <div class="card-header">
          <div class="course-name">{{ course.courseName }}</div>
          <el-tag size="mini" type="success" v-if="course.semester === 1">春季</el-tag>
          <el-tag size="mini" type="warning" v-else-if="course.semester === 2">秋季</el-tag>
          <el-tag size="mini" v-else>{{ course.semester || '--' }}</el-tag>
        </div>

        <!-- 课程描述 -->
        <div class="card-body">
          <p class="course-desc" v-if="course.courseDescription">
            {{ course.courseDescription }}
          </p>
          <p class="course-desc empty-desc" v-else>暂无课程描述</p>
        </div>

        <!-- 教师信息 -->
        <div class="card-teacher">
          <i class="el-icon-user-solid"></i>
          <span>{{ course.userName || '未指定教师' }}</span>
        </div>

        <!-- 时间信息 -->
        <div class="card-footer">
          <div class="time-info">
            <div class="time-item" v-if="course.academicYear">
              <i class="el-icon-date"></i>
              <span>{{ course.academicYear }} 学年</span>
            </div>
            <div class="time-item" v-if="course.startTime || course.endTime">
              <i class="el-icon-time"></i>
              <span v-if="course.startTime && course.endTime">
                {{ formatDate(course.startTime) }} ~ {{ formatDate(course.endTime) }}
              </span>
              <span v-else-if="course.startTime">
                开始于 {{ formatDate(course.startTime) }}
              </span>
              <span v-else>
                结束于 {{ formatDate(course.endTime) }}
              </span>
            </div>
            <div class="time-item">
              <i class="el-icon-collection-tag"></i>
              <span>{{ course.className }}</span>
            </div>
          </div>
        </div>

        <!-- 进度指示 -->
        <div class="card-arrow">
          <i class="el-icon-arrow-right"></i>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="empty-state">
      <i class="el-icon-notebook-1 empty-icon"></i>
      <p class="empty-text">还没有加入任何课程</p>
      <p class="empty-hint">请联系教师加入班级，开始学习之旅</p>
    </div>
  </div>
</template>

<script>
import { getStudentClasses } from '@/api/studentweb/studentClass'

export default {
  name: 'StudentCourses',
  data() {
    return {
      courseList: [],
      loading: false
    }
  },
  created() {
    this.fetchCourses()
  },
  methods: {
    async fetchCourses() {
      this.loading = true
      try {
        const userInfo = this.$store.state.userInfo
        if (!userInfo || !userInfo.id) {
          this.loading = false
          return
        }
        const res = await getStudentClasses({ userId: userInfo.id })
        if (res.data && res.data.code === 0) {
          this.courseList = res.data.data || []
        } else {
          console.error('获取课程列表失败:', res.data)
        }
      } catch (err) {
        console.error('获取课程列表异常:', err)
      } finally {
        this.loading = false
      }
    },
    formatDate(dateStr) {
      if (!dateStr) return '--'
      return dateStr.substring(0, 10)
    },
    goToCourse(course) {
      // 后续可以跳转到课程详情页面
      this.$message.info('课程详情功能开发中...')
    }
  }
}
</script>

<style scoped>
.student-courses {
  padding: 24px;
  min-height: calc(100vh - 80px);
  background: #f0f2f5;
}

.page-header {
  display: flex;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  font-size: 22px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0;
}

.course-count {
  margin-left: 12px;
  font-size: 14px;
  color: #909399;
  background: #e8eaed;
  padding: 2px 12px;
  border-radius: 12px;
}

/* 加载状态 */
.loading-state {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 80px 0;
  color: #909399;
  font-size: 16px;
  gap: 10px;
}

.loading-state i {
  font-size: 24px;
}

/* 课程网格 */
.course-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 20px;
}

/* 课程卡片 */
.course-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  border: 1px solid #ebeef5;
  display: flex;
  flex-direction: column;
}

.course-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  border-color: #409eff;
}

.card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 12px;
}

.course-name {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a2e;
  flex: 1;
  margin-right: 12px;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.card-body {
  margin-bottom: 12px;
  flex: 1;
}

.course-desc {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
}

.course-desc.empty-desc {
  color: #c0c4cc;
  font-style: italic;
}

.card-teacher {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #606266;
  margin-bottom: 12px;
  padding: 8px 12px;
  background: #f5f7fa;
  border-radius: 8px;
}

.card-teacher i {
  color: #409eff;
  font-size: 15px;
}

.card-footer {
  border-top: 1px solid #f0f2f5;
  padding-top: 12px;
}

.time-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.time-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #909399;
}

.time-item i {
  font-size: 14px;
  width: 16px;
  text-align: center;
}

.card-arrow {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  color: #c0c4cc;
  font-size: 18px;
  opacity: 0;
  transition: opacity 0.3s;
}

.course-card:hover .card-arrow {
  opacity: 1;
  color: #409eff;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 80px 0;
}

.empty-icon {
  font-size: 80px;
  color: #dcdfe6;
  margin-bottom: 20px;
}

.empty-text {
  font-size: 18px;
  color: #606266;
  margin-bottom: 8px;
}

.empty-hint {
  font-size: 14px;
  color: #c0c4cc;
}
</style>