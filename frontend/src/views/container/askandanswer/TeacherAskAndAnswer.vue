<template>
    <div>
        <div class="ti"> <b> 问答管理</b></div>
        <el-divider></el-divider>
        
        <!-- 统计信息 -->
        <div class="stats-section" v-if="questions.length > 0">
            <el-row :gutter="20">
                <el-col :span="8">
                    <div class="stat-card">
                        <div class="stat-number">{{ total }}</div>
                        <div class="stat-label">总问题数</div>
                    </div>
                </el-col>
                <el-col :span="8">
                    <div class="stat-card pending-card">
                        <div class="stat-number">{{ unansweredCount }}</div>
                        <div class="stat-label">待回复</div>
                    </div>
                </el-col>
                <el-col :span="8">
                    <div class="stat-card answered-card">
                        <div class="stat-number">{{ answeredCount }}</div>
                        <div class="stat-label">已回复</div>
                    </div>
                </el-col>
            </el-row>
            <el-divider></el-divider>
        </div>
        
        <!-- 问题列表 -->
        <div v-if="questions.length === 0" class="empty-state">
            <i class="el-icon-question" style="font-size: 48px; color: #ccc;"></i>
            <p>暂无问题</p>
        </div>
        
        <div v-for="t in questions" :key="t.id" class="question-item">
            <div class="question-header">
                <h4 class="jk">问题来源：{{ t.topic || '通用问题' }}</h4>
                <span class="question-time">{{ t.createTime }}</span>
            </div>
            
            <div class="question-info">
                <span class="info-label">提问学生：</span>
                <span class="info-value">{{ t.senderName }}</span>
            </div>
            
            <div class="question-content">
                <span class="info-label">问题描述：</span>
                <p style="color: red; font-size: 18px;">
                    <i class="iconfont icon-r-shield"></i>
                    <b>{{ t.content }}</b>
                </p>
            </div>
            
            <!-- 已回复状态 -->
            <div v-if="t.restore && t.restore !== 'undefined'" class="answer-section">
                <div class="answer-header">
                    <span class="info-label">我的回复：</span>
                    <el-tag type="success" size="small">已回复</el-tag>
                </div>
                <p style="color: green; font-size: 18px;">
                    <i class="iconfont icon-r-yes"></i>
                    <b>{{ t.restore }}</b>
                </p>
            </div>
            
            <!-- 待回复状态 -->
            <div v-else class="answer-section pending">
                <div class="answer-header">
                    <span class="info-label">回复：</span>
                    <el-tag type="warning" size="small">待回复</el-tag>
                </div>
                <el-input 
                    type="textarea" 
                    placeholder="请输入回复内容" 
                    v-model="answerContents[t.id]" 
                    maxlength="500" 
                    show-word-limit
                    rows="3">
                </el-input>
                <div class="answer-actions">
                    <el-button type="success" @click="answerQuestion(t.id)">提交回复</el-button>
                </div>
            </div>
            
            <!-- 操作按钮 -->
            <div class="question-actions">
                <el-button 
                    type="danger" 
                    size="small" 
                    icon="el-icon-delete"
                    @click="confirmDelete(t.id)">
                    删除问题
                </el-button>
            </div>

            <el-divider><span class="status">{{ t.status }}</span></el-divider>
        </div>
        
        <el-pagination 
            v-if="total > 0"
            @size-change="handleSizeChange" 
            @current-change="handleCurrentChange" 
            :current-page="page.page" 
            :page-sizes="[10, 20, 30, 40]" 
            :page-size="page.pageSize" 
            layout="total, sizes, prev, pager, next, jumper" 
            :total="total">
        </el-pagination>
    </div>
</template>

