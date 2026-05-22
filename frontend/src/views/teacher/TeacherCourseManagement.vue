<template>
  <div class="teacher-course-management">
    <!-- 顶部导航 -->
    <div class="management-header">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/teachercourselist' }">课程列表</el-breadcrumb-item>
        <el-breadcrumb-item>{{ courseName }}</el-breadcrumb-item>
      </el-breadcrumb>
      <div class="header-actions">
        <el-button
          type="danger"
          size="small"
          icon="el-icon-delete"
          :loading="deletingCourse"
          native-type="button"
          @click="onDeleteCourse">
          删除课程
        </el-button>
      </div>
    </div>

    <!-- Tab 导航栏 -->
    <div class="management-tabs">
      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <el-tab-pane label="课程信息" name="info">
          <div class="course-info-panel">
            <el-form ref="courseForm" :model="courseForm" label-width="100px" size="small" :rules="courseRules">
              <el-form-item label="课程封面">
                <div class="cover-wrapper">
                  <div class="cover-preview">
                    <img v-if="courseForm.coverUrl && !coverLoadFailed && coverDisplayUrl" :src="String(coverDisplayUrl)" class="cover-image" @error="onCoverError" />
                    <div v-else class="cover-fallback" :style="{ background: coverColor }">
                      <span class="cover-letter">{{ (courseForm.courseName || '课').charAt(0).toUpperCase() }}</span>
                    </div>
                    <el-button
                       v-if="courseForm.coverUrl"
                       size="mini"
                       type="danger"
                       icon="el-icon-delete"
                       class="cover-remove-btn"
                       circle
                       native-type="button"
                       @click="removeCover">
                     </el-button>
                  </div>
                  <el-button type="primary" icon="el-icon-picture" native-type="button" @click="coverDialogVisible = true">
                    选择封面
                  </el-button>
                </div>
              </el-form-item>
              <el-form-item label="课程名称" prop="courseName">
                <el-input v-model="courseForm.courseName" placeholder="请输入课程名称" maxlength="100" />
              </el-form-item>
              <el-form-item label="课程简介" prop="description">
                <el-input
                  v-model="courseForm.description"
                  type="textarea"
                  :rows="4"
                  placeholder="请输入课程简介"
                  maxlength="500"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" native-type="button" @click="saveCourseInfo" :loading="savingCourse">保存修改</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>
        <el-tab-pane label="课程大纲" name="outline">
          <div class="outline-toolbar">
            <el-select v-model="currentClassId" placeholder="选择班级" @change="onClassChange" style="width: 250px;">
              <el-option
                v-for="item in classList"
                :key="item.id"
                :label="item.className"
                :value="item.id">
                <span>{{ item.className }}</span>
                <span class="class-course-name"> - {{ item.courseName || item.course_id || '' }}</span>
              </el-option>
            </el-select>
          </div>
          <div class="management-body">
            <!-- 大纲列表 -->
            <div class="outline-panel">
              <div class="panel-header">
                <span>课程大纲</span>
                <div class="panel-actions">
                  <el-button
                    size="small"
                    type="primary"
                    icon="el-icon-plus"
                    @click="addTopLevelChapter"
                    :disabled="!currentClassId">
                    添加章节
                  </el-button>
                  <el-button
                    size="small"
                    icon="el-icon-refresh"
                    @click="loadChapters"
                    :disabled="!currentClassId">
                    刷新
                  </el-button>
                </div>
              </div>
              <div class="panel-body">
                <el-tree
                  ref="chapterTree"
                  :data="chapterTree"
                  :props="treeProps"
                  node-key="id"
                  default-expand-all
                  draggable
                  :allow-drag="() => true"
                  :allow-drop="allowDrop"
                  @node-drag-end="onDragEnd"
                  @node-click="selectChapter"
                  :expand-on-click-node="false"
                  empty-text="暂无章节，点击上方「添加章节」开始创建">
                  <span class="custom-tree-node" slot-scope="{ node, data }">
                    <span class="node-label">
                      <i class="el-icon-document" v-if="!data.parentId"></i>
                      <i class="el-icon-document-copy" v-else></i>
                      <span class="node-name" :class="{ 'is-leaf': data.parentId }">{{ data.chapterName }}</span>
                      <el-tag size="mini" type="info" v-if="data.resourceCount > 0">{{ data.resourceCount }} 资源</el-tag>
                    </span>
                    <span class="node-actions">
                      <el-button
                        type="text"
                        size="mini"
                        icon="el-icon-plus"
                        @click="addChildChapter(data)">
                      </el-button>
                      <el-button
                        type="text"
                        size="mini"
                        icon="el-icon-edit"
                        @click="editChapter(data)">
                      </el-button>
                      <el-button
                        type="text"
                        size="mini"
                        icon="el-icon-delete"
                        style="color: #F56C6C;"
                        @click="deleteChapter(data)">
                      </el-button>
                    </span>
                  </span>
                </el-tree>
              </div>
            </div>

            <!-- 章节详情 -->
            <div class="detail-panel">
              <div class="panel-header">
                <span>章节详情</span>
                <div class="panel-actions">
                  <el-button
                    size="small"
                    type="primary"
                    icon="el-icon-plus"
                    @click="showAddResourceDialog"
                    :disabled="!selectedChapter">
                    添加资源
                  </el-button>
                </div>
              </div>
              <div class="panel-body">
                <el-empty v-if="!selectedChapter" description="请从左侧选择一个章节"></el-empty>
                <div v-else class="chapter-detail">
                  <el-form label-width="80px" size="small">
                    <el-form-item label="章节名称">
                      <el-input v-model="selectedChapter.chapterName" :disabled="true"></el-input>
                    </el-form-item>
                  </el-form>

                  <div class="resource-section">
                    <div class="resource-header">
                      <span>关联资源（{{ resources.length }}）</span>
                    </div>
                    <el-table :data="resources" style="width: 100%" size="small" empty-text="暂无关联资源">
                      <el-table-column label="资源名称" prop="contentTitle" min-width="180">
                        <template slot-scope="{ row }">
                          <i :class="row.contentType === 1 ? 'el-icon-video-camera' : 'el-icon-document'"></i>
                          {{ row.contentTitle || '未命名资源' }}
                        </template>
                      </el-table-column>
                      <el-table-column label="类型" width="100">
                        <template slot-scope="{ row }">
                          <el-tag :type="row.contentType === 1 ? 'primary' : (row.contentType === 3 ? 'warning' : 'success')" size="mini">
                            {{ row.contentType === 1 ? '视频' : (row.contentType === 3 ? '本地' : '阅读') }}
                          </el-tag>
                        </template>
                      </el-table-column>
                      <el-table-column label="操作" width="80">
                        <template slot-scope="{ row }">
                          <el-button
                            type="text"
                            size="mini"
                            style="color: #F56C6C;"
                            icon="el-icon-delete"
                            @click="deleteResource(row)">
                          </el-button>
                        </template>
                      </el-table-column>
                    </el-table>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 选择封面对话框 -->
    <el-dialog title="选择课程封面" :visible.sync="coverDialogVisible" width="600px" @opened="onCoverDialogOpen" @closed="coverUrlInput = ''">
      <el-tabs v-model="coverTab">
        <el-tab-pane label="资源库" name="library">
          <div class="cover-upload-area">
            <div class="upload-tip">
              <i class="el-icon-upload el-icon--large"></i>
              <p>从本地上传图片到资源库</p>
              <span>文件将保存到 resource 目录，并以课程名称命名</span>
            </div>
            <input
              ref="coverFileInput"
              type="file"
              accept="image/*"
              style="display: none"
              @change="onCoverFileSelected"
            />
            <el-button type="primary" icon="el-icon-folder-opened" @click="$refs.coverFileInput.click()" :loading="uploadingCover" native-type="button">
              选择本地图片
            </el-button>
          </div>
        </el-tab-pane>
        <el-tab-pane label="网络链接" name="url">
          <div class="cover-url-input">
            <el-input
              v-model="coverUrlInput"
              placeholder="请输入网络图片链接，如 https://example.com/image.jpg"
              clearable
            >
            </el-input>
          </div>
        </el-tab-pane>
      </el-tabs>
      <span slot="footer">
        <el-button @click="coverDialogVisible = false" native-type="button">取消</el-button>
        <el-button type="primary" @click="confirmCover" native-type="button">确定</el-button>
      </span>
    </el-dialog>

    <!-- 添加/编辑章节对话框 -->
    <el-dialog :title="chapterDialogTitle" :visible.sync="chapterDialogVisible" width="500px">
      <el-form :model="chapterForm" label-width="80px" size="small">
        <el-form-item label="章节名称" required>
          <el-input v-model="chapterForm.chapterName" placeholder="请输入章节名称"></el-input>
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            type="textarea"
            v-model="chapterForm.description"
            placeholder="章节描述（可选）"
            :rows="3">
          </el-input>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="chapterDialogVisible = false">取消</el-button>
        <el-button type="primary" native-type="button" @click="saveChapter">确定</el-button>
      </span>
    </el-dialog>

    <!-- 添加资源对话框 -->
    <el-dialog title="添加资源" :visible.sync="resourceDialogVisible" width="600px">
      <el-form :model="resourceForm" label-width="80px" size="small">
        <el-form-item label="资源类型" required>
          <el-radio-group v-model="resourceForm.contentType">
            <el-radio :label="1">视频</el-radio>
            <el-radio :label="2">文字阅读</el-radio>
            <el-radio :label="3">本地路径（测试）</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="resourceForm.contentType === 1 ? '视频' : (resourceForm.contentType === 2 ? '阅读材料' : '本地路径')" required>
          <el-select
            v-if="resourceForm.contentType !== 3"
            v-model="resourceForm.contentData"
            filterable
            remote
            :remote-method="searchResources"
            :loading="resourceSearchLoading"
            placeholder="搜索并选择资源"
            style="width: 100%">
            <el-option
              v-for="item in resourceOptions"
              :key="item.id"
              :label="item.title || item.topic"
              :value="item.id">
              <span>{{ item.title || item.topic }}</span>
            </el-option>
          </el-select>
          <el-input
            v-else
            v-model="resourceForm.localPath"
            placeholder="点击右侧按钮从课程文件库选择"
            style="width: calc(100% - 130px); margin-right: 8px;"
            readonly>
          </el-input>
          <el-button
            v-if="resourceForm.contentType === 3"
            type="primary"
            icon="el-icon-folder-opened"
            size="small"
            @click="openLocalFilePicker">
            选择本地文件
          </el-button>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="resourceDialogVisible = false">取消</el-button>
        <el-button type="primary" native-type="button" @click="saveResource">确定</el-button>
      </span>
    </el-dialog>

    <!-- 选择本地文件（从 courses/ 资源库选择） -->
    <el-dialog
      title="选择本地文件"
      :visible.sync="localFileDialogVisible"
      width="700px"
      @opened="loadLocalFileTree">
      <div class="local-file-picker">
        <div class="picker-tip">
          <i class="el-icon-info"></i>
          <span>从项目根目录 <code>courses/</code> 资源库中选择文件（视频 / PDF / 讲义等）</span>
        </div>
        <div v-loading="loadingLocalFileTree" class="picker-body">
          <el-tree
            v-if="localFileTree.length > 0"
            :data="localFileTree"
            :props="localFileTreeProps"
            node-key="path"
            highlight-current
            default-expand-all
            @node-click="onLocalFileNodeClick">
            <span class="local-file-node" slot-scope="{ node, data }">
              <i :class="data.type === 'directory' ? 'el-icon-folder' : getFileIcon(data.name)"></i>
              <span class="node-name">{{ node.label }}</span>
              <span class="node-meta" v-if="data.type === 'file'">{{ formatFileSize(data.size) }}</span>
            </span>
          </el-tree>
          <div v-else-if="!loadingLocalFileTree" class="picker-empty">
            <i class="el-icon-folder-delete"></i>
            <p>courses/ 目录为空或不存在</p>
          </div>
        </div>
        <div v-if="selectedLocalFile" class="picker-selected">
          已选择：<span>{{ selectedLocalFile.path }}</span>
        </div>
      </div>
      <span slot="footer">
        <el-button @click="localFileDialogVisible = false">取消</el-button>
        <el-button type="primary" :disabled="!selectedLocalFile" @click="confirmLocalFile">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  getMyClasses,
  getChapterTree,
  createChapter,
  updateChapter,
  deleteChapter,
  getChapterContents,
  addContent,
  deleteContent,
  addLocalResource,
  updateCourse,
  importCoverImage,
  deleteCourse
} from '@/api/teacher/teacherApi'
import { get, post } from '@/api/request'

