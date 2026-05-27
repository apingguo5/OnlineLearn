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
            <div class="tab-item" :class="{ active: activeTab === 'notices' }" @click="activeTab = 'notices'">
                <i class="el-icon-bell"></i>
                <span>通知公告</span>
                <el-badge v-if="noticeList.length > 0" :value="noticeList.length" class="tab-badge" />
            </div>
            <div class="tab-item" :class="{ active: activeTab === 'ask' }" @click="activeTab = 'ask'">
                <i class="el-icon-edit-outline"></i>
                <span>我要提问</span>
            </div>
            <div class="tab-item" :class="{ active: activeTab === 'my' }" @click="activeTab = 'my'">
                <i class="el-icon-question"></i>
                <span>我的提问</span>
                <el-badge v-if="questionTotal > 0" :value="questionTotal" class="tab-badge" />
            </div>
        </div>

        <!-- ====== 通知公告 ====== -->
        <div v-show="activeTab === 'notices'" class="tab-content">
            <!-- 筛选 + 统计 -->
            <div class="notice-toolbar">
                <div class="toolbar-left">
                    <el-select
                        v-model="noticeFilterClassId"
                        placeholder="全部班级"
                        size="small"
                        clearable
                        @change="onNoticeFilterChange"
                        class="notice-filter-select"
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
                    <span class="notice-count-text">共 <strong>{{ noticeList.length }}</strong> 条通知</span>
                </div>
            </div>

            <!-- 空状态 -->
            <div v-if="noticeList.length === 0" class="empty-state">
                <div class="empty-icon">
                    <i class="el-icon-reading"></i>
                </div>
                <p class="empty-title">暂无通知公告</p>
                <p class="empty-desc">当老师发布新通知时，会在这里显示</p>
            </div>

            <!-- 通知卡片列表 -->
            <div v-else class="notice-card-list">
                <div
                    v-for="n in noticeList"
                    :key="'notice-' + n.id"
                    class="notice-card"
                    :class="{ 'notice-pinned': n.isPinned == 1 }"
                >
                    <div class="notice-card-left">
                        <div class="notice-dot" :class="n.isPinned == 1 ? 'dot-pinned' : 'dot-normal'"></div>
                    </div>
                    <div class="notice-card-body">
                        <div class="notice-card-head">
                            <span class="notice-card-title">
                                <i v-if="n.isPinned == 1" class="el-icon-top" style="color:#F56C6C;margin-right:4px;"></i>
                                {{ n.title }}
                            </span>
                            <el-tag size="mini" effect="plain" type="info">{{ n.courseName ? n.courseName + ' - ' : '' }}{{ n.className }}</el-tag>
                        </div>
                        <p class="notice-card-content">{{ n.content }}</p>
                        <div class="notice-card-foot">
                            <span class="foot-sender">
                                <el-avatar :size="20" style="vertical-align:middle;margin-right:4px;">{{ (n.senderName || '教')[0] }}</el-avatar>
                                {{ n.senderName }}
                            </span>
                            <span class="foot-time">{{ n.createTime }}</span>
                        </div>
                    </div>
                </div>
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

        <!-- ====== 我的提问 ====== -->
        <div v-show="activeTab === 'my'" class="tab-content">
            <!-- 空状态 -->
            <div v-if="NotHomeWork.length === 0" class="empty-state">
                <div class="empty-icon">
                    <i class="el-icon-chat-square"></i>
                </div>
                <p class="empty-title">还没有提问记录</p>
                <p class="empty-desc">去「我要提问」发布你的第一个问题吧</p>
                <el-button type="primary" size="small" @click="activeTab = 'ask'">去提问</el-button>
            </div>

            <!-- 提问列表 -->
            <div v-else class="qa-list">
                <div v-for="t in NotHomeWork" :key="'qa-' + t.id" class="qa-card" :class="{ 'qa-answered': t.restore && t.restore !== 'undefined' }">
                    <!-- 头部：来源 + 时间 + 状态 -->
                    <div class="qa-card-head">
                        <div class="qa-head-left">
                            <span class="qa-topic-tag">
                                <i class="el-icon-collection-tag"></i>
                                {{ t.topic || '通用问题' }}
                            </span>
                            <span class="qa-sender-name">{{ t.senderName }}</span>
                        </div>
                        <div class="qa-head-right">
                            <el-tag v-if="t.restore && t.restore !== 'undefined'" type="success" size="small" effect="plain">已回复</el-tag>
                            <el-tag v-else type="warning" size="small" effect="plain">待回复</el-tag>
                            <span class="qa-time">{{ t.createTime }}</span>
                        </div>
                    </div>

                    <!-- 问题内容 -->
                    <div class="qa-card-question">
                        <div class="qa-label">📝 问题描述</div>
                        <div v-if="editingId !== t.id" class="qa-text">
                            <p>{{ t.content }}</p>
                        </div>
                        <div v-else class="qa-edit">
                            <el-input type="textarea" v-model="editContent" :rows="3" placeholder="修改你的问题..." />
                            <div class="qa-edit-btns">
                                <el-button type="primary" size="small" @click="confirmEdit(t.id)">保存</el-button>
                                <el-button size="small" @click="cancelEdit">取消</el-button>
                            </div>
                        </div>
                    </div>

                    <!-- 回复内容 -->
                    <div class="qa-card-reply">
                        <div class="qa-label">💬 老师回复</div>
                        <div v-if="t.restore && t.restore !== 'undefined'" class="qa-reply-body">
                            <el-avatar :size="28" style="margin-right:8px;">师</el-avatar>
                            <div class="qa-reply-box">
                                <p class="qa-reply-text">{{ t.restore }}</p>
                                <span class="qa-reply-teacher">— {{ t.recipientName || '老师' }}</span>
                            </div>
                        </div>
                        <p v-else class="qa-reply-waiting">
                            <i class="el-icon-time"></i> 老师正在赶来回复的路上...
                        </p>
                    </div>

                    <!-- 操作 -->
                    <div class="qa-card-actions">
                        <el-button
                            v-if="canEditOrDelete(t)"
                            type="text"
                            size="small"
                            icon="el-icon-edit"
                            @click="startEdit(t)"
                        >修改</el-button>
                        <el-button
                            v-if="canEditOrDelete(t)"
                            type="text"
                            size="small"
                            icon="el-icon-delete"
                            style="color:#F56C6C;"
                            @click="confirmDelete(t.id)"
                        >删除</el-button>
                    </div>
                </div>
            </div>

            <!-- 分页 -->
            <el-pagination
                v-if="questionTotal > 0"
                class="qa-pagination"
                @size-change="handleQaSizeChange"
                @current-change="handleQaPageChange"
                :current-page="qaPage.page"
                :page-sizes="[10, 20, 30, 40]"
                :page-size="qaPage.pageSize"
                layout="total, sizes, prev, pager, next, jumper"
                :total="questionTotal"
            />
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
            activeTab: 'notices',
            submitting: false,
            // 班级
            enrolledClasses: [],
            // 通知
            noticeList: [],
            noticeFilterClassId: null,
            // 提问
            qaPage: { page: 1, pageSize: 10 },
            NotHomeWork: [],
            questionTotal: 0,
            askForm: { content: '', sender: null, recipient: 2, videoId: 1, status: 2 },
            editingId: null,
            editContent: ''
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
        onNoticeFilterChange() {
            this.loadNotices()
        },
        async loadNotices() {
            try {
                const params = { userId: this.userId, page: 1, pageSize: 100 }
                if (this.noticeFilterClassId) {
                    params.classId = this.noticeFilterClassId
                }
                const res = await getStudentNotices(params)
                if (res.data.code === 200) {
                    this.noticeList = (res.data.resultData && res.data.resultData.data) || []
                }
            } catch (e) { this.noticeList = [] }
        },
        async loadQuestions() {
            try {
                const params = { page: this.qaPage.page, pageSize: this.qaPage.pageSize, userId: this.userId, roleId: 3 }
                const res = await askandanswer(params)
                if (res.data.code === 200) {
                    const data = res.data.resultData
                    this.NotHomeWork = (data && (data.data || data.records)) || []
                    this.questionTotal = (data && data.total) || 0
                }
            } catch (e) { this.NotHomeWork = [] }
        },
        handleQaSizeChange(size) { this.qaPage.pageSize = size; this.loadQuestions() },
        handleQaPageChange(p) { this.qaPage.page = p; this.loadQuestions() },

        submitQuestion() {
            if (!this.askForm.content.trim()) { this.$message.error('问题内容不能为空'); return }
            this.askForm.sender = this.userId
            this.submitting = true
            saveQuestion(this.askForm).then(res => {
                if (res.data.code === 200) {
                    this.$message.success('提问成功！老师会尽快回复你')
                    this.askForm.content = ''
                    this.loadQuestions()
                    this.activeTab = 'my'
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

/* ====== 通知筛选栏 ====== */
.notice-toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
}
.notice-filter-select { width: 180px; }
.notice-count-text { font-size: 13px; color: #909399; }
.notice-count-text strong { color: #303133; }

/* ====== 通知卡片 ====== */
.notice-card-list { display: flex; flex-direction: column; gap: 12px; }
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
.qa-list { display: flex; flex-direction: column; gap: 16px; }
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

.qa-edit { margin-top: 8px; }
.qa-edit-btns { margin-top: 8px; display: flex; gap: 8px; }

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

.qa-card-actions {
    display: flex;
    gap: 4px;
    padding-top: 4px;
}

.qa-pagination {
    margin-top: 20px;
    text-align: center;
}
</style>
