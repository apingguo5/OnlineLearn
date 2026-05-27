<template>
    <div class="teacher-communication">
        <div class="page-header">
            <h2><i class="el-icon-chat-dot-round"></i> 互动与答疑</h2>
            <div class="header-tabs">
                <el-radio-group v-model="activeTab" size="medium">
                    <el-radio-button label="notification">系统通知</el-radio-button>
                    <el-radio-button label="qa">问答讨论区</el-radio-button>
                </el-radio-group>
            </div>
        </div>

        <!-- ====== 系统通知 ====== -->
        <div v-show="activeTab === 'notification'">
            <el-card shadow="never" class="main-card">
                <div slot="header" class="card-header">
                    <span>系统通知 / 公告</span>
                    <el-button type="primary" icon="el-icon-plus" size="small" @click="showSendNotification = true">发送通知</el-button>
                </div>
                <el-table :data="notificationList" v-loading="loading" stripe style="width:100%">
                    <el-table-column prop="title" label="标题" min-width="200">
                        <template slot-scope="scope">
                            <div class="notification-title">
                                <i class="el-icon-bell" style="color:#E6A23C; margin-right:6px"></i>
                                <span>{{ scope.row.title }}</span>
                            </div>
                        </template>
                    </el-table-column>
                    <el-table-column prop="className" label="目标班级" min-width="140"></el-table-column>
                    <el-table-column prop="content" label="内容" min-width="280" show-overflow-tooltip></el-table-column>
                    <el-table-column prop="createTime" label="发送时间" width="170" align="center"></el-table-column>
                    <el-table-column label="操作" width="80" align="center">
                        <template slot-scope="scope">
                            <el-button type="text" icon="el-icon-delete" style="color:#F56C6C" @click="deleteNotification(scope.row)">删除</el-button>
                        </template>
                    </el-table-column>
                </el-table>
                <el-pagination
                    v-if="notificationTotal > 0"
                    @size-change="handleNotifSizeChange"
                    @current-change="handleNotifPageChange"
                    :current-page="notificationPage.page"
                    :page-sizes="[10, 20, 30, 40]"
                    :page-size="notificationPage.pageSize"
                    layout="total, sizes, prev, pager, next, jumper"
                    :total="notificationTotal">
                </el-pagination>
            </el-card>
        </div>

        <!-- ====== 问答讨论区 ====== -->
        <div v-show="activeTab === 'qa'">
            <el-row :gutter="20">
                <el-col :span="16">
                    <el-card shadow="never" class="main-card">
                        <div slot="header" class="card-header">
                            <span>待回答的问题</span>
                        </div>
                        <div class="qa-empty" v-if="questionList.length === 0">
                            <i class="el-icon-chat-line-square" style="font-size:48px;color:#C0C4CC"></i>
                            <p>暂无待回答的问题</p>
                        </div>
                        <div class="qa-list" v-else v-loading="loading">
                            <div class="qa-item" v-for="(q, idx) in questionList" :key="idx" @click="selectedQuestion = q" :class="{ 'qa-item-active': selectedQuestion && selectedQuestion.id === q.id }">
                                <div class="qa-item-header">
                                    <el-avatar :size="28" style="margin-right:8px">{{ (q.senderName || '?')[0] }}</el-avatar>
                                    <span class="qa-student">{{ q.senderName }}</span>
                                    <el-tag size="mini" type="info">{{ q.topic || '通用问题' }}</el-tag>
                                </div>
                                <p class="qa-question">{{ q.content }}</p>
                                <div class="qa-meta">
                                    <span>{{ q.createTime }}</span>
                                    <span v-if="q.restore && q.restore !== 'undefined'" style="margin-left:12px;color:#67C23A">已回复</span>
                                    <span v-else style="margin-left:12px;color:#E6A23C">待回复</span>
                                </div>
                            </div>
                        </div>
                        <el-pagination
                            v-if="questionTotal > 0"
                            @size-change="handleQaSizeChange"
                            @current-change="handleQaPageChange"
                            :current-page="qaPage.page"
                            :page-sizes="[10, 20, 30, 40]"
                            :page-size="qaPage.pageSize"
                            layout="total, sizes, prev, pager, next, jumper"
                            :total="questionTotal">
                        </el-pagination>
                    </el-card>
                </el-col>
                <el-col :span="8">
                    <el-card shadow="never" class="main-card">
                        <div slot="header" class="card-header">
                            <span>回答区</span>
                        </div>
                        <div v-if="!selectedQuestion" class="qa-select-hint">
                            <i class="el-icon-chat-dot-round" style="font-size:40px;color:#DCDFE6"></i>
                            <p>请从左侧选择一个问题</p>
                        </div>
                        <div v-else class="qa-detail">
                            <div class="qa-detail-question">
                                <p><strong>{{ selectedQuestion.senderName }}：</strong></p>
                                <p>{{ selectedQuestion.content }}</p>
                            </div>
                            <div class="qa-existing-answers" v-if="selectedQuestion.restore && selectedQuestion.restore !== 'undefined'">
                                <p class="qa-answer-label">已有回复：</p>
                                <div class="qa-answer-item">
                                    <el-avatar :size="24" style="margin-right:6px">师</el-avatar>
                                    <div class="qa-answer-content">
                                        <p>{{ selectedQuestion.restore }}</p>
                                    </div>
                                </div>
                            </div>
                            <div class="qa-reply-area">
                                <el-input type="textarea" v-model="replyContent" :rows="3" placeholder="输入回答..."></el-input>
                                <el-button type="primary" size="small" style="margin-top:8px" @click="submitReply">提交回答</el-button>
                            </div>
                        </div>
                    </el-card>
                </el-col>
            </el-row>
        </div>

        <!-- 发送通知对话框 -->
        <el-dialog title="发送通知" :visible.sync="showSendNotification" width="550px">
            <el-form :model="notificationForm" label-width="100px">
                <el-form-item label="通知标题">
                    <el-input v-model="notificationForm.title" placeholder="如：开课提醒"></el-input>
                </el-form-item>
                <el-form-item label="目标班级">
                    <el-select v-model.number="notificationForm.classId" placeholder="请选择班级" style="width:100%">
                        <el-option v-for="cls in classList" :key="cls.className + '_' + cls.id" :label="cls.className" :value="cls.id"></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="通知内容">
                    <el-input type="textarea" v-model="notificationForm.content" :rows="5" placeholder="请输入通知内容..."></el-input>
                </el-form-item>
            </el-form>
            <span slot="footer">
                <el-button @click="showSendNotification = false">取消</el-button>
                <el-button type="primary" @click="sendNotification">发送</el-button>
            </span>
        </el-dialog>
    </div>
