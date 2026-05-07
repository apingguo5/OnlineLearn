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
            </el-card>
        </div>

        <!-- ====== 问答讨论区 ====== -->
        <div v-show="activeTab === 'qa'">
            <el-row :gutter="20">
                <el-col :span="16">
                    <el-card shadow="never" class="main-card">
                        <div slot="header" class="card-header">
                            <span>待回答的问题</span>
                            <el-select v-model="qaFilterClass" placeholder="按班级筛选" style="width:160px" clearable @change="loadQuestions">
                                <el-option v-for="cls in classList" :key="cls.id" :label="cls.className" :value="cls.id"></el-option>
                            </el-select>
                        </div>
                        <div class="qa-empty" v-if="questionList.length === 0">
                            <i class="el-icon-chat-line-square" style="font-size:48px;color:#C0C4CC"></i>
                            <p>暂无待回答的问题</p>
                        </div>
                        <div class="qa-list" v-else v-loading="loading">
                            <div class="qa-item" v-for="(q, idx) in questionList" :key="idx" @click="selectedQuestion = q" :class="{ 'qa-item-active': selectedQuestion && selectedQuestion.id === q.id }">
                                <div class="qa-item-header">
                                    <el-avatar :size="28" :src="q.avatar" style="margin-right:8px">{{ (q.studentName || '?')[0] }}</el-avatar>
                                    <span class="qa-student">{{ q.studentName }}</span>
                                    <el-tag size="mini" type="info">{{ q.className }}</el-tag>
                                </div>
                                <p class="qa-question">{{ q.content }}</p>
                                <div class="qa-meta">
                                    <span>{{ q.createTime }}</span>
                                    <span v-if="q.answerCount" style="margin-left:12px;color:#409EFF">{{ q.answerCount }} 个回答</span>
                                </div>
                            </div>
                        </div>
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
                                <p><strong>{{ selectedQuestion.studentName }}：</strong></p>
                                <p>{{ selectedQuestion.content }}</p>
                            </div>
                            <div class="qa-existing-answers" v-if="selectedQuestion.answers && selectedQuestion.answers.length">
                                <p class="qa-answer-label">已有回答：</p>
                                <div class="qa-answer-item" v-for="(ans, idx) in selectedQuestion.answers" :key="idx">
                                    <el-avatar :size="24" style="margin-right:6px">师</el-avatar>
                                    <div class="qa-answer-content">
                                        <p>{{ ans.content }}</p>
                                        <span class="qa-answer-time">{{ ans.createTime }}</span>
                                        <el-button type="text" size="mini" icon="el-icon-top" style="margin-left:8px">置顶</el-button>
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
                    <el-select v-model="notificationForm.classId" placeholder="请选择班级" style="width:100%">
                        <el-option v-for="cls in classList" :key="cls.id" :label="cls.className" :value="cls.id"></el-option>
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

export default {
    name: "TeacherCommunication",
    data() {
        return {
            activeTab: 'notification',
            loading: false,
            classList: [],
            // 通知
            notificationList: [],
            showSendNotification: false,
            notificationForm: {
                title: '',
                classId: null,
                content: ''
            },
            // 问答
            questionList: [],
            qaFilterClass: null,
            selectedQuestion: null,
            replyContent: ''
        }
    },
    created() {
        this.loadClasses()
        this.loadNotifications()
        this.loadQuestions()
    },
    methods: {
        async loadClasses() {
            try {
                const res = await teacherApi.getMyClasses({})
                this.classList = (res.data && res.data.list) ? res.data.list : []
            } catch (e) { this.classList = [] }
        },
        async loadNotifications() {
            this.loading = true
            try {
                const res = await teacherApi.getNotifications({})
                this.notificationList = (res.data && res.data.list) ? res.data.list : []
            } catch (e) { this.notificationList = [] }
            this.loading = false
        },
        async loadQuestions() {
            this.loading = true
            try {
                const params = {}
                if (this.qaFilterClass) params.classId = this.qaFilterClass
                const res = await teacherApi.getQuestions(params)
                this.questionList = (res.data && res.data.list) ? res.data.list : []
            } catch (e) { this.questionList = [] }
            this.loading = false
        },
        sendNotification() {
            if (!this.notificationForm.title || !this.notificationForm.content || !this.notificationForm.classId) {
                this.$message.warning('请填写完整信息')
                return
            }
            teacherApi.sendNotification(this.notificationForm).then(() => {
                this.$message.success('发送成功')
                this.showSendNotification = false
                this.notificationForm = { title: '', classId: null, content: '' }
                this.loadNotifications()
            }).catch(() => this.$message.error('发送失败'))
        },
        deleteNotification(row) {
            this.$confirm('确定删除该通知吗？', '提示', { type: 'warning' })
                .then(() => teacherApi.deleteNotification({ id: row.id })
                    .then(() => {
                        this.$message.success('已删除')
                        this.loadNotifications()
                    })).catch(() => {})
        },
        submitReply() {
            if (!this.replyContent.trim()) {
                this.$message.warning('请输入回答内容')
                return
            }
            teacherApi.answerQuestion({ questionId: this.selectedQuestion.id, content: this.replyContent })
                .then(() => {
                    this.$message.success('回答已提交')
                    this.replyContent = ''
                    this.loadQuestions()
                    // 重新选择该问题以刷新回答区
                    const qid = this.selectedQuestion.id
                    this.selectedQuestion = null
                    this.$nextTick(() => {
                        this.selectedQuestion = this.questionList.find(q => q.id === qid)
                    })
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