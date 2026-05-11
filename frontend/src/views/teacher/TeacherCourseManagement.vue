<template>
  <div class="outline-editor" tabindex="0" @keydown="handleGlobalKeydown">
    <!-- 顶部栏 -->
    <header class="editor-header">
      <div class="header-left">
        <el-button size="mini" icon="el-icon-back" @click="goBack" style="margin-right:8px">返回</el-button>
        <i class="el-icon-s-management" style="font-size:20px;color:#409eff"></i>
        <h2><i class="el-icon-notebook-2"></i>{{ courseName || '课程大纲编辑器' }}</h2>
        <span v-if="autoSaveStatus" class="save-status"><i class="el-icon-success"></i> 已自动保存</span>
        <span v-if="saving" class="save-status saving"><i class="el-icon-loading"></i> 保存中...</span>
      </div>
      <div class="header-actions">
        <el-tooltip content="全屏编辑 (F11)" placement="bottom">
          <el-button size="mini" :icon="isFullscreen ? 'el-icon-full-screen' : 'el-icon-c-scale-to-original'" @click="toggleFullscreen"></el-button>
        </el-tooltip>
      </div>
    </header>

    <!-- 主体 -->
    <div class="editor-body">
      <!-- ====== 左侧：大纲树 ====== -->
      <aside class="left-panel">
        <!-- 课程信息栏 -->
        <div class="course-info-bar">
          <span class="outline-title">
            <i class="el-icon-s-unfold"></i> 章节大纲
            <el-tag size="mini" type="info" style="margin-left:6px">{{ flatChapters.length }}</el-tag>
          </span>
          <div class="outline-actions">
            <el-tooltip content="展开全部" placement="top">
              <el-button size="mini" icon="el-icon-arrow-down" circle @click="expandAll"></el-button>
            </el-tooltip>
            <el-tooltip content="折叠全部" placement="top">
              <el-button size="mini" icon="el-icon-arrow-up" circle @click="collapseAll"></el-button>
            </el-tooltip>
            <el-tooltip content="新增同级章节 (Enter)" placement="top">
              <el-button size="mini" icon="el-icon-plus" type="primary" circle @click="addChapter(null)"></el-button>
            </el-tooltip>
          </div>
        </div>

        <!-- 大纲树 -->
        <div class="outline-tree">
          <div v-if="loading" class="empty-outline">
            <i class="el-icon-loading"></i>
            <p>加载中...</p>
          </div>
          <div v-else-if="chapterTree.length === 0" class="empty-outline">
            <i class="el-icon-document-add"></i>
            <p>暂无章节，点击 <el-button size="mini" type="text" @click="addChapter(null)">新增章节</el-button></p>
            <p style="font-size:12px;color:#909399;margin-top:8px">快捷键 Enter 快速添加</p>
          </div>
          <template v-else>
            <TreeNode
              v-for="node in chapterTree"
              :key="node.id"
              :node="node"
              :depth="0"
              :selectedId="selectedChapterId"
              :draggedNode="dragNode"
              @select="selectChapter"
              @drag-start="handleDragStart"
              @drag-end="handleDragEnd"
              @drop="handleTreeDrop"
              @add="addChapter"
              @copy="copyChapter"
              @delete="deleteChapter"
              @toggle="toggleExpand"
            />
          </template>
        </div>
      </aside>

      <!-- ====== 右侧：编辑面板 ====== -->
      <main class="right-panel" :class="{ placeholder: !selectedChapter }">
        <!-- 未选中占位 -->
        <template v-if="!selectedChapter">
          <i class="el-icon-document"></i>
          <h3>选择左侧章节开始编辑</h3>
          <p style="color:#909399;margin-bottom:16px">支持拖拽排序、键盘快捷键操作</p>
          <div style="display:flex;gap:12px;flex-wrap:wrap;justify-content:center">
            <kbd>Enter</kbd> <span style="color:#909399">新增同级</span>
            <kbd>Tab</kbd> <span style="color:#909399">缩进</span>
            <kbd>Shift+Tab</kbd> <span style="color:#909399">提升</span>
            <kbd>Delete</kbd> <span style="color:#909399">删除</span>
          </div>
        </template>

        <!-- 选中章节编辑面板 -->
        <template v-if="selectedChapter">
          <div class="right-panel-scroll" v-loading="rightPanelLoading">
            <!-- 编辑头部 -->
            <div class="panel-header">
              <div class="breadcrumb">
                <span @click="selectChapter(null)" style="cursor:pointer;color:#409eff">章节大纲</span>
                <i class="el-icon-arrow-right"></i>
                <span class="breadcrumb-current">{{ selectedChapter.title || '编辑章节' }}</span>
              </div>
              <div class="panel-actions">
                <el-button size="mini" icon="el-icon-view" @click="previewChapter">预览</el-button>
              </div>
            </div>

            <!-- 基础信息 -->
            <el-form :model="editForm" label-width="80px" size="small">
              <el-form-item label="章节标题">
                <el-input
                  v-model="editForm.title"
                  placeholder="输入章节标题"
                  @input="autoSave()"
                  maxlength="200"
                />
              </el-form-item>
              <el-form-item label="简介">
                <el-input
                  v-model="editForm.summary"
                  type="textarea"
                  :rows="2"
                  placeholder="输入章节简介（可选）"
                  @input="autoSave()"
                  maxlength="500"
                />
              </el-form-item>

              <!-- 类型切换 -->
              <el-form-item label="学习类型">
                <el-radio-group v-model="editForm.type" @change="onTypeChange" size="small">
                  <el-radio-button label="video">
                    <i class="el-icon-video-camera" style="color:#409eff"></i> 视频学习
                  </el-radio-button>
                  <el-radio-button label="quiz">
                    <i class="el-icon-edit-outline" style="color:#e6a23c"></i> 答题测验
                  </el-radio-button>
                  <el-radio-button label="reading">
                    <i class="el-icon-document-copy" style="color:#67c23a"></i> 阅读
                  </el-radio-button>
                </el-radio-group>
              </el-form-item>

              <!-- 视频配置 -->
              <template v-if="editForm.type === 'video'">
                <el-form-item label="视频来源">
                  <el-radio-group v-model="videoSourceType" size="small">
                    <el-radio label="link">视频链接</el-radio>
                    <el-radio label="upload">上传文件</el-radio>
                    <el-radio label="library">媒资库</el-radio>
                  </el-radio-group>
                </el-form-item>
                <el-form-item label="视频链接" v-if="videoSourceType === 'link'">
                  <el-input
                    v-model="editForm.content.videoUrl"
                    placeholder="粘贴视频链接（支持 YouTube、Bilibili 等）"
                    @input="autoSave()"
                  />
                </el-form-item>
                <el-form-item label="上传视频" v-if="videoSourceType === 'upload'">
                  <el-upload
                    :action="uploadUrl"
                    :headers="{ token: getToken() }"
                    :data="{ type: 'video', courseId: courseId }"
                    :on-success="handleVideoUploadSuccess"
                    :before-upload="beforeVideoUpload"
                    accept="video/*"
                  >
                    <el-button size="small" type="primary">点击上传</el-button>
                    <div slot="tip" class="el-upload__tip">支持 mp4, webm, ogg 格式</div>
                  </el-upload>
                </el-form-item>
                <el-form-item label="防拖拽">
                  <el-switch v-model="editForm.content.disableSeek" @change="autoSave()" />
                  <span style="margin-left:8px;color:#909399;font-size:12px">启用后学生无法拖动进度条</span>
                </el-form-item>
                <el-form-item label="允许倍速">
                  <el-switch v-model="editForm.content.allowSpeed" @change="autoSave()" />
                </el-form-item>
              </template>

              <!-- 测验配置 -->
              <template v-if="editForm.type === 'quiz'">
                <el-alert
                  v-if="!editForm.content.questionIds || editForm.content.questionIds.length === 0"
                  title="此测验章节尚未添加题目，学生将无法进入"
                  type="warning"
                  :closable="false"
                  show-icon
                  style="margin-bottom:16px"
                />
                <el-form-item label="题目">
                  <div class="quiz-question-list">
                    <el-tag
                      v-for="(qid, idx) in editForm.content.questionIds || []"
                      :key="qid"
                      closable
                      @close="removeQuestion(qid)"
                      style="margin:4px"
                    >
                      {{ getQuestionTitle(qid) || ('题目 ' + (idx + 1)) }}
                    </el-tag>
                    <el-button
                      size="small"
                      icon="el-icon-plus"
                      @click="showQuestionBank = true"
                    >从题库选择</el-button>
                    <el-button
                      size="small"
                      icon="el-icon-document-add"
                      @click="quickCreateQuestion"
                    >快速新建</el-button>
                  </div>
                </el-form-item>
                <el-form-item label="及格分数">
                  <el-input-number
                    v-model="editForm.content.passScore"
                    :min="0"
                    :max="100"
                    size="small"
                    @change="autoSave()"
                  /> 分
                </el-form-item>
                <el-form-item label="允许重试">
                  <el-switch v-model="editForm.content.allowRetry" @change="autoSave()" />
                </el-form-item>
              </template>

              <!-- 阅读配置 -->
              <template v-if="editForm.type === 'reading'">
                <el-form-item label="上传文档">
                  <el-upload
                    :action="uploadUrl"
                    :headers="{ token: getToken() }"
                    :data="{ type: 'document', courseId: courseId }"
                    :on-success="handleDocUploadSuccess"
                    accept=".pdf,.ppt,.pptx,.doc,.docx"
                  >
                    <el-button size="small" type="primary">上传 PDF/PPT</el-button>
                    <div slot="tip" class="el-upload__tip">支持 pdf, ppt, pptx, doc, docx 格式</div>
                  </el-upload>
                </el-form-item>
                <el-form-item label="上传的文件">
                  <div v-if="editForm.content.docUrl" class="uploaded-file">
                    <i class="el-icon-document"></i>
                    <span>{{ editForm.content.docName || '已上传文档' }}</span>
                    <el-button size="mini" type="text" @click="editForm.content.docUrl = ''; editForm.content.docName = ''; autoSave()">移除</el-button>
                  </div>
                  <span v-else style="color:#909399">暂无上传文件</span>
                </el-form-item>
                <el-form-item label="正文内容">
                  <div class="rich-editor-wrapper">
                    <quill-editor
                      v-if="quillReady"
                      v-model="editForm.content.htmlContent"
                      :options="quillOptions"
                      @change="autoSave()"
                    />
                    <div v-else class="editor-loading">
                      <i class="el-icon-loading"></i> 富文本编辑器加载中...
                    </div>
                  </div>
                </el-form-item>
              </template>

              <!-- 发布设置 -->
              <el-divider><i class="el-icon-setting"></i> 发布设置</el-divider>
              <el-form-item label="发布状态">
                <el-switch
                  v-model="editForm.publishStatus"
                  active-value="published"
                  inactive-value="draft"
                  active-color="#67c23a"
                  inactive-color="#909399"
                  @change="autoSave()"
                />
                <span style="margin-left:8px;font-size:12px">
                  {{ editForm.publishStatus === 'published' ? '已发布，学生可见' : '草稿，仅自己可见' }}
                </span>
              </el-form-item>
              <el-form-item label="发布时间">
                <el-date-picker
                  v-model="editForm.publishTime"
                  type="datetime"
                  placeholder="立即发布（不选）"
                  @change="autoSave()"
                />
              </el-form-item>

              <!-- 保存按钮 -->
              <el-form-item>
                <el-button type="primary" @click="saveChapter" :loading="saving">
                  <i class="el-icon-circle-check"></i> 保存
                </el-button>
                <el-button @click="previewChapter">
                  <i class="el-icon-view"></i> 预览本章节
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </template>
      </main>
    </div>

    <!-- 题库选择对话框 -->
    <el-dialog
      title="从题库选择题目"
      :visible.sync="showQuestionBank"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-input
        v-model="questionSearch"
        placeholder="搜索题目..."
        prefix-icon="el-icon-search"
        style="margin-bottom:12px"
      />
      <el-table
        :data="filteredQuestions"
        @selection-change="onQuestionSelect"
        ref="questionTable"
        height="360"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="title" label="题目内容" min-width="200" show-overflow-tooltip />
        <el-table-column label="类型" width="80">
          <template slot-scope="{ row }">
            <el-tag size="mini" :type="row.type === 'choice' ? 'primary' : 'warning'">
              {{ row.type === 'choice' ? '选择题' : '判断题' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <span slot="footer">
        <el-button @click="showQuestionBank = false">取消</el-button>
        <el-button type="primary" @click="confirmQuestionSelect">添加选中题目</el-button>
      </span>
    </el-dialog>

    <!-- 快速新建题目对话框 -->
    <el-dialog
      title="快速新建题目"
      :visible.sync="showQuickCreateQuestion"
      width="500px"
    >
      <el-form :model="quickQuestion" label-width="80px" size="small">
        <el-form-item label="题目类型">
          <el-radio-group v-model="quickQuestion.type">
            <el-radio label="choice">选择题</el-radio>
            <el-radio label="judge">判断题</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="题干">
          <el-input v-model="quickQuestion.title" type="textarea" :rows="2" placeholder="输入题目内容" />
        </el-form-item>
        <template v-if="quickQuestion.type === 'choice'">
          <el-form-item
            v-for="(opt, idx) in quickQuestion.options"
            :key="idx"
            :label="'选项 ' + String.fromCharCode(65 + idx)"
          >
            <el-input v-model="quickQuestion.options[idx]" placeholder="输入选项内容">
              <el-checkbox
                slot="prefix"
                v-model="quickQuestion.correct[idx]"
                style="line-height:36px;margin-left:4px"
              />
            </el-input>
          </el-form-item>
          <el-form-item>
            <el-button size="mini" @click="addOption">+ 添加选项</el-button>
          </el-form-item>
        </template>
        <template v-if="quickQuestion.type === 'judge'">
          <el-form-item label="正确答案">
            <el-radio-group v-model="quickQuestion.correctJudge">
              <el-radio :label="true">正确</el-radio>
              <el-radio :label="false">错误</el-radio>
            </el-radio-group>
          </el-form-item>
        </template>
      </el-form>
      <span slot="footer">
        <el-button @click="showQuickCreateQuestion = false">取消</el-button>
        <el-button type="primary" @click="submitQuickQuestion" :loading="creatingQuestion">添加</el-button>
      </span>
    </el-dialog>

    <!-- 预览对话框 -->
    <el-dialog
      title="章节预览 - 学生视角"
      :visible.sync="showPreview"
      width="700px"
      top="40px"
      :close-on-click-modal="false"
    >
      <div class="preview-content" v-loading="previewLoading">
        <h2 style="margin-bottom:16px">{{ editForm.title }}</h2>
        <p v-if="editForm.summary" style="color:#606266;margin-bottom:16px">{{ editForm.summary }}</p>
        <el-tag size="small" :type="editForm.type === 'video' ? 'primary' : editForm.type === 'quiz' ? 'warning' : 'success'">
          {{ editForm.type === 'video' ? '视频学习' : editForm.type === 'quiz' ? '答题测验' : '阅读' }}
        </el-tag>
        <el-divider />
        <template v-if="editForm.type === 'video' && editForm.content.videoUrl">
          <video
            :src="editForm.content.videoUrl"
            controls
            style="width:100%;max-height:400px"
            :controlsList="editForm.content.disableSeek ? 'nodownload noremoteplayback' : ''"
          />
        </template>
        <template v-else-if="editForm.type === 'reading'">
          <div class="preview-html" v-html="editForm.content.htmlContent"></div>
          <div v-if="editForm.content.docUrl" style="margin-top:12px">
            <i class="el-icon-document"></i> 附件：{{ editForm.content.docName || '文档' }}
          </div>
        </template>
        <template v-else-if="editForm.type === 'quiz'">
          <p>共 {{ (editForm.content.questionIds || []).length }} 道题目</p>
          <p>及格线：{{ editForm.content.passScore || 0 }} 分</p>
        </template>
        <template v-else>
          <p style="color:#909399">此章节尚未配置内容</p>
        </template>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getChaptersByCourseId, createChapter, updateChapter, deleteChapter, reorderChapters } from '@/api/teacher/teacherApi'

// 树节点递归组件
const TreeNode = {
  name: 'TreeNode',
  props: ['node', 'depth', 'selectedId', 'draggedNode'],
  template: `
    <div
      class="tree-node"
      :class="{
        selected: node.id === selectedId,
        dragging: draggedNode && draggedNode.id === node.id,
        'has-warning': node.hasWarning
      }"
      :style="{ paddingLeft: depth * 16 + 'px' }"
      draggable="true"
      @click.stop="$emit('select', node)"
      @dragstart.stop="$emit('drag-start', node)"
      @dragover.prevent=""
      @dragenter.prevent="$emit('drag-start', node)"
      @drop.stop="$emit('drop', { target: node, dragged: draggedNode })"
      @dragend.stop="$emit('drag-end')"
    >
      <div class="node-content">
        <span class="expand-btn" @click.stop="$emit('toggle', node)">
          <i v-if="node.children && node.children.length > 0" :class="node.expanded ? 'el-icon-caret-bottom' : 'el-icon-caret-right'"></i>
          <i v-else class="el-icon-minus" style="visibility:hidden"></i>
        </span>
        <i
          class="type-icon"
          :class="{
            'el-icon-video-camera': node.type === 'video',
            'el-icon-edit-outline': node.type === 'quiz',
            'el-icon-document-copy': node.type === 'reading',
            'el-icon-document': !node.type || node.type === ''
          }"
          :style="{
            color: node.type === 'video' ? '#409eff' : node.type === 'quiz' ? '#e6a23c' : node.type === 'reading' ? '#67c23a' : '#909399'
          }"
        ></i>
        <span class="node-title" :title="node.title">{{ node.title || '未命名章节' }}</span>
        <span v-if="node.hasWarning" class="warning-dot" title="此章节缺少必要配置">
          <i class="el-icon-warning" style="color:#e6a23c"></i>
        </span>
        <span class="node-actions" @click.stop>
          <el-button size="mini" type="text" @click="$emit('add', node)" icon="el-icon-plus" title="添加子章节"></el-button>
          <el-button size="mini" type="text" @click="$emit('copy', node)" icon="el-icon-copy-document" title="复制"></el-button>
          <el-button size="mini" type="text" @click="$emit('delete', node)" icon="el-icon-delete" title="删除" style="color:#f56c6c"></el-button>
        </span>
      </div>
      <template v-if="node.children && node.children.length > 0 && node.expanded !== false">
        <TreeNode
          v-for="child in node.children"
          :key="child.id"
          :node="child"
          :depth="depth + 1"
          :selectedId="selectedId"
          :draggedNode="draggedNode"
          @select="$emit('select', $event)"
          @drag-start="$emit('drag-start', $event)"
          @drag-end="$emit('drag-end', $event)"
          @drop="$emit('drop', $event)"
          @add="$emit('add', $event)"
          @copy="$emit('copy', $event)"
          @delete="$emit('delete', $event)"
          @toggle="$emit('toggle', $event)"
        />
      </template>
    </div>
  `
}

export default {
  name: 'TeacherCourseManagement',
  components: { TreeNode },
  data() {
    return {
      // 课程信息
      courseId: null,
      courseName: '',
      loading: false,

      // 大纲数据
      chapterTree: [],
      flatChapters: [],
      selectedChapter: null,
      selectedChapterId: null,
      rightPanelLoading: false,
      saving: false,
      autoSaveStatus: false,

      // 编辑表单
      editForm: this.getEmptyEditForm(),

      // 视频配置
      videoSourceType: 'link',

      // 题库
      showQuestionBank: false,
      questionSearch: '',
      mockQuestions: [],
      selectedQuestions: [],

      // 快速新建题目
      showQuickCreateQuestion: false,
      quickQuestion: this.getEmptyQuickQuestion(),
      creatingQuestion: false,

      // 预览
      showPreview: false,
      previewLoading: false,

      // 拖拽
      dragNode: null,
      autoSaveTimer: null,
      isFullscreen: false,

      // 富文本
      quillReady: false,
      quillOptions: {
        theme: 'snow',
        modules: {
          toolbar: [
            ['bold', 'italic', 'underline', 'strike'],
            [{ header: [1, 2, 3, false] }],
            [{ list: 'ordered' }, { list: 'bullet' }],
            ['blockquote', 'code-block'],
            [{ color: [] }, { background: [] }],
            ['link', 'image'],
            ['clean']
          ]
        }
      }
    }
  },
  computed: {
    uploadUrl() {
      // Use relative path pointing to backend upload endpoint
      return '/api/upload'  
    },
    filteredQuestions() {
      if (!this.questionSearch) return this.mockQuestions
      const q = this.questionSearch.toLowerCase()
      return this.mockQuestions.filter(qi => (qi.title || '').toLowerCase().includes(q))
    }
  },
  created() {
    this.courseId = this.$route.params.courseId
    this.courseName = this.$route.query.courseName || ''
    if (this.courseId) {
      this.loadChapters()
    }
    this.loadMockQuestions()
    // Try loading Quill
    this.initQuill()
  },
  methods: {
    getToken() {
      return localStorage.getItem('token') || ''
    },
    getEmptyEditForm() {
      return {
        id: null,
        title: '',
        summary: '',
        type: 'video',
        content: {
          videoUrl: '',
          disableSeek: false,
          allowSpeed: true,
          questionIds: [],
          passScore: 60,
          allowRetry: true,
          htmlContent: '',
          docUrl: '',
          docName: ''
        },
        publishStatus: 'draft',
        publishTime: null
      }
    },
    getEmptyQuickQuestion() {
      return {
        type: 'choice',
        title: '',
        options: ['', ''],
        correct: [false, false],
        correctJudge: true
      }
    },
    initQuill() {
      // Check if quill editor is available globally or via dynamic import
      if (typeof window !== 'undefined' && window.Quill) {
        this.quillReady = true
      } else {
        // Try dynamic import
        import('quill').then(() => {
          this.quillReady = true
        }).catch(() => {
          // Quill not installed, will show loading
        })
      }
    },
    goBack() {
      if (window.history.length > 1) {
        this.$router.go(-1)
      } else {
        this.$router.push('/teachercourselist')
      }
    },
    async loadChapters() {
      if (!this.courseId) return
      this.loading = true
      try {
        const resp = await getChaptersByCourseId({ courseId: this.courseId })
        const data = resp.data || resp
        this.buildTree(data.list || data || [])
        this.buildFlatList()
      } catch (e) {
        console.error('加载章节失败:', e)
        this.$message.error('加载章节失败')
      } finally {
        this.loading = false
      }
    },
    buildTree(chapters) {
      const map = {}
      const roots = []
      // Create map
      chapters.forEach(ch => {
        map[ch.id] = { ...ch, children: [], expanded: ch.expanded !== false, hasWarning: this.checkWarning(ch) }
      })
      // Build tree
      chapters.forEach(ch => {
        const node = map[ch.id]
        if (node.parentId && map[node.parentId]) {
          map[node.parentId].children.push(node)
        } else {
          roots.push(node)
        }
      })
      this.chapterTree = roots
    },
    buildFlatList() {
      const flat = []
      const traverse = (nodes) => {
        nodes.forEach(n => {
          flat.push(n)
          if (n.children && n.children.length > 0) traverse(n.children)
        })
      }
      traverse(this.chapterTree)
      this.flatChapters = flat
    },
    checkWarning(chapter) {
      if (!chapter.type) return false
      if (chapter.type === 'quiz' && (!chapter.content || !chapter.content.questionIds || chapter.content.questionIds.length === 0)) {
        return true
      }
      if (chapter.type === 'video' && (!chapter.content || !chapter.content.videoUrl)) {
        return true
      }
      return false
    },
    selectChapter(chapter) {
      this.selectedChapter = chapter
      this.selectedChapterId = chapter ? chapter.id : null
      if (chapter) {
        this.loadChapterEditForm(chapter)
      }
    },
    loadChapterEditForm(chapter) {
      this.editForm = {
        id: chapter.id,
        title: chapter.title || '',
        summary: chapter.summary || '',
        type: chapter.type || 'video',
        content: chapter.content || { ...this.getEmptyEditForm().content },
        publishStatus: chapter.publishStatus || 'draft',
        publishTime: chapter.publishTime || null
      }
      this.videoSourceType = this.editForm.content.videoUrl ? 'link' : 'upload'
    },
    async addChapter(parentNode) {
      const newChapter = {
        courseId: this.courseId,
        parentId: parentNode ? parentNode.id : null,
        title: '新章节',
        type: parentNode ? parentNode.type || 'video' : 'video',
        sort: this.getNextSort(parentNode),
        content: { ...this.getEmptyEditForm().content },
        publishStatus: 'draft'
      }
      try {
        const resp = await createChapter(newChapter)
        const saved = resp.data || resp
        this.$message.success('章节创建成功')
        await this.loadChapters()
        // Select the new chapter
        const flatNode = this.flatChapters.find(c => c.id === saved.id)
        if (flatNode) this.selectChapter(flatNode)
      } catch (e) {
        this.$message.error('创建失败')
      }
    },
    getNextSort(parentNode) {
      if (!parentNode) return this.chapterTree.length
      if (parentNode.children) return parentNode.children.length
      return 0
    },
    async saveChapter() {
      if (!this.editForm.id) return
      this.saving = true
      try {
        await updateChapter({
          id: this.editForm.id,
          title: this.editForm.title,
          summary: this.editForm.summary,
          type: this.editForm.type,
          content: JSON.stringify(this.editForm.content),
          publishStatus: this.editForm.publishStatus,
          publishTime: this.editForm.publishTime
        })
        this.$message.success('保存成功')
        this.autoSaveStatus = true
        setTimeout(() => { this.autoSaveStatus = false }, 2000)
        await this.loadChapters()
      } catch (e) {
        this.$message.error('保存失败')
      } finally {
        this.saving = false
      }
    },
    autoSave() {
      if (this.autoSaveTimer) clearTimeout(this.autoSaveTimer)
      this.autoSaveTimer = setTimeout(() => {
        this.saveChapter()
      }, 2000)
    },
    onTypeChange(type) {
      // Keep common fields, reset content per type as needed
      if (type === 'video' && !this.editForm.content.videoUrl) {
        this.editForm.content = { videoUrl: '', disableSeek: false, allowSpeed: true }
      } else if (type === 'quiz' && !this.editForm.content.questionIds) {
        this.editForm.content = { questionIds: [], passScore: 60, allowRetry: true }
      } else if (type === 'reading' && !this.editForm.content.htmlContent) {
        this.editForm.content = { htmlContent: '', docUrl: '', docName: '' }
      }
      this.autoSave()
    },
    async copyChapter(chapter) {
      this.$confirm(`复制章节《${chapter.title}》及其子章节？`, '确认复制', {
        confirmButtonText: '复制',
        cancelButtonText: '取消',
        type: 'info'
      }).then(async () => {
        try {
          await createChapter({
            courseId: this.courseId,
            parentId: chapter.parentId,
            title: chapter.title + ' (副本)',
            type: chapter.type || 'video',
            content: JSON.stringify(chapter.content || {}),
            sort: (chapter.sort || 0) + 1,
            publishStatus: 'draft'
          })
          this.$message.success('复制成功')
          await this.loadChapters()
        } catch (e) {
          this.$message.error('复制失败')
        }
      }).catch(() => {})
    },
    async deleteChapter(chapter) {
      this.$confirm(`确认删除章节《${chapter.title}》？`, '警告', {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        type: 'error'
      }).then(async () => {
        try {
          await deleteChapter({ id: chapter.id })
          this.$message.success('删除成功')
          if (this.selectedChapterId === chapter.id) {
            this.selectedChapter = null
            this.selectedChapterId = null
          }
          await this.loadChapters()
        } catch (e) {
          this.$message.error('删除失败')
        }
      }).catch(() => {})
    },
    handleDragStart(node) {
      this.dragNode = node
    },
    handleDragEnd() {
      this.dragNode = null
    },
    async handleTreeDrop({ target, dragged }) {
      if (!dragged || !target) return
      if (dragged.id === target.id) return
      // Simple reorder logic: move dragged as child of target
      try {
        await reorderChapters({
          chapterId: dragged.id,
          targetId: target.id,
          position: 'inner' // or 'before', 'after'
        })
        await this.loadChapters()
        this.autoSave()
      } catch (e) {
        console.error('Reorder failed:', e)
      }
    },
    toggleExpand(node) {
      node.expanded = !(node.expanded !== false)
      this.$forceUpdate()
    },
    expandAll() {
      const expand = (nodes) => {
        nodes.forEach(n => {
          n.expanded = true
          if (n.children) expand(n.children)
        })
      }
      expand(this.chapterTree)
      this.$forceUpdate()
    },
    collapseAll() {
      const collapse = (nodes) => {
        nodes.forEach(n => {
          n.expanded = false
          if (n.children) collapse(n.children)
        })
      }
      collapse(this.chapterTree)
      this.$forceUpdate()
    },
    handleGlobalKeydown(e) {
      // Enter: add sibling chapter
      if (e.key === 'Enter' && !e.ctrlKey && !e.metaKey) {
        e.preventDefault()
        this.addChapter(this.selectedChapter ? { id: this.selectedChapter.parentId } : null)
      }
      // Tab: indent (make child of previous sibling)
      if (e.key === 'Tab' && !e.shiftKey && this.selectedChapter) {
        e.preventDefault()
        this.indentChapter()
      }
      // Shift+Tab: outdent
      if (e.key === 'Tab' && e.shiftKey && this.selectedChapter) {
        e.preventDefault()
        this.outdentChapter()
      }
      // Delete: delete selected chapter
      if (e.key === 'Delete' && this.selectedChapter) {
        this.deleteChapter(this.selectedChapter)
      }
      // F11: toggle fullscreen
      if (e.key === 'F11') {
        e.preventDefault()
        this.toggleFullscreen()
      }
    },
    async indentChapter() {
      // Make this chapter a child of its previous sibling
      if (!this.selectedChapter) return
      const flat = this.flatChapters
      const idx = flat.findIndex(c => c.id === this.selectedChapter.id)
      if (idx <= 0) return
      const prevSibling = flat[idx - 1]
      if (!prevSibling) return
      try {
        await updateChapter({ id: this.selectedChapter.id, parentId: prevSibling.id })
        await this.loadChapters()
      } catch (e) {
        this.$message.error('缩进失败')
      }
    },
    async outdentChapter() {
      if (!this.selectedChapter || !this.selectedChapter.parentId) return
      try {
        await updateChapter({ id: this.selectedChapter.id, parentId: null })
        await this.loadChapters()
      } catch (e) {
        this.$message.error('提升失败')
      }
    },
    previewChapter() {
      this.showPreview = true
    },
    toggleFullscreen() {
      if (!document.fullscreenElement) {
        document.documentElement.requestFullscreen().then(() => {
          this.isFullscreen = true
        }).catch(() => {})
      } else {
        document.exitFullscreen().then(() => {
          this.isFullscreen = false
        }).catch(() => {})
      }
    },

    // === 题库相关 ===
    loadMockQuestions() {
      // Mock data for demonstration
      this.mockQuestions = [
        { id: 'q1', title: '云计算的定义是什么？', type: 'choice' },
        { id: 'q2', title: '以下哪个是 IaaS 服务？', type: 'choice' },
        { id: 'q3', title: '虚拟化是云计算的必要技术', type: 'judge' },
        { id: 'q4', title: 'SaaS 的全称是？', type: 'choice' },
        { id: 'q5', title: '公有云比私有云更安全', type: 'judge' }
      ]
    },
    onQuestionSelect(selection) {
      this.selectedQuestions = selection
    },
    confirmQuestionSelect() {
      const currentIds = this.editForm.content.questionIds || []
      this.selectedQuestions.forEach(q => {
        if (!currentIds.includes(q.id)) {
          currentIds.push(q.id)
        }
      })
      this.editForm.content.questionIds = currentIds
      this.showQuestionBank = false
      this.autoSave()
    },
    removeQuestion(qid) {
      const ids = this.editForm.content.questionIds || []
      this.editForm.content.questionIds = ids.filter(id => id !== qid)
      this.autoSave()
    },
    getQuestionTitle(qid) {
      const q = this.mockQuestions.find(qi => qi.id === qid)
      return q ? q.title : ''
    },
    quickCreateQuestion() {
      this.quickQuestion = this.getEmptyQuickQuestion()
      this.showQuickCreateQuestion = true
    },
    addOption() {
      this.quickQuestion.options.push('')
      this.quickQuestion.correct.push(false)
    },
    async submitQuickQuestion() {
      if (!this.quickQuestion.title) {
        this.$message.warning('请输入题目内容')
        return
      }
      this.creatingQuestion = true
      try {
        // In a real app, this would call an API to save the question
        const newId = 'q_new_' + Date.now()
        const newQuestion = {
          id: newId,
          title: this.quickQuestion.title,
          type: this.quickQuestion.type
        }
        this.mockQuestions.push(newQuestion)
        const currentIds = this.editForm.content.questionIds || []
        currentIds.push(newId)
        this.editForm.content.questionIds = currentIds
        this.showQuickCreateQuestion = false
        this.$message.success('题目创建成功')
        this.autoSave()
      } catch (e) {
        this.$message.error('创建失败')
      } finally {
        this.creatingQuestion = false
      }
    },

    // === 上传相关 ===
    handleVideoUploadSuccess(res) {
      if (res && res.data) {
        this.editForm.content.videoUrl = res.data.url || res.data
        this.autoSave()
        this.$message.success('视频上传成功')
      }
    },
    beforeVideoUpload(file) {
      const isVideo = file.type.startsWith('video/')
      if (!isVideo) {
        this.$message.error('只能上传视频文件')
      }
      return isVideo
    },
    handleDocUploadSuccess(res) {
      if (res && res.data) {
        this.editForm.content.docUrl = res.data.url || res.data
        this.editForm.content.docName = res.data.name || '文档'
        this.$message.success('文档上传成功')
      }
    }
  }
}
</script>

<style scoped>
.outline-editor {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #f5f7fa;
  outline: none;
  user-select: none;
}

/* 顶部栏 */
.editor-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 16px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
  z-index: 10;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-left h2 {
  font-size: 16px;
  margin: 0;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 6px;
}

.save-status {
  font-size: 12px;
  color: #67c23a;
  display: flex;
  align-items: center;
  gap: 4px;
}

.save-status.saving {
  color: #909399;
}

.header-actions {
  display: flex;
  gap: 8px;
}

/* 主体 */
.editor-body {
  display: flex;
  flex: 1;
  overflow: hidden;
}

/* 左侧面板 */
.left-panel {
  width: 320px;
  min-width: 280px;
  background: #fff;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.course-info-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid #f2f2f2;
}

.outline-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 4px;
}