</template>

<script>
import * as teacherApi from '@/api/teacher/teacherApi'
import Cookies from 'js-cookie'

export default {
    name: "TeacherCommunication",
    data() {
        return {
            activeTab: 'notification',
            loading: false,
            userId: null,
            classList: [],
            // 通知
            notificationList: [],
            notificationTotal: 0,
            notificationPage: { page: 1, pageSize: 10 },
            showSendNotification: false,
            notificationForm: {
                title: '',
                classId: null,
                content: ''
            },
            // 问答
            questionList: [],
            questionTotal: 0,
            qaPage: { page: 1, pageSize: 10 },
            selectedQuestion: null,
            replyContent: ''
        }
    },
    created() {
        this.userId = Number(Cookies.get('userId'))
        this.loadClasses()
        this.loadNotifications()
        this.loadQuestions()
    },
    methods: {
        async loadClasses() {
            try {
                const res = await teacherApi.getMyClasses({ userId: this.userId })
                const data = res.data.resultData
                this.classList = Array.isArray(data) ? data : []
            } catch (e) { this.classList = [] }
        },
        async loadNotifications() {
            this.loading = true
            try {
                const params = {
                    page: this.notificationPage.page,
                    pageSize: this.notificationPage.pageSize,
                    senderId: this.userId
                }
                const res = await teacherApi.getNotifications(params)
                if (res.data.code === 200) {
                    const data = res.data.resultData
                    this.notificationList = data.data || []
                    this.notificationTotal = data.total || 0
                }
            } catch (e) { this.notificationList = [] }
            this.loading = false
        },
        handleNotifSizeChange(size) {
            this.notificationPage.pageSize = size
            this.loadNotifications()
        },
        handleNotifPageChange(pageNum) {
            this.notificationPage.page = pageNum
            this.loadNotifications()
        },
        async loadQuestions() {
            this.loading = true
            try {
                const params = {
                    page: this.qaPage.page,
                    pageSize: this.qaPage.pageSize,
                    userId: this.userId,
                    roleId: Number(Cookies.get('roleId'))
                }
                const res = await teacherApi.getQaQuestions(params)
                if (res.data.code === 200) {
                    const data = res.data.resultData
                    this.questionList = data.data || data.records || []
                    this.questionTotal = data.total || 0
                }
            } catch (e) { this.questionList = [] }
            this.loading = false
        },
        handleQaSizeChange(size) {
            this.qaPage.pageSize = size
            this.loadQuestions()
        },
        handleQaPageChange(pageNum) {
            this.qaPage.page = pageNum
            this.loadQuestions()
        },
        sendNotification() {
            if (!this.notificationForm.title || !this.notificationForm.content || !this.notificationForm.classId) {
                this.$message.warning('请填写完整信息')
                return
            }
            const params = {
                title: this.notificationForm.title,
                content: this.notificationForm.content,
                classId: Number(this.notificationForm.classId),
                senderId: Number(this.userId)
            }
            teacherApi.sendNotification(params).then((res) => {
                if (res.data.code === 200) {
                    this.$message.success('发送成功')
                    this.showSendNotification = false
                    this.notificationForm = { title: '', classId: null, content: '' }
                    this.loadNotifications()
                } else {
                    this.$message.error('发送失败: ' + (res.data.msg || ''))
                }
            }).catch(() => this.$message.error('发送失败'))
        },
        deleteNotification(row) {
            this.$confirm('确定删除该通知吗？', '提示', { type: 'warning' })
                .then(() => teacherApi.deleteNotification({ id: row.id })
                    .then((res) => {
                        if (res.data.code === 200) {
                            this.$message.success('已删除')
                            this.loadNotifications()
                        } else {
                            this.$message.error('删除失败')
                        }
                    })).catch(() => {})
        },
        submitReply() {
            if (!this.replyContent.trim()) {
                this.$message.warning('请输入回答内容')
                return
            }
            const params = {
                id: this.selectedQuestion.id,
                restore: this.replyContent,
                status: 1
            }
            teacherApi.answerQaQuestion(params)
                .then((res) => {
                    if (res.data.code === 200) {
                        this.$message.success('回答已提交')
                        this.replyContent = ''
                        this.loadQuestions()
                        const qid = this.selectedQuestion.id
                        this.selectedQuestion = this.questionList.find(q => q.id === qid) || null
                    } else {
                        this.$message.error('提交失败')
                    }
                }).catch(() => this.$message.error('提交失败'))
        }
    }
}
</script>

