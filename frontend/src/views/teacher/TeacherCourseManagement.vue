<template>
  <div class="teacher-course-management">
    <!-- 顶部导航：课程选择 -->
    <div class="management-header">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/teachercourselist' }">课程列表</el-breadcrumb-item>
        <el-breadcrumb-item>章节管理</el-breadcrumb-item>
      </el-breadcrumb>
      <div class="header-info">
        <h2>{{ courseName }}</h2>
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
    </div>

    <!-- 主体区域：大纲编辑器 -->
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
                <el-tag size="mini" :type="data.publishStatus === 1 ? 'success' : 'warning'">
                  {{ data.publishStatus === 1 ? '已发布' : '未发布' }}
                </el-tag>
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
          <!-- 未选择章节 -->
          <el-empty v-if="!selectedChapter" description="请从左侧选择一个章节"></el-empty>
          <!-- 章节详情 -->
          <div v-else class="chapter-detail">
            <el-form label-width="80px" size="small">
              <el-form-item label="章节名称">
                <el-input v-model="selectedChapter.chapterName" :disabled="true"></el-input>
              </el-form-item>
              <el-form-item label="发布状态">
                <el-switch
                  v-model="selectedChapter.publishStatus"
                  :active-value="1"
                  :inactive-value="0"
                  @change="togglePublishStatus(selectedChapter)"
                  active-text="已发布"
                  inactive-text="未发布">
                </el-switch>
              </el-form-item>
            </el-form>

            <!-- 章节资源列表 -->
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
                    <el-tag :type="row.contentType === 1 ? 'primary' : 'success'" size="mini">
                      {{ row.contentType === 1 ? '视频' : '阅读' }}
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
        <el-button type="primary" @click="saveChapter">确定</el-button>
      </span>
    </el-dialog>

    <!-- 添加资源对话框 -->
    <el-dialog title="添加资源" :visible.sync="resourceDialogVisible" width="600px">
      <el-form :model="resourceForm" label-width="80px" size="small">
        <el-form-item label="资源类型" required>
          <el-radio-group v-model="resourceForm.contentType">
            <el-radio :label="1">视频</el-radio>
            <el-radio :label="2">文字阅读</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="resourceForm.contentType === 1 ? '视频' : '阅读材料'" required>
          <el-select
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
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="resourceDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveResource">确定</el-button>
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
  deleteContent
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
      // 课程信息
      courseId: null,
      courseName: '',
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
        contentData: null
      },
      resourceOptions: [],
      resourceSearchLoading: false,
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
  methods: {
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
          }
        } else if (data && data.list) {
          const course = data.list.find(c => c.id === this.courseId || c.subjectId === this.courseId)
          if (course) {
            this.courseName = course.courseName || course.subjectName || course.name || ''
          }
        }
      } catch (e) {
        console.error('加载课程信息失败:', e)
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
     * 切换发布状态
     */
    async togglePublishStatus(chapter) {
      try {
        await updateChapter({
          id: chapter.id,
          publishStatus: chapter.publishStatus
        })
        this.$message.success(
          chapter.publishStatus === 1 ? '章节已发布' : '章节已取消发布'
        )
      } catch (e) {
        console.error('更新发布状态失败:', e)
        this.$message.error('更新发布状态失败')
      }
    },

    /**
     * 显示添加资源对话框
     */
    showAddResourceDialog() {
      this.resourceForm = { contentType: 1, contentData: null }
      this.resourceOptions = []
      this.resourceDialogVisible = true
      // 初始搜索
      this.searchResources('')
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
      if (!this.resourceForm.contentData) {
        this.$message.warning('请选择资源')
        return
      }
      if (!this.selectedChapter) {
        this.$message.warning('请先选择章节')
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
}

.management-header .header-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 12px;
}

.management-header .header-info h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.management-body {
  flex: 1;
  display: flex;
  gap: 16px;
  padding: 16px;
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
</style>