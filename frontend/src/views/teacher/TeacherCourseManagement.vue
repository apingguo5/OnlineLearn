<template>
  <div class="course-management">
    <!-- ==================== 顶部操作栏 ==================== -->
    <div class="top-bar">
      <div class="top-bar-left">
        <h2>课程管理</h2>
      </div>
      <div class="top-bar-right">
        <el-button type="primary" size="medium" @click="showCreateCourseDialog">
          <i class="el-icon-plus"></i> 创建课程
        </el-button>
      </div>
    </div>

    <!-- ==================== 主体内容 ==================== -->
    <div class="main-content">
      <!-- 左侧：课程列表 -->
      <div class="course-list-panel" :class="{ collapsed: selectedCourse }">
        <div class="panel-header">
          <span>我的课程 ({{ courseList.length }})</span>
        </div>
        <div class="panel-body">
          <div
            v-for="course in courseList"
            :key="course.id"
            class="course-item"
            :class="{ active: selectedCourse && selectedCourse.id === course.id }"
            @click="selectCourse(course)"
          >
            <div class="course-icon">
              <i class="el-icon-notebook-2"></i>
            </div>
            <div class="course-info">
              <div class="course-name">{{ course.courseName }}</div>
              <div class="course-desc">{{ course.description || '暂无描述' }}</div>
            </div>
            <div class="course-actions">
              <el-button
                type="text"
                size="mini"
                icon="el-icon-edit"
                @click.stop="editCourse(course)"
              ></el-button>
              <el-button
                type="text"
                size="mini"
                icon="el-icon-delete"
                style="color: #f56c6c"
                @click.stop="deleteCourse(course)"
              ></el-button>
            </div>
          </div>
          <div v-if="courseList.length === 0" class="empty-tip">
            <i class="el-icon-document"></i>
            <p>暂无课程，点击上方按钮创建</p>
          </div>
        </div>
      </div>

      <!-- 右侧：课程详情（选择课程后显示） -->
      <div v-if="selectedCourse" class="course-detail-panel">
        <!-- Tab 切换 -->
        <el-tabs v-model="activeTab" class="detail-tabs">
          <!-- ===== Tab 1: 章节目录 ===== -->
          <el-tab-pane label="章节目录" name="chapters">
            <div class="tab-toolbar">
              <span class="tab-title">{{ selectedCourse.courseName }} - 章节目录</span>
              <el-button type="primary" size="small" @click="showAddChapterDialog">
                <i class="el-icon-plus"></i> 添加章节
              </el-button>
            </div>

            <div class="chapter-list">
              <div v-for="chapter in chapterList" :key="chapter.id" class="chapter-item">
                <div class="chapter-header">
                  <div class="chapter-index">{{ chapter.sort }}</div>
                  <div class="chapter-name">{{ chapter.chapterName }}</div>
                  <div class="chapter-actions">
                    <el-button type="text" size="mini" icon="el-icon-edit" @click="editChapter(chapter)"></el-button>
                    <el-button type="text" size="mini" icon="el-icon-delete" style="color: #f56c6c" @click="deleteChapter(chapter)"></el-button>
                    <el-button type="text" size="mini" icon="el-icon-upload2" @click="showUploadResource(chapter)">上传资源</el-button>
                  </div>
                </div>
                <!-- 章节资源列表 -->
                <div v-if="chapterResources[chapter.id] && chapterResources[chapter.id].length > 0" class="chapter-resources">
                  <div v-for="res in chapterResources[chapter.id]" :key="res.id" class="resource-item">
                    <i class="el-icon-document"></i>
                    <span class="res-name">{{ res.fileName }}</span>
                    <span class="res-size">{{ formatSize(res.fileSize) }}</span>
                    <span class="res-time">{{ res.createTime }}</span>
                    <el-button type="text" size="mini" icon="el-icon-delete" style="color: #f56c6c" @click="deleteResource(res)"></el-button>
                  </div>
                </div>
              </div>
              <div v-if="chapterList.length === 0" class="empty-tip">
                <i class="el-icon-s-management"></i>
                <p>暂无章节，点击上方按钮添加</p>
              </div>
            </div>
          </el-tab-pane>

          <!-- ===== Tab 2: 课程资源总览 ===== -->
          <el-tab-pane label="课程资源" name="resources">
            <div class="tab-toolbar">
              <span class="tab-title">{{ selectedCourse.courseName }} - 课件资源</span>
              <el-upload
                :action="uploadUrl"
                :headers="uploadHeaders"
                :data="{ subjectId: selectedCourse.id }"
                :on-success="handleUploadSuccess"
                :on-error="handleUploadError"
                :show-file-list="false"
                accept=".pdf,.ppt,.pptx,.doc,.docx,.xls,.xlsx,.mp4,.avi,.zip,.rar,.jpg,.png"
              >
                <el-button size="small" type="primary">
                  <i class="el-icon-upload"></i> 上传资源
                </el-button>
              </el-upload>
            </div>

            <div class="all-resources">
              <div v-for="res in allResources" :key="res.id" class="resource-card" @click="previewResource(res)">
                <div class="resource-icon">
                  <i :class="getFileIcon(res.fileType)"></i>
                </div>
                <div class="resource-name">{{ res.fileName }}</div>
                <div class="resource-meta">
                  <span>{{ formatSize(res.fileSize) }}</span>
                  <span class="res-type">{{ getFileTypeLabel(res.fileType) }}</span>
                </div>
                <div class="resource-actions">
                  <el-button type="text" size="mini" icon="el-icon-download" @click.stop="downloadResource(res)"></el-button>
                  <el-button type="text" size="mini" icon="el-icon-delete" style="color: #f56c6c" @click.stop="deleteResource(res)"></el-button>
                </div>
              </div>
              <div v-if="allResources.length === 0" class="empty-tip">
                <i class="el-icon-folder-opened"></i>
                <p>暂无资源，点击上方按钮上传</p>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>

      <!-- 未选择课程时的提示 -->
      <div v-else class="no-course-selected">
        <i class="el-icon-notebook-1"></i>
        <p>请从左侧选择一个课程</p>
      </div>
    </div>

    <!-- ==================== 创建/编辑课程对话框 ==================== -->
    <el-dialog
      :title="courseDialog.title"
      :visible.sync="courseDialog.visible"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="courseDialog.form" :rules="courseDialog.rules" ref="courseForm" label-width="80px">
        <el-form-item label="课程名称" prop="courseName">
          <el-input v-model="courseDialog.form.courseName" placeholder="请输入课程名称" maxlength="50"></el-input>
        </el-form-item>
        <el-form-item label="课程描述" prop="description">
          <el-input
            v-model="courseDialog.form.description"
            type="textarea"
            :rows="4"
            placeholder="请输入课程描述（可选）"
            maxlength="200"
          ></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="courseDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitCourseForm" :loading="courseDialog.loading">确定</el-button>
      </span>
    </el-dialog>

    <!-- ==================== 添加/编辑章节对话框 ==================== -->
    <el-dialog
      :title="chapterDialog.title"
      :visible.sync="chapterDialog.visible"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form :model="chapterDialog.form" :rules="chapterDialog.rules" ref="chapterForm" label-width="80px">
        <el-form-item label="章节名称" prop="chapterName">
          <el-input v-model="chapterDialog.form.chapterName" placeholder="请输入章节名称" maxlength="50"></el-input>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="chapterDialog.form.sort" :min="1" :max="999"></el-input-number>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="chapterDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitChapterForm" :loading="chapterDialog.loading">确定</el-button>
      </span>
    </el-dialog>

    <!-- ==================== 上传资源到章节对话框 ==================== -->
    <el-dialog
      title="上传课件资源"
      :visible.sync="uploadDialog.visible"
      width="450px"
      :close-on-click-modal="false"
    >
      <div class="upload-info">
        <p><strong>课程：</strong>{{ selectedCourse ? selectedCourse.courseName : '' }}</p>
        <p><strong>章节：</strong>{{ uploadDialog.chapter ? uploadDialog.chapter.chapterName : '' }}</p>
      </div>
      <el-upload
        drag
        :action="uploadUrl"
        :headers="uploadHeaders"
        :data="{ subjectId: selectedCourse ? selectedCourse.id : '' }"
        :on-success="handleChapterUploadSuccess"
        :on-error="handleUploadError"
        :show-file-list="true"
        :multiple="true"
        accept=".pdf,.ppt,.pptx,.doc,.docx,.xls,.xlsx,.mp4,.avi,.zip,.rar,.jpg,.png"
        class="upload-area"
      >
        <i class="el-icon-upload"></i>
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <div class="el-upload__tip" slot="tip">支持 PDF、PPT、Word、Excel、视频、压缩包等格式</div>
      </el-upload>
    </el-dialog>
  </div>