<script>
import { askandanswer, answerQuestion, deleteQuestion } from '../../../api/teacher/test.js'
import Cookies from "js-cookie";
export default {
    name: "TeacherAskAndAnswer",
    data() {
        return {
            page: {
                page: 1,
                pageSize: 10,
                userId: 0,
            },
            questions: [],
            total: 0,
            // 使用对象存储每个问题的回复内容
            answerContents: {}
        }
    },
    computed: {
        // 未回复数量
        unansweredCount() {
            return this.questions.filter(q => !q.restore || q.restore === 'undefined').length
        },
        // 已回复数量
        answeredCount() {
            return this.questions.filter(q => q.restore && q.restore !== 'undefined').length
        }
    },
    created() {
        this.page.userId = Cookies.get('userId')
        this.page.roleId = Cookies.get('roleId')
        this.getQuestions(this.page)
    },
    methods: {
        handleSizeChange(size) {
            this.page.pageSize = size;
            this.getQuestions(this.page)
        },
        handleCurrentChange(pageNum) {
            this.page.page = pageNum;
            this.getQuestions(this.page)
        },
        getQuestions(valid) {
            askandanswer(valid).then(resp => {
                this.questions = resp.data.resultData.records
                this.total = resp.data.resultData.total
                // 初始化回复内容对象
                this.answerContents = {}
            })
        },
        answerQuestion(id) {
            const content = this.answerContents[id]
            if (!content || !content.trim()) {
                this.$message.error('回复内容不能为空');
                return;
            }
            const params = {
                id: id,
                restore: content,
                status: '1'  // 已回复状态
            }
            answerQuestion(params).then(resp => {
                if (resp.data.code == 200) {
                    this.$message.success('回复成功');
                    this.answerContents[id] = ''
                    this.getQuestions(this.page);
                } else {
                    this.$message.error('回复失败');
                }
            })
        },
        // 确认删除
        confirmDelete(id) {
            this.$confirm('确定要删除这个问题吗？删除后无法恢复。', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                this.deleteQuestion(id)
            }).catch(() => {
                this.$message.info('已取消删除');
            })
        },
        // 删除问题
        deleteQuestion(id) {
            deleteQuestion({ id: id }).then(resp => {
                if (resp.data.code == 200) {
                    this.$message.success('删除成功');
                    this.getQuestions(this.page);
                } else {
                    this.$message.error('删除失败');
                }
            })
        }
    }
}
</script>

<style scoped>
.ti {
    font-size: 28px;
    width: 100%;
    text-align: center;
}

.status {
    font-size: 20px;
}

/* 统计区域 */
.stats-section {
    margin-bottom: 20px;
}

.stat-card {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    padding: 20px;
    border-radius: 8px;
    text-align: center;
    box-shadow: 0 4px 6px rgba(0,0,0,0.1);
}

.pending-card {
    background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.answered-card {
    background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-number {
    font-size: 32px;
    font-weight: bold;
    margin-bottom: 5px;
}

.stat-label {
    font-size: 14px;
    opacity: 0.9;
}

/* 空状态 */
.empty-state {
    text-align: center;
    padding: 60px 20px;
    color: #999;
}

.empty-state p {
    margin-top: 10px;
    font-size: 16px;
}

/* 问题项 */
.question-item {
    background-color: #fafafa;
    padding: 20px;
    border-radius: 8px;
    margin-bottom: 20px;
    box-shadow: 0 2px 4px rgba(0,0,0,0.08);
}

.question-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 15px;
}

.jk {
    color: #333;
    margin: 0;
    font-size: 16px;
}

.question-time {
    color: #999;
    font-size: 14px;
}

.question-info {
    margin: 10px 0;
}

.info-label {
    font-weight: bold;
    color: #666;
    margin-right: 5px;
}

.info-value {
    color: #333;
}

.question-content {
    margin: 15px 0;
}

/* 回复区域 */
.answer-section {
    background-color: #f0f9ff;
    padding: 15px;
    border-radius: 6px;
    margin: 15px 0;
}

.answer-section.pending {
    background-color: #fff7ed;
}

.answer-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;
}

.answer-actions {
    margin-top: 10px;
    text-align: right;
}

/* 操作按钮 */
.question-actions {
    margin-top: 15px;
    text-align: right;
}
</style>