<style scoped>
.teacher-communication {
    padding: 24px;
    background: #f5f7fa;
    min-height: 100vh;
}
.page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    flex-wrap: wrap;
    gap: 12px;
}
.page-header h2 { margin: 0; font-size: 22px; color: #303133; }
.page-header h2 i { margin-right: 8px; color: #409EFF; }
.main-card { border-radius: 8px; margin-bottom: 20px; }
.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}
.card-header span i { margin-right: 6px; }
.notification-title {
    display: flex;
    align-items: center;
}
/* Q&A */
.qa-empty { text-align: center; padding: 48px 0; color: #909399; }
.qa-empty p { margin: 8px 0 0; }
.qa-list { max-height: 560px; overflow-y: auto; }
.qa-item {
    padding: 14px 16px;
    border-bottom: 1px solid #ebeef5;
    cursor: pointer;
    transition: background 0.2s;
}
.qa-item:hover { background: #f5f7fa; }
.qa-item-active { background: #ecf5ff; }
.qa-item-header { display: flex; align-items: center; margin-bottom: 6px; }
.qa-student { font-weight: 600; font-size: 14px; margin-right: 8px; }
.qa-question {
    margin: 6px 0;
    font-size: 14px;
    color: #303133;
    line-height: 1.5;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
}
.qa-meta { font-size: 12px; color: #909399; }
.qa-select-hint { text-align: center; padding: 48px 0; color: #909399; }
.qa-select-hint p { margin: 8px 0 0; }
.qa-detail-question {
    background: #f5f7fa;
    border-radius: 6px;
    padding: 12px;
    margin-bottom: 16px;
}
.qa-detail-question p { margin: 4px 0; }
.qa-answer-label { font-weight: 600; margin: 0 0 8px; color: #606266; }
.qa-answer-item {
    display: flex;
    align-items: flex-start;
    margin-bottom: 12px;
    padding: 8px;
    background: #fafafa;
    border-radius: 6px;
}
.qa-answer-content { flex: 1; }
.qa-answer-content p { margin: 0 0 4px; }
.qa-answer-time { font-size: 12px; color: #909399; }
.qa-reply-area { margin-top: 16px; }
</style>