</template>

<script>
import teacherApi from '@/api/teacher/teacherApi'

export default {
  name: 'TeacherCourseManagement',
  data() {
    return {
      // 用户信息 - 从 localStorage 读取单个字段
      userId: localStorage.getItem('userId') || '',
      userInfo: {
        id: localStorage.getItem('userId') || ''
      },

      // 课程列表
      courseList: [],

      // 选中的课程
      selectedCourse: null,

      // 当前 Tab
      activeTab: 'chapters',

      // 章节列表
      chapterList: [],

      // 章节资源映射 { chapterId: [resources] }
      chapterResources: {},

      // 所有资源
      allResources: [],

      // ===== 对话框状态 =====
      courseDialog: {
        title: '创建课程',
        visible: false,
        loading: false,
        isEdit: false,
        editId: null,
        form: { courseName: '', description: '' },
        rules: {
          courseName: [{ required: true, message: '请输入课程名称', trigger: 'blur' }]
        }
      },

      chapterDialog: {
        title: '添加章节',
        visible: false,
        loading: false,
        isEdit: false,
        editId: null,
        form: { chapterName: '', sort: 1 },
        rules: {
          chapterName: [{ required: true, message: '请输入章节名称', trigger: 'blur' }]
        }
      },

      uploadDialog: {
        visible: false,
        chapter: null
      }
    }
  },

  computed: {
    uploadUrl() {
      const base = process.env.VUE_APP_API_BASE || ''
      return base + '/study/teacher/dashboard/uploadFile'
    },
    uploadHeaders() {
      return {
        'Authorization': localStorage.getItem('token') || ''
      }
    }
  },

  mounted() {
    this.loadCourses()
  },

  methods: {
    // ============================================================
    // 课程 CRUD
    // ============================================================

    async loadCourses() {
      try {
        const resp = await teacherApi.getMyCourses(this.userInfo.id)
        var res = resp.data
        if (res.code === 200) {
          this.courseList = res.resultData || []
        }
      } catch (e) {
        this.$message.error('获取课程列表失败')
      }
    },

    selectCourse(course) {
      this.selectedCourse = course
      this.activeTab = 'chapters'
      this.loadChapters()
      this.loadAllResources()
    },

    showCreateCourseDialog() {
      this.courseDialog.title = '创建课程'
      this.courseDialog.isEdit = false
      this.courseDialog.editId = null
      this.courseDialog.form = { courseName: '', description: '' }
      this.courseDialog.visible = true
    },

    editCourse(course) {
      this.courseDialog.title = '编辑课程'
      this.courseDialog.isEdit = true
      this.courseDialog.editId = course.id
      this.courseDialog.form = {
        courseName: course.courseName,
        description: course.description
      }
      this.courseDialog.visible = true
    },

    submitCourseForm() {
      this.$refs.courseForm.validate(async (valid) => {
        if (!valid) return
        this.courseDialog.loading = true
        try {
          if (this.courseDialog.isEdit) {
            var resp = await teacherApi.updateCourse({
              id: this.courseDialog.editId,
              courseName: this.courseDialog.form.courseName,
              description: this.courseDialog.form.description
            })
            var res = resp.data
            if (res.code === 200) {
              this.$message.success('课程更新成功')
              this.courseDialog.visible = false
              this.loadCourses()
            } else {
              this.$message.error(res.resultData || '更新失败')
            }
          } else {
            var resp2 = await teacherApi.createCourse({
              courseName: this.courseDialog.form.courseName,
              description: this.courseDialog.form.description,
              userId: this.userInfo.id
            })
            var res2 = resp2.data
            if (res2.code === 200) {
              this.$message.success('课程创建成功')
              this.courseDialog.visible = false
              this.loadCourses()
            } else {
              this.$message.error(res2.resultData || '创建失败')
            }
          }
        } catch (e) {
          this.$message.error('操作失败')
        } finally {
          this.courseDialog.loading = false
        }
      })
    },

    deleteCourse(course) {
      this.$confirm('确定删除课程"' + course.courseName + '"吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          var resp = await teacherApi.deleteCourse(course.id)
          var res = resp.data
          if (res.code === 200) {
            this.$message.success('删除成功')
            if (this.selectedCourse && this.selectedCourse.id === course.id) {
              this.selectedCourse = null
              this.chapterList = []
              this.allResources = []
            }
            this.loadCourses()
          } else {
            this.$message.error(res.resultData || '删除失败')
          }
        } catch (e) {
          this.$message.error('删除失败')
        }
      }).catch(() => {})
    },

    // ============================================================
    // 章节 CRUD
    // ============================================================

    async loadChapters() {
      if (!this.selectedCourse) return
      try {
        var resp = await teacherApi.getChapters(this.selectedCourse.id)
        var res = resp.data
        if (res.code === 200) {
          this.chapterList = res.resultData || []
          // 加载每个章节的资源
          var self = this
          this.chapterList.forEach(function(ch) {
            self.loadChapterResources(ch.id)
          })
        }
      } catch (e) {
        this.$message.error('获取章节列表失败')
      }
    },

    async loadChapterResources(chapterId) {
      try {
        var resp = await teacherApi.getResources(this.selectedCourse.id)
        var res = resp.data
        if (res.code === 200) {
          this.$set(this.chapterResources, chapterId, res.resultData || [])
        }
      } catch (e) {
        // ignore
      }
    },

    showAddChapterDialog() {
      if (!this.selectedCourse) {
        this.$message.warning('请先选择一个课程')
        return
      }
      this.chapterDialog.title = '添加章节'
      this.chapterDialog.isEdit = false
      this.chapterDialog.editId = null
      this.chapterDialog.form = {
        chapterName: '',
        sort: this.chapterList.length + 1
      }
      this.chapterDialog.visible = true
    },

    editChapter(chapter) {
      this.chapterDialog.title = '编辑章节'
      this.chapterDialog.isEdit = true
      this.chapterDialog.editId = chapter.id
      this.chapterDialog.form = {
        chapterName: chapter.chapterName,
        sort: chapter.sort
      }
      this.chapterDialog.visible = true
    },

    submitChapterForm() {
      this.$refs.chapterForm.validate(async (valid) => {
        if (!valid) return
        this.chapterDialog.loading = true
        try {
          if (this.chapterDialog.isEdit) {
            var resp = await teacherApi.updateChapter({
              id: this.chapterDialog.editId,
              chapterName: this.chapterDialog.form.chapterName
            })
            var res = resp.data
            if (res.code === 200) {
              this.$message.success('章节更新成功')
              this.chapterDialog.visible = false
              this.loadChapters()
            } else {
              this.$message.error(res.resultData || '更新失败')
            }
          } else {
            var resp2 = await teacherApi.addChapter({
              classId: this.selectedCourse.id,
              chapterName: this.chapterDialog.form.chapterName
            })
            var res2 = resp2.data
            if (res2.code === 200) {
              this.$message.success('章节添加成功')
              this.chapterDialog.visible = false
              this.loadChapters()
            } else {
              this.$message.error(res2.resultData || '添加失败')
            }
          }
        } catch (e) {
          this.$message.error('操作失败')
        } finally {
          this.chapterDialog.loading = false
        }
      })
    },

    deleteChapter(chapter) {
      this.$confirm('确定删除章节"' + chapter.chapterName + '"吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          var resp = await teacherApi.deleteChapter(chapter.id)
          var res = resp.data
          if (res.code === 200) {
            this.$message.success('删除成功')
            this.loadChapters()
          } else {
            this.$message.error(res.resultData || '删除失败')
          }
        } catch (e) {
          this.$message.error('删除失败')
        }
      }).catch(() => {})
    },

    // ============================================================
    // 资源管理
    // ============================================================

    showUploadResource(chapter) {
      this.uploadDialog.chapter = chapter
      this.uploadDialog.visible = true
    },

    async loadAllResources() {
      if (!this.selectedCourse) return
      try {
        var resp = await teacherApi.getResources(this.selectedCourse.id)
        var res = resp.data
        if (res.code === 200) {
          this.allResources = res.resultData || []
        }
      } catch (e) {
        // ignore
      }
    },

    handleUploadSuccess(response) {
      if (response.code === 200) {
        this.$message.success('上传成功')
        this.loadAllResources()
        // 刷新所有章节的资源
        var self = this
        this.chapterList.forEach(function(ch) {
          self.loadChapterResources(ch.id)
        })
      } else {
        this.$message.error(response.resultData || '上传失败')
      }
    },

    handleChapterUploadSuccess(response) {
      if (response.code === 200) {
        this.$message.success('上传成功')
        this.uploadDialog.visible = false
        this.loadAllResources()
        if (this.uploadDialog.chapter) {
          this.loadChapterResources(this.uploadDialog.chapter.id)
        }
      } else {
        this.$message.error(response.resultData || '上传失败')
      }
    },

    handleUploadError() {
      this.$message.error('上传失败，请检查网络')
    },

    deleteResource(resource) {
      this.$confirm('确定删除资源"' + resource.fileName + '"吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          var resp = await teacherApi.deleteResource(resource.id)
          var res = resp.data
          if (res.code === 200) {
            this.$message.success('删除成功')
            this.loadAllResources()
            var self = this
            this.chapterList.forEach(function(ch) {
              self.loadChapterResources(ch.id)
            })
          } else {
            this.$message.error(res.resultData || '删除失败')
          }
        } catch (e) {
          this.$message.error('删除失败')
        }
      }).catch(() => {})
    },

    previewResource(resource) {
      window.open(this.getResourceUrl(resource), '_blank')
    },

    downloadResource(resource) {
      var link = document.createElement('a')
      link.href = this.getResourceUrl(resource)
      link.download = resource.fileName
      link.click()
    },

    getResourceUrl(resource) {
      var base = process.env.VUE_APP_API_BASE || ''
      return base + resource.filePath
    },

    getFileIcon(fileType) {
      if (!fileType) return 'el-icon-document'
      var type = fileType.toLowerCase()
      var icons = {
        pdf: 'el-icon-document',
        ppt: 'el-icon-picture-outline',
        pptx: 'el-icon-picture-outline',
        doc: 'el-icon-document',
        docx: 'el-icon-document',
        xls: 'el-icon-s-data',
        xlsx: 'el-icon-s-data',
        mp4: 'el-icon-video-camera',
        avi: 'el-icon-video-camera',
        zip: 'el-icon-folder',
        rar: 'el-icon-folder',
        jpg: 'el-icon-picture',
        jpeg: 'el-icon-picture',
        png: 'el-icon-picture',
        gif: 'el-icon-picture'
      }
      return icons[type] || 'el-icon-document'
    },

    getFileTypeLabel(fileType) {
      if (!fileType) return ''
      return fileType.toUpperCase()
    },

    formatSize(bytes) {
      if (!bytes) return ''
      var units = ['B', 'KB', 'MB', 'GB']
      var i = 0
      var size = bytes
      while (size >= 1024 && i < units.length - 1) {
        size = size / 1024
        i++
      }
      return size.toFixed(1) + ' ' + units[i]
    }
  }
}
</script>

