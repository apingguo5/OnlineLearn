<template>
    <div class="ask-answer-page">
        <!-- ====== 顶部标题区 ====== -->
        <div class="hero-banner">
            <div class="hero-content">
                <div class="hero-icon-wrap">
                    <i class="el-icon-chat-dot-round"></i>
                </div>
                <div class="hero-text">
                    <h1 class="hero-title">问答社区</h1>
                    <p class="hero-subtitle">有问题尽管问，老师同学都在这里帮你</p>
                </div>
            </div>
        </div>

        <!-- ====== Tab 切换区 ====== -->
        <div class="tab-nav">
            <div class="tab-item" :class="{ active: activeTab === 'square' }" @click="activeTab = 'square'">
                <i class="el-icon-s-promotion"></i>
                <span>问答广场</span>
                <el-badge v-if="squareTotal > 0" :value="squareTotal" class="tab-badge" />
            </div>
            <div class="tab-item" :class="{ active: activeTab === 'ask' }" @click="activeTab = 'ask'">
                <i class="el-icon-edit-outline"></i>
                <span>我要提问</span>
            </div>
        </div>

        <!-- ====== 问答广场 ====== -->
        <div v-show="activeTab === 'square'" class="tab-content">
            <!-- 筛选 + 统计 -->
            <div class="square-toolbar">
                <div class="toolbar-left" style="display:flex;gap:12px;align-items:center;">
                    <el-select
                        v-model="squareFilterType"
                        placeholder="筛选类型"
                        size="small"
                        @change="onSquareFilter"
                    >
                        <el-option label="全部" value="all" />
                        <el-option label="通知公告" value="notice" />
                        <el-option label="问题" value="question" />
                    </el-select>
                    <el-select
                        v-model="squareFilterClassId"
                        placeholder="全部班级"
                        size="small"
                        clearable
                        @change="onSquareFilter"
                    >
                        <el-option
                            v-for="cls in enrolledClasses"
                            :key="cls.courseName + '-' + cls.classId"
                            :label="cls.courseName + ' - ' + cls.className"
                            :value="cls.classId"
                        />
                    </el-select>
                </div>
                <div class="toolbar-right">
                    <span class="square-count-text">共 <strong>{{ filteredSquareList.length }}</strong> 条</span>
                </div>
            </div>

            <!-- 空状态 -->
            <div v-if="filteredSquareList.length === 0" class="empty-state">
                <div class="empty-icon">
                    <i class="el-icon-chat-square"></i>
                </div>
                <p class="empty-title">暂无内容</p>
                <p class="empty-desc">切换筛选条件或去「我要提问」发布你的第一个问题吧</p>
                <el-button type="primary" size="small" @click="activeTab = 'ask'">去提问</el-button>
            </div>

            <!-- 混合卡片列表 -->
            <div v-else class="square-list">
                <!-- ====== 通知卡片 ====== -->
                <div v-for="item in filteredSquareList" :key="item._key">
                    <div
                        v-if="item._type === 'notice'"
                        class="notice-card"
                        :class="{ 'notice-pinned': item.isPinned == 1 }"
                    >
                        <div class="notice-card-left">
                            <div class="notice-dot" :class="item.isPinned == 1 ? 'dot-pinned' : 'dot-normal'"></div>
                        </div>
                        <div class="notice-card-body">
                            <div class="notice-card-head">
                                <span class="notice-card-title">
                                    <i v-if="item.isPinned == 1" class="el-icon-top" style="color:#F56C6C;margin-right:4px;"></i>
                                    {{ item.title }}
                                </span>
                                <el-tag size="mini" effect="plain" type="info">
                                    {{ item.courseName ? item.courseName + ' - ' : '' }}{{ item.className }}
                                </el-tag>
                            </div>
                            <p class="notice-card-content">{{ item.content }}</p>
                            <div class="notice-card-foot">
                                <span class="foot-sender">
                                    <el-avatar :size="20" style="vertical-align:middle;margin-right:4px;">{{ (item.senderName || '教')[0] }}</el-avatar>
                                    {{ item.senderName }}
                                </span>
                                <el-tag size="mini" type="warning" effect="plain">通知</el-tag>
                                <span class="foot-time">{{ item.createTime }}</span>
                            </div>
                        </div>
                    </div>

                    <!-- ====== 问题卡片 ====== -->
                    <div
                        v-else
                        class="qa-card"
                        :class="{ 'qa-answered': item.restore && item.restore !== 'undefined' }"
                    >
                        <div class="qa-card-head">
                            <div class="qa-head-left">
                                <span class="qa-topic-tag">
                                    <i class="el-icon-collection-tag"></i>
                                    {{ item.topic || '通用问题' }}
                                </span>
                                <span class="qa-sender-name">{{ item.senderName }}</span>
                            </div>
                            <div class="qa-head-right">
                                <el-tag v-if="item.restore && item.restore !== 'undefined'" type="success" size="small" effect="plain">已回复</el-tag>
                                <el-tag v-else type="warning" size="small" effect="plain">待回复</el-tag>
                                <el-tag size="mini" type="primary" effect="plain">问题</el-tag>
                                <span class="qa-time">{{ item.createTime }}</span>
                            </div>
                        </div>

                        <div class="qa-card-question">
                            <div class="qa-label">📝 问题描述</div>
                            <div class="qa-text">
                                <p>{{ item.content }}</p>
                            </div>
                        </div>

                        <div class="qa-card-reply">
                            <div class="qa-label">💬 老师回复</div>
                            <div v-if="item.restore && item.restore !== 'undefined'" class="qa-reply-body">
                                <el-avatar :size="28" style="margin-right:8px;">师</el-avatar>
                                <div class="qa-reply-box">
                                    <p class="qa-reply-text">{{ item.restore }}</p>
                                    <span class="qa-reply-teacher">— {{ item.recipientName || '老师' }}</span>
                                </div>
                            </div>
                            <p v-else class="qa-reply-waiting">
                                <i class="el-icon-time"></i> 老师正在赶来回复的路上...
                            </p>
                        </div>

                        <div v-if="item.className" class="qa-card-class">
                            <i class="el-icon-school"></i>
                            {{ item.courseName ? item.courseName + ' - ' : '' }}{{ item.className }}
                        </div>

                        <div class="qa-card-actions">
                            <el-button
                                v-if="canEditOrDelete(item)"
                                type="text"
                                size="small"
                                icon="el-icon-edit"
                                @click="startEdit(item)"
                            >修改</el-button>
                            <el-button
                                v-if="canEditOrDelete(item)"
                                type="text"
                                size="small"
                                icon="el-icon-delete"
                                style="color:#F56C6C;"
                                @click="confirmDelete(item.id)"
                            >删除</el-button>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 修改对话框 -->
            <div v-if="editingId" class="qa-edit-inline" style="margin-top:12px;">
                <el-card shadow="never">
                    <el-input type="textarea" v-model="editContent" :rows="3" placeholder="修改你的问题..." />
                    <div style="margin-top:8px;display:flex;gap:8px;">
                        <el-button type="primary" size="small" @click="confirmEdit(editingId)">保存</el-button>
                        <el-button size="small" @click="cancelEdit">取消</el-button>
                    </div>
                </el-card>
            </div>
        </div>

        <!-- ====== 我要提问 ====== -->
        <div v-show="activeTab === 'ask'" class="tab-content">
            <el-card shadow="never" class="ask-card">
                <div slot="header" class="ask-card-header">
                    <i class="el-icon-edit-outline"></i>
                    <span>写下你的问题</span>
                </div>
                <el-form :model="askForm" label-position="top">
                    <el-form-item label="问题描述">
                        <el-input
                            type="textarea"
                            v-model="askForm.content"
                            placeholder="请详细描述你遇到的问题，描述越清楚，收到的回答越准确..."
                            :rows="5"
                            maxlength="500"
                            show-word-limit
                        />
                    </el-form-item>
                    <el-form-item label="选择班级（可选）">
                        <el-select
                            v-model="askForm.classId"
                            placeholder="不选择则全员可见"
                            size="small"
                            clearable
                            style="width:100%;"
                        >
                            <el-option
                                v-for="cls in enrolledClasses"
                                :key="'ask-' + cls.classId"
                                :label="cls.courseName + ' - ' + cls.className"
                                :value="cls.classId"
                            />
                        </el-select>
                        <span style="font-size:12px;color:#909399;margin-top:4px;display:block;">
                            <i class="el-icon-info"></i> 选择班级后仅该班学生可见，不选择则所有人可见
                        </span>
                    </el-form-item>
                    <el-form-item>
                        <el-button
                            type="primary"
                            size="medium"
                            icon="el-icon-send"
                            :loading="submitting"
                            @click="submitQuestion"
                        >
                            发布问题
                        </el-button>
                        <span class="ask-hint">发布后老师会收到通知并及时回复</span>
                    </el-form-item>
                </el-form>
            </el-card>
        </div>
    </div>