/**
 * 提取后端 Result 响应的数据
 * 后端返回格式：{ code: 0, data: ..., msg: "success" }
 * axios 响应格式：{ data: { code: 0, data: ..., msg: "success" } }
 */
function extractData(response) {
  if (!response) return null
  // axios 响应有 data 属性
  if (response.data) {
    const body = response.data
    // 标准 Result 格式：{ code: 0, data: ..., msg: "success" }
    if (body.data !== undefined) {
      return body.data
    }
    // 兼容旧格式：{ resultData: ..., code: 200 }
    if (body.resultData !== undefined) {
      return body.resultData
    }
    return body
  }
  return response
}

/**
 * 构建菜单树
 */
function buildMenuTree(flatList, parentId) {
  if (!Array.isArray(flatList)) return []
  const tree = []
  for (const item of flatList) {
    if (
      (parentId === null || parentId === undefined) &&
      (item.parentId === null || item.parentId === undefined || item.parentId === 0)
    ) {
      const children = buildMenuTree(flatList, item.id)
      if (children.length > 0) {
        item.children = children
      }
      tree.push(item)
    } else if (parentId !== null && parentId !== undefined && item.parentId === parentId) {
      const children = buildMenuTree(flatList, item.id)
      if (children.length > 0) {
        item.children = children
      }
      tree.push(item)
    }
  }
  return tree
}