.outline-actions {
  display: flex;
  gap: 6px;
}

.outline-tree {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
}

.empty-outline {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #c0c4cc;
  font-size: 14px;
}

.empty-outline i {
  font-size: 48px;
  margin-bottom: 12px;
}

/* 树节点 */
.tree-node {
  cursor: pointer;
  transition: background 0.15s;
  border-left: 3px solid transparent;
}

.tree-node:hover {
  background: #f5f7fa;
}

.tree-node.selected {
  background: #ecf5ff;
  border-left-color: #409eff;
}

.tree-node.dragging {
  opacity: 0.5;
}

.tree-node.has-warning .node-title::after {
  content: '';
}

.node-content {
  display: flex;
  align-items: center;
  padding: 6px 12px;
  gap: 4px;
  position: relative;
}

.expand-btn {
  width: 16px;
  text-align: center;
  flex-shrink: 0;
  color: #909399;
}

.type-icon {
  font-size: 14px;
  flex-shrink: 0;
}

.node-title {
  flex: 1;
  font-size: 13px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  min-width: 0;
}

.warning-dot {
  flex-shrink: 0;
  display: flex;
  align-items: center;
}

.node-actions {
  display: none;
  flex-shrink: 0;
  gap: 2px;
}

.tree-node:hover .node-actions {
  display: flex;
}