</template>

<script>
import { askandanswer, deleteQuestion, updateQuestion, saveQuestion, getStudentNotices, getEnrolledClasses } from '../../../api/studentweb/askandanswer.js'
import Cookies from 'js-cookie'

export default {
    name: 'AskAndAnswer',
    data() {
        return {
            userId: null,
            activeTab: 'square',
            submitting: false,
            // 班级
            enrolledClasses: [],
            // 通知
            noticeList: [],
            // 问题
            NotHomeWork: [],
            questionTotal: 0,
            // 问答广场筛选
            squareFilterType: 'all',
            squareFilterClassId: null,
            // 提问
            askForm: { content: '', sender: null, recipient: 2, videoId: 1, status: 2, classId: null },
            editingId: null,
            editContent: ''
        }
    },
    computed: {
        squareList() {
            const notices = this.noticeList.map(n => ({
                ...n,
                _type: 'notice',
                _key: 'notice-' + n.id
            }))
            const questions = this.NotHomeWork.map(q => ({
                ...q,
                _type: 'question',
                _key: 'qa-' + q.id
            }))
            const merged = [...notices, ...questions]
            merged.sort((a, b) => {
                const ta = a.createTime || ''
                const tb = b.createTime || ''
                return tb.localeCompare(ta)
            })
            return merged
        },
        filteredSquareList() {
            return this.squareList.filter(item => {
                if (this.squareFilterType && this.squareFilterType !== 'all') {
                    if (item._type !== this.squareFilterType) return false
                }
                if (this.squareFilterClassId) {
                    const cid = item._type === 'notice'
                        ? item.classId
                        : (item.classId || null)
                    if (cid != this.squareFilterClassId) return false
                }
                return true
            })
        },
        squareTotal() {
            return this.noticeList.length + this.NotHomeWork.length
        }
    },
    created() {
        this.userId = Number(Cookies.get('userId'))
        this.askForm.sender = this.userId
        this.initData()
    },
    methods: {
        initData() {
            this.loadEnrolledClasses()
            this.loadNotices()
            this.loadQuestions()
        },
        async loadEnrolledClasses() {
            try {
                const res = await getEnrolledClasses(this.userId)
                if (res.data.code === 200) {
                    const raw = res.data.resultData || []
                    const seen = {}
                    this.enrolledClasses = raw.filter(item => {
                        const key = item.className + '-' + item.classId
                        if (seen[key]) return false
                        seen[key] = true
                        return true
                    })
                }
            } catch (e) { this.enrolledClasses = [] }
        },
        onSquareFilter() {},
        async loadNotices() {
            try {
                const params = { userId: this.userId, page: 1, pageSize: 100 }
                if (this.squareFilterClassId) {
                    params.classId = this.squareFilterClassId
                }
                const res = await getStudentNotices(params)
                if (res.data.code === 200) {
                    this.noticeList = (res.data.resultData && res.data.resultData.data) || []
                }
            } catch (e) { this.noticeList = [] }
        },
        async loadQuestions() {
            try {
                const params = { page: 1, pageSize: 100, userId: this.userId, roleId: 3 }
                const res = await askandanswer(params)
                if (res.data.code === 200) {
                    const data = res.data.resultData
                    this.NotHomeWork = (data && (data.data || data.records)) || []
                    this.questionTotal = (data && data.total) || 0
                }
            } catch (e) { this.NotHomeWork = [] }
        },

        submitQuestion() {
            if (!this.askForm.content.trim()) { this.$message.error('问题内容不能为空'); return }
            this.askForm.sender = this.userId
            this.askForm.classId = this.askForm.classId || null
            this.submitting = true
            saveQuestion(this.askForm).then(res => {
                if (res.data.code === 200) {
                    this.$message.success('提问成功！老师会尽快回复你')
                    this.askForm.content = ''
                    this.askForm.classId = null
                    this.loadQuestions()
                    this.activeTab = 'square'
                } else {
                    this.$message.error('提问失败')
                }
            }).catch(() => this.$message.error('网络错误')).finally(() => { this.submitting = false })
        },

        canEditOrDelete(q) {
            const s = q.status
            return s === '未回复' || s === '2' || !s || q.restore === undefined || q.restore === null || q.restore === 'undefined'
        },
        startEdit(q) { this.editingId = q.id; this.editContent = q.content },
        confirmEdit(id) {
            if (!this.editContent.trim()) { this.$message.error('问题内容不能为空'); return }
            updateQuestion({ id, content: this.editContent }).then(res => {
                if (res.data.code === 200) {
                    this.$message.success('修改成功')
                    this.editingId = null; this.editContent = ''
                    this.loadQuestions()
                } else { this.$message.error('修改失败') }
            }).catch(() => this.$message.error('网络错误'))
        },
        cancelEdit() { this.editingId = null; this.editContent = '' },
        confirmDelete(id) {
            this.$confirm('确定要删除这个问题吗？', '提示', { type: 'warning' }).then(() => {
                deleteQuestion({ id }).then(res => {
                    if (res.data.code === 200) { this.$message.success('已删除'); this.loadQuestions() }
                    else { this.$message.error('删除失败') }
                })
            }).catch(() => {})
        }
    }
}
</script>