<style scoped>
.course-management {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
  padding: 20px;
}

/* ===== 顶部栏 ===== */
.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  background: #fff;
  border-radius: 8px;
  padding: 16px 24px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
}

.top-bar-left h2 {
  margin: 0;
  font-size: 20px;
  color: #303133;
  font-weight: 600;
}

/* ===== 主体 ===== */
.main-content {
  flex: 1;
  display: flex;
  gap: 20px;
  min-height: 0;
}

/* ===== 左侧课程列表 ===== */
.course-list-panel {
  width: 320px;
  min-width: 280px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transition: width 0.3s;
}

.panel-header {
  padding: 14px 20px;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  border-bottom: 1px solid #ebeef5;
  background: #fafafa;
}

.panel-body {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
}

.course-item {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  cursor: pointer;
  transition: background 0.2s;
  border-left: 3px solid transparent;
}

.course-item:hover {
  background: #f0f9ff;
}

.course-item.active {
  background: #ecf5ff;
  border-left-color: #409eff;
}

.course-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 20px;
  flex-shrink: 0;
}

.course-info {
  flex: 1;
  min-width: 0;
  margin-left: 12px;
}

.course-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.course-desc {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.course-actions {
  display: none;
  flex-shrink: 0;
  margin-left: 8px;
}

.course-item:hover .course-actions {
  display: flex;
  gap: 4px;
}

/* ===== 右侧详情面板 ===== */
.course-detail-panel {
  flex: 1;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.course-detail-panel .detail-tabs {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 0 20px;
}

.detail-tabs /deep/ .el-tabs__content {
  flex: 1;
  overflow-y: auto;
}

.detail-tabs /deep/ .el-tab-pane {
  height: 100%;
}

.tab-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 8px 0;
  border-bottom: 1px solid #ebeef5;
}

.tab-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

/* ===== 未选中提示 ===== */
.no-course-selected {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #c0c4cc;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
}

.no-course-selected i {
  font-size: 80px;
  margin-bottom: 16px;
}

.no-course-selected p {
  font-size: 16px;
}

/* ===== 章节列表 ===== */
.chapter-list {
  padding: 0;
}

.chapter-item {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  margin-bottom: 12px;
  overflow: hidden;
  transition: box-shadow 0.2s;
}

.chapter-item:hover {
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}

.chapter-header {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background: #fafafa;
}

.chapter-index {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #409eff;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  flex-shrink: 0;
}

.chapter-name {
  flex: 1;
  margin-left: 12px;
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.chapter-actions {
  display: flex;
  gap: 2px;
}

/* 章节下的资源列表 */
.chapter-resources {
  border-top: 1px solid #ebeef5;
  padding: 4px 0;
}

.resource-item {
  display: flex;
  align-items: center;
  padding: 6px 16px 6px 56px;
  font-size: 13px;
  color: #606266;
  transition: background 0.2s;
}

.resource-item:hover {
  background: #f5f7fa;
}

.resource-item i {
  margin-right: 6px;
  color: #409eff;
}

.res-item-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.res-item-size,
.res-item-time {
  margin-left: 12px;
  color: #909399;
  font-size: 12px;
  flex-shrink: 0;
}

/* ===== 资源卡片视图 ===== */
.all-resources {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
  padding: 4px 0;
}

.resource-card {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 16px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.resource-card:hover {
  border-color: #409eff;
  box-shadow: 0 2px 12px rgba(64,158,255,0.15);
  transform: translateY(-2px);
}

.resource-icon {
  font-size: 48px;
  color: #409eff;
  margin-bottom: 12px;
}

.resource-name {
  font-size: 13px;
  color: #303133;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 160px;
}

.resource-meta {
  display: flex;
  gap: 8px;
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
}

.resource-meta .res-type {
  color: #409eff;
  font-weight: 500;
}

.resource-actions {
  display: none;
  gap: 8px;
}

.resource-card:hover .resource-actions {
  display: flex;
}

/* ===== 空状态 ===== */
.empty-tip {
  text-align: center;
  padding: 40px 0;
  color: #c0c4cc;
}

.empty-tip i {
  font-size: 48px;
  margin-bottom: 12px;
}

.empty-tip p {
  font-size: 14px;
  margin: 0;
}

/* ===== 上传弹窗区域 ===== */
.upload-info {
  margin-bottom: 16px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 6px;
}

.upload-info p {
  margin: 4px 0;
  font-size: 14px;
  color: #606266;
}

.upload-area {
  width: 100%;
}
</style>