/* 右侧面板 */
.right-panel {
  flex: 1;
  background: #fff;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.right-panel.placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  color: #c0c4cc;
}

.right-panel.placeholder i {
  font-size: 64px;
  margin-bottom: 16px;
}

.right-panel.placeholder h3 {
  margin: 0 0 8px 0;
  color: #909399;
}

.right-panel-scroll {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f2f2f2;
}

.breadcrumb {
  font-size: 13px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 8px;
}

.breadcrumb-current {
  color: #303133;
  font-weight: 500;
}

/* 测验题目列表 */
.quiz-question-list {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  align-items: center;
}

/* 富文本编辑器 */
.rich-editor-wrapper {
  min-height: 300px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.editor-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  color: #909399;
}

/* 上传文件 */
.uploaded-file {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px;
  background: #f5f7fa;
  border-radius: 4px;
}

/* 预览 */
.preview-content {
  max-height: 60vh;
  overflow-y: auto;
}

.preview-html {
  line-height: 1.8;
  color: #303133;
}

/* 键盘快捷键样式 */
kbd {
  display: inline-block;
  padding: 2px 8px;
  font-size: 12px;
  color: #303133;
  background: #f5f7fa;
  border: 1px solid #dcdfe6;
  border-radius: 3px;
  box-shadow: 0 1px 0 #dcdfe6;
  font-family: inherit;
}
</style>