<style scoped>
.ask-answer-page {
    max-width: 900px;
    margin: 0 auto;
    padding: 0 0 40px;
}

/* ====== 顶部 Banner ====== */
.hero-banner {
    background: linear-gradient(135deg, #4e6ef2 0%, #7b8cf8 50%, #a8b4ff 100%);
    border-radius: 12px;
    padding: 32px 36px;
    margin-bottom: 24px;
    box-shadow: 0 4px 20px rgba(78, 110, 242, 0.25);
}
.hero-content {
    display: flex;
    align-items: center;
    gap: 20px;
}
.hero-icon-wrap {
    width: 56px;
    height: 56px;
    border-radius: 14px;
    background: rgba(255,255,255,0.2);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28px;
    color: #fff;
}
.hero-text { flex: 1; }
.hero-title {
    color: #fff;
    font-size: 26px;
    font-weight: 700;
    margin: 0 0 6px;
    letter-spacing: 1px;
}
.hero-subtitle {
    color: rgba(255,255,255,0.85);
    font-size: 14px;
    margin: 0;
}

/* ====== Tab 导航 ====== */
.tab-nav {
    display: flex;
    background: #fff;
    border-radius: 10px;
    padding: 4px;
    margin-bottom: 20px;
    box-shadow: 0 1px 6px rgba(0,0,0,0.06);
    gap: 4px;
}
.tab-item {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
    padding: 12px 0;
    border-radius: 8px;
    font-size: 14px;
    color: #606266;
    cursor: pointer;
    transition: all 0.25s;
    user-select: none;
}
.tab-item:hover { background: #f0f5ff; color: #4e6ef2; }
.tab-item.active {
    background: #4e6ef2;
    color: #fff;
    font-weight: 600;
    box-shadow: 0 2px 8px rgba(78,110,242,0.3);
}
.tab-badge { margin-left: 2px; }
.tab-badge >>> .el-badge__content {
    border: 2px solid #fff;
}

/* ====== 内容区 ====== */
.tab-content { animation: fadeIn 0.25s ease; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(8px); } to { opacity: 1; transform: translateY(0); } }

/* ====== 问答广场筛选栏 ====== */
.square-toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    flex-wrap: wrap;
    gap: 8px;
}
.square-count-text { font-size: 13px; color: #909399; }
.square-count-text strong { color: #303133; }

/* ====== 广场卡片列表 ====== */
.square-list { display: flex; flex-direction: column; gap: 12px; }

/* ====== 通知卡片 ====== */
.notice-card {
    display: flex;
    gap: 14px;
    background: #fff;
    border-radius: 10px;
    padding: 18px 20px;
    box-shadow: 0 1px 4px rgba(0,0,0,0.05);
    transition: all 0.25s;
    border: 1px solid #ebeef5;
}
.notice-card:hover {
    box-shadow: 0 4px 16px rgba(0,0,0,0.08);
    transform: translateY(-1px);
}
.notice-card.notice-pinned {
    border-color: #ffe0e0;
    background: #fffbfb;
}
.notice-card-left {
    display: flex;
    align-items: flex-start;
    padding-top: 6px;
}
.notice-dot {
    width: 10px;
    height: 10px;
    border-radius: 50%;
    flex-shrink: 0;
}
.dot-normal { background: #409EFF; }
.dot-pinned { background: #F56C6C; }
.notice-card-body { flex: 1; min-width: 0; }
.notice-card-head {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
    gap: 10px;
}
.notice-card-title {
    font-size: 15px;
    font-weight: 600;
    color: #303133;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}
.notice-card-content {
    margin: 0 0 10px;
    font-size: 14px;
    color: #606266;
    line-height: 1.7;
    display: -webkit-box;
    -webkit-line-clamp: 3;
    -webkit-box-orient: vertical;
    overflow: hidden;
}
.notice-card-foot {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 12px;
    color: #909399;
}
.foot-sender { display: flex; align-items: center; }
.foot-time { color: #C0C4CC; }

/* ====== 提问卡片 ====== */
.ask-card { border-radius: 10px; overflow: hidden; }
.ask-card-header {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 600;
    color: #303133;
}
.ask-card-header i { font-size: 20px; color: #409EFF; }
.ask-hint {
    margin-left: 16px;
    font-size: 12px;
    color: #909399;
    vertical-align: middle;
}

/* ====== 空状态 ====== */
.empty-state {
    text-align: center;
    padding: 60px 20px;
    background: #fff;
    border-radius: 10px;
    box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}
.empty-icon {
    width: 72px;
    height: 72px;
    margin: 0 auto 16px;
    border-radius: 50%;
    background: #f0f2f5;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 32px;
    color: #C0C4CC;
}
.empty-title { font-size: 16px; color: #909399; margin: 0 0 6px; }
.empty-desc { font-size: 13px; color: #C0C4CC; margin: 0 0 16px; }

/* ====== QA 卡片 ====== */
.qa-card {
    background: #fff;
    border-radius: 10px;
    padding: 20px 24px;
    box-shadow: 0 1px 4px rgba(0,0,0,0.05);
    border: 1px solid #ebeef5;
    border-left: 4px solid #E6A23C;
    transition: all 0.25s;
}
.qa-card:hover { box-shadow: 0 4px 16px rgba(0,0,0,0.08); }
.qa-card.qa-answered { border-left-color: #67C23A; }

.qa-card-head {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 14px;
    flex-wrap: wrap;
    gap: 8px;
}
.qa-head-left { display: flex; align-items: center; gap: 10px; }
.qa-topic-tag {
    font-size: 12px;
    color: #909399;
    background: #f0f2f5;
    padding: 3px 10px;
    border-radius: 4px;
}
.qa-sender-name { font-size: 14px; font-weight: 600; color: #303133; }
.qa-head-right { display: flex; align-items: center; gap: 10px; }
.qa-time { font-size: 12px; color: #C0C4CC; }

.qa-label {
    font-size: 13px;
    font-weight: 600;
    color: #909399;
    margin-bottom: 8px;
}

.qa-card-question {
    margin-bottom: 16px;
    padding-bottom: 16px;
    border-bottom: 1px dashed #ebeef5;
}
.qa-text p {
    margin: 0;
    font-size: 15px;
    color: #303133;
    line-height: 1.7;
}

.qa-card-reply { margin-bottom: 8px; }
.qa-reply-body {
    display: flex;
    align-items: flex-start;
    background: #f0fdf4;
    border-radius: 8px;
    padding: 12px 14px;
}
.qa-reply-box { flex: 1; }
.qa-reply-text {
    margin: 0 0 4px;
    font-size: 14px;
    color: #303133;
    line-height: 1.6;
}
.qa-reply-teacher { font-size: 12px; color: #909399; }
.qa-reply-waiting {
    font-size: 13px;
    color: #909399;
    padding: 10px 14px;
    background: #fafafa;
    border-radius: 8px;
    margin: 0;
}
.qa-reply-waiting i { margin-right: 4px; }

.qa-card-class {
    font-size: 12px;
    color: #909399;
    margin-bottom: 4px;
    display: flex;
    align-items: center;
    gap: 4px;
}

.qa-card-actions {
    display: flex;
    gap: 4px;
    padding-top: 4px;
}

.qa-edit-inline { margin-top: 12px; }
</style>