export default {
  name: 'TeacherCourseManagement',
  data() {
    return {
      // Tab 切换
      activeTab: 'info',
      // 课程信息
      courseId: null,
      courseName: '',
      // 课程信息表单
      courseForm: {
        courseName: '',
        description: '',
        coverUrl: '',
        coverColor: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
      },
      coverUrlInput: '',
      coverDialogVisible: false,
      coverTab: 'library',
      uploadingCover: false,
      coverLoadFailed: false,
      courseRules: {
        courseName: [
          { required: true, message: '课程名称不能为空', trigger: 'blur' },
          { max: 100, message: '名称不超过100字符', trigger: 'blur' }
        ]
      },
      savingCourse: false,
      deletingCourse: false,
      // 班级列表
      classList: [],
      currentClassId: null,
      // 章节树
      chapterTree: [],
      treeProps: {
        children: 'children',
        label: 'chapterName'
      },
      // 当前选中的章节
      selectedChapter: null,
      // 资源列表
      resources: [],
      // 章节对话框
      chapterDialogVisible: false,
      chapterDialogTitle: '',
      chapterForm: {
        id: null,
        chapterName: '',
        description: '',
        parentId: null
      },
      // 资源对话框
      resourceDialogVisible: false,
      resourceForm: {
        contentType: 1,
        contentData: null,
        localPath: ''
      },
      resourceOptions: [],
      resourceSearchLoading: false,
      // 本地文件选择器（从 courses/ 资源库选择）
      localFileDialogVisible: false,
      loadingLocalFileTree: false,
      localFileTree: [],
      localFileTreeProps: {
        children: 'children',
        label: 'name',
        isLeaf: data => data.type === 'file'
      },
      selectedLocalFile: null,
      // 加载状态
      loading: false
    }
  },
  async created() {
    this.courseId = parseInt(this.$route.params.courseId)
    if (!this.courseId) {
      this.$message.error('缺少课程ID参数')
      return
    }
    await this.loadCourseInfo()
    await this.loadClassList()
  },
  computed: {
    /**
     * 获取封面完整显示 URL（计算属性，依赖 courseForm.coverUrl）
     * ⚠️ 必须始终返回字符串，避免 :src 绑定到函数引用导致出现 "function() { [native code]}" 这类异常 URL
     */
    coverDisplayUrl() {
      const raw = this.courseForm && this.courseForm.coverUrl
      if (raw == null) return ''
      const url = String(raw).trim()
      if (!url) return ''
      if (url.startsWith('http://') || url.startsWith('https://')) {
        return url
      }
      const base = (this.$store && this.$store.state && this.$store.state.baseApi) || ''
      if (url.startsWith('./')) {
        return base + '/resource/' + url.substring(2)
      }
      if (url.startsWith('/')) {
        return base + url
      }
      return base + '/resource/' + url
    },

    /**
     * 封面渐变色（计算属性，依赖 courseId）
     */
    coverColor() {
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
      for (let i = 0; i < String(this.courseId).length; i++) {
        hash = ((hash << 5) - hash) + String(this.courseId).charCodeAt(i)
      }
      return colors[Math.abs(hash) % colors.length]
    }
  },
  methods: {
    /**
     * 打开封面对话框时初始化
     */
    onCoverDialogOpen() {
      this.coverTab = 'library'
      this.coverLoadFailed = false
    },

    /**
     * 加载课程信息
     */
    async loadCourseInfo() {
      try {
        const res = await post('/study/teacher/dashboard/mySubjects', { userId: this.getUserId() })
        const data = extractData(res)
        if (Array.isArray(data)) {
          const course = data.find(c => c.id === this.courseId || c.subjectId === this.courseId)
          if (course) {
            this.courseName = course.courseName || course.subjectName || course.name || ''
            this.courseForm.courseName = this.courseName
            this.courseForm.description = course.description || ''
            this.courseForm.coverUrl = course.coverUrl || ''
            this.coverLoadFailed = false
          }
        } else if (data && data.list) {
          const course = data.list.find(c => c.id === this.courseId || c.subjectId === this.courseId)
          if (course) {
            this.courseName = course.courseName || course.subjectName || course.name || ''
            this.courseForm.courseName = this.courseName
            this.courseForm.description = course.description || ''
            this.courseForm.coverUrl = course.coverUrl || ''
            this.coverLoadFailed = false
          }
        }
      } catch (e) {
        console.error('加载课程信息失败:', e)
      }
    },

    /**
     * Tab 切换
     */
    handleTabClick(tab) {
      if (tab.name === 'outline' && !this.currentClassId) {
        this.loadClassList()
      }
    },

    /**
     * 选择本地图片 → 上传到 resource 目录，后端自动存库
     */
    async onCoverFileSelected(e) {
      const file = e.target.files[0]
      if (!file) return
      if (!file.type.startsWith('image/')) {
        this.$message.warning('请选择图片文件')
        return
      }
      this.uploadingCover = true
      try {
        const formData = new FormData()
        formData.append('file', file)
        formData.append('courseId', this.courseId)
        console.log('[封面上传] 开始上传:', { fileName: file.name, fileSize: file.size, courseId: this.courseId })
        const res = await importCoverImage(formData)
        console.log('[封面上传] 后端响应:', res)
        const body = res.data
        if (body && body.code === 200 && body.resultData) {
          const coverUrl = body.resultData.relativePath || body.resultData.coverUrl
          console.log('[封面上传] 成功, coverUrl:', coverUrl, 'fileWritten:', body.resultData.fileWritten, 'dbRows:', body.resultData.dbRows)
          this.courseForm.coverUrl = coverUrl
          this.coverLoadFailed = false
          this.coverDialogVisible = false
          this.$message.success('封面上传成功')
          this.$forceUpdate()
        } else {
          console.error('[封面上传] 失败:', body)
          this.$message.error(body?.resultData || '封面上传失败')
        }
      } catch (e) {
        console.error('[封面上传] 异常:', e)
        this.$message.error('封面上传失败: ' + (e.message || '网络错误'))
      } finally {
        this.uploadingCover = false
        e.target.value = ''
      }
    },

    /**
     * 确认选择封面（"确定"按钮）
     * - 网络链接模式：直接存储 URL 到数据库
     * - 资源库模式：不做额外操作（上传时已完成）
     */
    confirmCover() {
      if (this.coverTab === 'url') {
        const url = this.coverUrlInput.trim()
        if (!url) {
          this.$message.warning('请输入图片链接')
          return
        }
        console.log('[封面保存] 网络链接模式, URL:', url)
        this.courseForm.coverUrl = url
        this.coverDialogVisible = false
        this.saveCoverToDatabase()
      }
      // 资源库模式：图片已在 onCoverFileSelected 中上传完成，直接关闭对话框
      if (this.coverTab === 'library') {
        this.coverDialogVisible = false
      }
    },

    /**
     * 图片加载失败时的回退
     * ⚠️ 绝对不能清空 coverUrl！
     * 原因：如果清空了，用户再点"保存修改"会把空值持久化到 DB，覆盖掉正确的 cover_url
     */
    onCoverError(e) {
      const failedSrc = e && e.target && e.target.src
      console.warn('[封面显示] 图片加载失败，URL:', failedSrc, 'cover_url 保持不变:', this.courseForm.coverUrl)
      // 如果失败的 src 不是当前 coverDisplayUrl（例如残留的旧 src 或异常字符串），
      // 触发一次重渲染，让 :src 重新绑定到正确的计算属性
      if (failedSrc && this.coverDisplayUrl && failedSrc !== this.coverDisplayUrl) {
        console.warn('[封面显示] 检测到 src 与 coverDisplayUrl 不一致，尝试重新渲染')
        this.$nextTick(() => this.$forceUpdate())
      }
      this.coverLoadFailed = true
    },

    /**
     * 移除封面
     */
    removeCover() {
      this.courseForm.coverUrl = ''
      this.coverUrlInput = ''
      this.saveCoverToDatabase()
    },

    /**
     * 保存封面 URL 到数据库（自动保存，无需点击「保存修改」）
     */
    async saveCoverToDatabase() {
      this.savingCourse = true
      try {
        const res = await updateCourse({
          id: this.courseId,
          coverUrl: this.courseForm.coverUrl || ''
        })
        const body = res.data
        if (body && body.code === 200) {
          this.$message.success('封面保存成功')
        } else {
          this.$message.error(body?.resultData || '封面保存失败')
        }
      } catch (e) {
        console.error('封面保存失败:', e)
        this.$message.error('封面保存失败')
      } finally {
        this.savingCourse = false
      }
    },

    /**
     * 保存课程信息
     */
    async saveCourseInfo() {
      try {
        await this.$refs.courseForm.validate()
      } catch {
        return
      }
      this.savingCourse = true
      try {
        const res = await updateCourse({
          id: this.courseId,
          courseName: this.courseForm.courseName.trim(),
          description: this.courseForm.description || '',
          coverUrl: this.courseForm.coverUrl || ''
        })
        const body = res.data
        if (body && body.code === 200) {
          this.$message.success('课程信息保存成功')
          this.courseName = this.courseForm.courseName.trim()
        } else {
          this.$message.error(body?.resultData || '保存失败')
        }
      } catch (e) {
        console.error('保存课程信息失败:', e)
        this.$message.error('保存课程信息失败')
      } finally {
        this.savingCourse = false
      }
    },

    /**
     * 删除当前课程
     * - 二次确认后调用后端 deleteSubject 接口
     * - 删除成功后返回课程列表页
     * - 数据库已配置 ON DELETE CASCADE，会自动清理 class / chapter 等关联数据
     */
    async onDeleteCourse() {
      if (!this.courseId) {
        this.$message.warning('缺少课程ID，无法删除')
        return
      }
      try {
        await this.$confirm(
          `确定要删除课程「${this.courseName || '当前课程'}」吗？此操作将同时删除该课程下的所有班级、章节和资源，且不可恢复！`,
          '删除确认',
          {
            confirmButtonText: '确认删除',
            cancelButtonText: '取消',
            type: 'warning',
            confirmButtonClass: 'el-button--danger'
          }
        )
      } catch {
        // 用户取消
        return
      }
      this.deletingCourse = true
      try {
        const res = await deleteCourse(this.courseId)
        const body = res.data
        if (body && body.code === 200) {
          this.$message.success('课程已删除')
          this.$router.replace({ path: '/teachercourselist' })
        } else {
          this.$message.error(body?.resultData || '删除失败')
        }
      } catch (e) {
        console.error('删除课程失败:', e)
        this.$message.error('删除课程失败: ' + (e.message || '网络错误'))
      } finally {
        this.deletingCourse = false
      }
    },

    /**
     * 获取当前用户ID
     */
    getUserId() {
      const userId = this.$store?.state?.user?.id || localStorage.getItem('userId') || ''
      return userId
    },

    /**
     * 加载班级列表
     */
    async loadClassList() {
      try {
        const userId = this.getUserId()
        const res = await getMyClasses({ userId })
        const data = extractData(res)
        this.classList = Array.isArray(data) ? data : []
        // 自动选择第一个匹配该课程的班级
        if (this.classList.length > 0) {
          const matched = this.classList.filter(c => {
            const cid = c.courseId || c.course_id || c.subjectId
            return cid == this.courseId
          })
          if (matched.length > 0) {
            this.currentClassId = matched[0].id
            await this.loadChapters()
          }
        }
      } catch (e) {
        console.error('加载班级列表失败:', e)
        this.$message.error('加载班级列表失败')
      }
    },

    /**
     * 班级切换
     */
    async onClassChange() {
      this.selectedChapter = null
      this.resources = []
      await this.loadChapters()
    },

    /**
     * 加载章节树
     */
    async loadChapters() {
      if (!this.currentClassId) {
        this.chapterTree = []
        return
      }
      this.loading = true
      try {
        const res = await getChapterTree({ classId: this.currentClassId })
        const data = extractData(res)
        const flatList = Array.isArray(data) ? data : []
        this.chapterTree = buildMenuTree(flatList, null)
      } catch (e) {
        console.error('加载章节失败:', e)
        this.$message.error('加载章节失败')
        this.chapterTree = []
      } finally {
        this.loading = false
      }
    },

    /**
     * 添加顶级章节
     */
    addTopLevelChapter() {
      this.chapterForm = { id: null, chapterName: '', description: '', parentId: null }
      this.chapterDialogTitle = '添加章节'
      this.chapterDialogVisible = true
    },

    /**
     * 添加子章节
     */
    addChildChapter(data) {
      this.chapterForm = { id: null, chapterName: '', description: '', parentId: data.id }
      this.chapterDialogTitle = `添加子章节（${data.chapterName}）`
      this.chapterDialogVisible = true
    },

    /**
     * 编辑章节
     */
    editChapter(data) {
      this.chapterForm = {
        id: data.id,
        chapterName: data.chapterName,
        description: data.description || '',
        parentId: data.parentId || null
      }
      this.chapterDialogTitle = '编辑章节'
      this.chapterDialogVisible = true
    },

    /**
     * 保存章节（新增/更新）
     */
    async saveChapter() {
      if (!this.chapterForm.chapterName || !this.chapterForm.chapterName.trim()) {
        this.$message.warning('请输入章节名称')
        return
      }
      try {
        if (this.chapterForm.id) {
          // 更新
          const res = await updateChapter({
            id: this.chapterForm.id,
            chapterName: this.chapterForm.chapterName.trim(),
            description: this.chapterForm.description || ''
          })
          const data = extractData(res)
          if (data || (res.data && res.data.code === 200)) {
            this.$message.success('章节更新成功')
          } else {
            this.$message.error('章节更新失败')
          }
        } else {
          // 新增
          const params = {
            classId: this.currentClassId,
            chapterName: this.chapterForm.chapterName.trim()
          }
          if (this.chapterForm.description) {
            params.description = this.chapterForm.description
          }
          if (this.chapterForm.parentId) {
            params.parentId = this.chapterForm.parentId
          }
          const res = await createChapter(params)
          const data = extractData(res)
          if (data) {
            this.$message.success('章节创建成功')
          } else if (res.data && res.data.code === 200) {
            this.$message.success('章节创建成功')
          } else {
            this.$message.error('章节创建失败')
          }
        }
        this.chapterDialogVisible = false
        await this.loadChapters()
      } catch (e) {
        console.error('保存章节失败:', e)
        this.$message.error('保存章节失败')
      }
    },

    /**
     * 删除章节
     */
    async deleteChapter(data) {
      try {
        await this.$confirm(`确定删除章节「${data.chapterName}」吗？`, '确认删除', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        const res = await deleteChapter({ id: data.id })
        const result = extractData(res)
        if (result || (res.data && res.data.code === 200)) {
          this.$message.success('章节已删除')
        } else {
          this.$message.error('删除失败')
        }
        if (this.selectedChapter && this.selectedChapter.id === data.id) {
          this.selectedChapter = null
          this.resources = []
        }
        await this.loadChapters()
      } catch (e) {
        if (e !== 'cancel') {
          console.error('删除章节失败:', e)
          this.$message.error('删除章节失败')
        }
      }
    },

    /**
     * 拖拽排序
     */
    allowDrop(draggingNode, dropNode, type) {
      return type !== 'inner'
    },

    async onDragEnd(draggingNode, dropNode, dropType, ev) {
      if (!dropNode) return
      try {
        // 重新计算所有节点的排序
        const allNodes = this.$refs.chapterTree.getFlattenNodes
          ? this.$refs.chapterTree.getFlattenNodes()
          : this.$refs.chapterTree.root.childNodes
        const updateList = []
        const traverse = (nodes, parentId) => {
          nodes.forEach((node, index) => {
            if (node.data && node.data.id) {
              updateList.push({
                id: node.data.id,
                sortOrder: index,
                parentId: parentId
              })
            }
            if (node.childNodes && node.childNodes.length > 0) {
              traverse(node.childNodes, node.data.id)
            }
          })
        }
        traverse(this.$refs.chapterTree.root.childNodes, null)
        if (updateList.length > 0) {
          await post('/study/teacher/course/batchUpdateChapters', { chapters: updateList })
          this.$message.success('排序已更新')
          await this.loadChapters()
        }
      } catch (e) {
        console.error('排序更新失败:', e)
        this.$message.error('排序更新失败')
        await this.loadChapters()
      }
    },

    /**
     * 显示添加资源对话框
     */
    showAddResourceDialog() {
      this.resourceForm = { contentType: 1, contentData: null, localPath: '' }
      this.resourceOptions = []
      this.resourceDialogVisible = true
      // 初始搜索
      this.searchResources('')
    },

    /**
     * 打开本地文件选择器（从 courses/ 资源库中选择）
     */
    openLocalFilePicker() {
      this.selectedLocalFile = null
      this.localFileDialogVisible = true
    },

    /**
     * 加载 courses/ 目录文件树
     * 调用后端 /study/teacher/course-scanner/tree
     */
    async loadLocalFileTree() {
      if (this.localFileTree.length > 0) return
      this.loadingLocalFileTree = true
      try {
        const res = await get('/study/teacher/course-scanner/tree')
        const data = extractData(res) || res.data && res.data.resultData || []
        this.localFileTree = this.normalizeFileTree(Array.isArray(data) ? data : [])
      } catch (e) {
        console.error('加载课程文件树失败:', e)
        this.$message.error('加载课程文件树失败')
        this.localFileTree = []
      } finally {
        this.loadingLocalFileTree = false
      }
    },

    /**
     * 规范化后端返回的课程结构 → 文件树
     * 后端返回：[{ name, path, chapters: [{ name, path, resources: [{ fileName, filePath, fileSize, fileType }] }] }]
     * 转成：[{ type:'directory', name, path, children: [{ type:'directory', name, path, children: [{ type:'file', name, path, size }] }] }]
     */
    normalizeFileTree(courses) {
      const toRelative = p => {
        if (!p) return ''
        let s = String(p).replace(/\\/g, '/')
        const idx = s.toLowerCase().indexOf('/courses/')
        if (idx >= 0) s = s.substring(idx)
        else if (!s.startsWith('/courses/')) s = '/courses/' + s.replace(/^\/+/, '')
        return s
      }
      return (courses || []).map(course => ({
        type: 'directory',
        name: course.name,
        path: toRelative(course.path),
        size: 0,
        children: (course.chapters || []).map(chapter => ({
          type: 'directory',
          name: chapter.name,
          path: toRelative(chapter.path),
          size: 0,
          children: (chapter.resources || []).map(res => ({
            type: 'file',
            // 若资源在子目录下，文件名前显示子目录路径，避免同名混淆
            name: res.subDirectory ? (res.subDirectory + '/' + res.fileName) : res.fileName,
            path: toRelative(res.filePath),
            size: res.fileSize || 0
          }))
        }))
      }))
    },

    /**
     * 树节点点击：仅文件节点可选
     */
    onLocalFileNodeClick(data) {
      if (data.type === 'file') {
        this.selectedLocalFile = data
      }
    },

    /**
     * 确认选择本地文件
     */
    confirmLocalFile() {
      if (!this.selectedLocalFile) {
        this.$message.warning('请选择一个文件')
        return
      }
      // 把选中文件的相对路径填入表单（统一以 /courses/ 开头）
      let p = this.selectedLocalFile.path || ''
      p = p.replace(/\\/g, '/')
      if (!p.startsWith('/courses/') && !p.startsWith('courses/')) {
        // 兜底拼上 /courses/ 前缀
        p = '/courses/' + p.replace(/^\/+/, '')
      } else if (p.startsWith('courses/')) {
        p = '/' + p
      }
      this.resourceForm.localPath = p
      this.localFileDialogVisible = false
    },

    /**
     * 根据文件名后缀返回对应 Element 图标
     */
    getFileIcon(fileName) {
      const name = (fileName || '').toLowerCase()
      if (/\.(mp4|webm|mkv|avi|mov)$/.test(name)) return 'el-icon-video-play'
      if (/\.(pdf)$/.test(name)) return 'el-icon-document'
      if (/\.(pptx?|key)$/.test(name)) return 'el-icon-data-analysis'
      if (/\.(docx?|md|markdown|txt|html?)$/.test(name)) return 'el-icon-reading'
      if (/\.(png|jpe?g|gif|svg|webp)$/.test(name)) return 'el-icon-picture'
      if (/\.(zip|rar|7z|tar|gz)$/.test(name)) return 'el-icon-folder-checked'
      return 'el-icon-document'
    },

    /**
     * 格式化文件大小
     */
    formatFileSize(bytes) {
      if (!bytes || bytes <= 0) return ''
      const units = ['B', 'KB', 'MB', 'GB']
      let i = 0
      let n = bytes
      while (n >= 1024 && i < units.length - 1) { n /= 1024; i++ }
      return n.toFixed(n < 10 && i > 0 ? 1 : 0) + ' ' + units[i]
    },

    /**
     * 搜索资源（视频/阅读材料）
     */
    async searchResources(query) {
      this.resourceSearchLoading = true
      try {
        let res
        if (this.resourceForm.contentType === 1) {
          // 搜索视频
          res = await post('/study/videos/list', {
            page: 1,
            pageSize: 50,
            topic: query || undefined
          })
        } else {
          // 搜索知识点(阅读材料)
          res = await post('/study/knowledgepoint/list', {
            page: 1,
            pageSize: 50,
            title: query || undefined
          })
        }
        const data = extractData(res)
        if (data && data.list) {
          this.resourceOptions = data.list
        } else if (Array.isArray(data)) {
          this.resourceOptions = data
        } else {
          this.resourceOptions = []
        }
      } catch (e) {
        console.error('搜索资源失败:', e)
        this.resourceOptions = []
      } finally {
        this.resourceSearchLoading = false
      }
    },

    /**
     * 保存资源关联
     */
    async saveResource() {
      if (!this.selectedChapter) {
        this.$message.warning('请先选择章节')
        return
      }
      // 本地路径模式
      if (this.resourceForm.contentType === 3) {
        if (!this.resourceForm.localPath || !this.resourceForm.localPath.trim()) {
          this.$message.warning('请输入本地路径')
          return
        }
        try {
          const res = await addLocalResource({
            chapterId: this.selectedChapter.id,
            courseId: this.courseId,
            localPath: this.resourceForm.localPath.trim()
          })
          const data = extractData(res)
          if (data) {
            this.$message.success('本地资源添加成功')
          } else if (res.data && res.data.code === 200) {
            this.$message.success('本地资源添加成功')
          } else {
            this.$message.error('本地资源添加失败')
          }
          this.resourceDialogVisible = false
          await this.loadResources()
          await this.loadChapters()
        } catch (e) {
          console.error('添加本地资源失败:', e)
          this.$message.error('添加本地资源失败')
        }
        return
      }
      // 视频/阅读材料模式
      if (!this.resourceForm.contentData) {
        this.$message.warning('请选择资源')
        return
      }
      try {
        const res = await addContent({
          chapterId: this.selectedChapter.id,
          contentType: this.resourceForm.contentType,
          contentData: this.resourceForm.contentData
        })
        const data = extractData(res)
        if (data) {
          this.$message.success('资源添加成功')
        } else if (res.data && res.data.code === 200) {
          this.$message.success('资源添加成功')
        } else {
          this.$message.error('资源添加失败')
        }
        this.resourceDialogVisible = false
        await this.loadResources()
        await this.loadChapters()
      } catch (e) {
        console.error('添加资源失败:', e)
        this.$message.error('添加资源失败')
      }
    },

    /**
     * 删除资源关联
     */
    async deleteResource(row) {
      try {
        await this.$confirm('确定移除该资源吗？', '确认', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        const res = await deleteContent({ id: row.id })
        const data = extractData(res)
        if (data || (res.data && res.data.code === 200)) {
          this.$message.success('资源已移除')
        } else {
          this.$message.error('移除失败')
        }
        await this.loadResources()
        await this.loadChapters()
      } catch (e) {
        if (e !== 'cancel') {
          console.error('删除资源失败:', e)
        }
      }
    },

    /**
     * 加载章节资源
     */
    async loadResources() {
      if (!this.selectedChapter) {
        this.resources = []
        return
      }
      try {
        const res = await getChapterContents({ chapterId: this.selectedChapter.id })
        const data = extractData(res)
        this.resources = Array.isArray(data) ? data : []
      } catch (e) {
        console.error('加载资源失败:', e)
        this.resources = []
      }
    },

    /**
     * 选中章节
     */
    selectChapter(data) {
      this.selectedChapter = data
      this.loadResources()
    }
  },
  watch: {
    selectedChapter(val) {
      if (val) {
        this.loadResources()
      }
    }
  }
}
</script>

<style scoped>
.teacher-course-management {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

.management-header {
  background: #fff;
  padding: 16px 24px;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.management-header .header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.management-tabs {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 0 16px;
  min-height: 0;
}

.management-tabs .el-tabs {
  display: flex;
  flex-direction: column;
  flex: 1;
}

.management-tabs .el-tabs__content {
  flex: 1;
  min-height: 0;
}

.management-tabs .el-tab-pane {
  height: 100%;
  overflow-y: auto;
}

.course-info-panel {
  max-width: 680px;
  padding: 24px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  margin-top: 16px;
}

.cover-wrapper {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.cover-preview {
  width: 100%;
  max-width: 480px;
  height: 160px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  position: relative;
}

.cover-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-letter {
  font-size: 56px;
  font-weight: bold;
  color: #fff;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.cover-remove-btn {
  position: absolute;
  top: 8px;
  right: 8px;
}

.cover-upload-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 40px 20px;
}

.upload-tip {
  text-align: center;
  color: #909399;
}

.upload-tip i {
  font-size: 48px;
  color: #c0c4cc;
  margin-bottom: 12px;
}

.upload-tip p {
  font-size: 14px;
  color: #606266;
  margin: 8px 0 4px;
}

.upload-tip span {
  font-size: 12px;
  color: #c0c4cc;
}

.cover-url-input {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 20px;
}

.outline-toolbar {
  padding: 12px 0;
  background: #f5f7fa;
}

.management-body {
  flex: 1;
  display: flex;
  gap: 16px;
  min-height: 0;
}

.outline-panel {
  width: 380px;
  min-width: 320px;
  background: #fff;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.detail-panel {
  flex: 1;
  background: #fff;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid #ebeef5;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.panel-actions {
  display: flex;
  gap: 8px;
}

.panel-body {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}

.custom-tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
  padding-right: 8px;
}

.node-label {
  display: flex;
  align-items: center;
  gap: 6px;
  overflow: hidden;
}

.node-label .node-name {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 140px;
}

.node-label .node-name.is-leaf {
  padding-left: 12px;
}

.node-actions {
  display: none;
  gap: 2px;
}

.custom-tree-node:hover .node-actions {
  display: flex;
}

.class-course-name {
  font-size: 12px;
  color: #909399;
}

.chapter-detail {
  padding: 8px 0;
}

.resource-section {
  margin-top: 20px;
}

.resource-header {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}

/* 本地文件选择器 */
.local-file-picker {
  display: flex;
  flex-direction: column;
}
.picker-tip {
  background: #ecf5ff;
  color: #409EFF;
  padding: 8px 12px;
  border-radius: 4px;
  margin-bottom: 12px;
  font-size: 13px;
}
.picker-tip code {
  background: rgba(255,255,255,0.6);
  padding: 1px 6px;
  border-radius: 3px;
  margin: 0 2px;
}
.picker-body {
  min-height: 280px;
  max-height: 420px;
  overflow: auto;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 8px;
}
.picker-empty {
  text-align: center;
  color: #909399;
  padding: 60px 0;
}
.picker-empty i {
  font-size: 48px;
  display: block;
  margin-bottom: 8px;
}
.local-file-node {
  display: flex;
  align-items: center;
  flex: 1;
}
.local-file-node i {
  margin-right: 6px;
  color: #909399;
}
.local-file-node .node-name {
  flex: 1;
}
.local-file-node .node-meta {
  font-size: 12px;
  color: #c0c4cc;
  margin-left: 8px;
}
.picker-selected {
  margin-top: 12px;
  padding: 8px 12px;
  background: #f4faff;
  border-left: 3px solid #409EFF;
  font-size: 13px;
  color: #606266;
}
.picker-selected span {
  color: #409EFF;
  font-family: Consolas, Monaco, monospace;
}
</style>