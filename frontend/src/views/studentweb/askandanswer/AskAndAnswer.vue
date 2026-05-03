<template>
    <div>
        <!-- 页面标题和装饰 -->
        <div class="page-header">
            <h2 class="page-title">
                <img src="https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=question%20answer%20community%20icon%20chat%20dialog%20help&image_size=square" alt="问答社区" class="title-icon">
                问答社区
            </h2>
            <p class="page-subtitle">知识共享，互助成长</p>
        </div>
        <el-divider></el-divider>
        
        <!-- 提问区域 -->
        <div class="ask-section">
            <div class="section-header">
                <img src="https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=ask%20question%20icon%20help%20support&image_size=square" alt="我要提问" class="section-icon">
                <h3>我要提问</h3>
            </div>
            <el-form :model="askForm" label-width="80px">
                <el-form-item label="问题内容">
                    <el-input type="textarea" v-model="askForm.content" placeholder="请详细描述你的问题" rows="4"></el-input>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="submitQuestion">
                        <i class="el-icon-send"></i> 提交提问
                    </el-button>
                </el-form-item>
            </el-form>
            <el-divider></el-divider>
        </div>
        
        <!-- 我的提问列表 -->
        <div class="my-questions-section">
            <div class="section-header">
                <img src="https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=my%20questions%20icon%20list%20history&image_size=square" alt="我的提问" class="section-icon">
                <h3>我的提问</h3>
            </div>

            <div v-if="NotHomeWork.length === 0" style="text-align: center; color: #999; padding: 50px; background: url('https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=empty%20state%20illustration%20no%20questions%20simple%20minimal&image_size=landscape_16_9') no-repeat center; background-size: contain;">
                <div style="background: rgba(255,255,255,0.8); padding: 20px; border-radius: 8px; display: inline-block;">
                    暂无提问记录
                </div>
            </div>
            <div v-for="t in NotHomeWork" :key="t.id" class="question-item">
                <h4 class="jk">问题来源：{{ t.topic || '通用问题' }}</h4>
                <div class="question-info">
                    <span class="info-label">提问学生：</span>
                    <span class="info-value">{{ t.senderName }}</span>
                </div>
                
                <!-- 问题内容区域 -->
                <div v-if="editingId !== t.id" class="question-content">
                    <span class="info-label">问题描述：</span>
                    <p class="content-text" style="color: red; font-size: 18px;">
                        <i class="iconfont icon-r-shield" style="font-size: 20px;"></i>
                        <b>{{ t.content }}</b>
                    </p>
                </div>
                
                <!-- 修改模式 -->
                <div v-else class="edit-content">
                    <el-input type="textarea" v-model="editContent" placeholder="请输入修改后的内容" rows="3"></el-input>
                    <div class="edit-actions">
                        <el-button type="primary" size="small" @click="confirmEdit(t.id)">确认修改</el-button>
                        <el-button size="small" @click="cancelEdit">取消</el-button>
                    </div>
                </div>
                
                <div class="question-info">
                    <span class="info-label">回复教师：</span>
                    <span class="info-value">{{ t.recipientName || '待分配' }}</span>
                </div>
                
                <div class="question-info">
                    <span class="info-label">问题回复：</span>
                    <p v-if="t.restore && t.restore !== 'undefined'" style="color: green; font-size: 18px;">
                        <i class="iconfont icon-r-yes" style="font-size: 20px;"></i>
                        <b>{{ t.restore }}</b>
                    </p>
                    <p v-else style="color: grey; font-size: 18px;">
                        <i class="iconfont icon-r-refresh" style="font-size: 20px;"></i>
                        <b>等待回复 ...</b>
                    </p>
                </div>
                
                <!-- 操作按钮 -->
                <div class="question-actions">
                    <!-- 学生只能修改和删除自己发布的、未被回复的问题 -->
                    <el-button 
                        v-if="canEditOrDelete(t)" 
                        type="warning" 
                        size="small" 
                        icon="el-icon-edit"
                        @click="startEdit(t)">
                        修改
                    </el-button>
                    <el-button 
                        v-if="canEditOrDelete(t)" 
                        type="danger" 
                        size="small" 
                        icon="el-icon-delete"
                        @click="confirmDelete(t.id)">
                        删除
                    </el-button>
                    <span v-if="!canEditOrDelete(t) && t.restore" class="answered-tag">
                        已回复
                    </span>
                </div>

                <el-divider><span class="status">{{ t.status }}</span></el-divider>
            </div>
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
import { askandanswer, deleteQuestion, updateQuestion } from '../../../api/studentweb/askandanswer.js'
import { post } from '../../../api/request'
import Cookies from "js-cookie";
export default {
    name: "AskAndAnswer",
    data() {
        return {
            page: {
                page: 1,
                pageSize: 10,
                userId: 0,
            },
            NotHomeWork: [],
            total: 0,
            askForm: {
                content: '',
                sender: '',
                recipient: 2,
                videoId: 1,
                status: 0
            },
            // 修改相关
            editingId: null,
            editContent: '',
        }
    },
    created() {
        console.log('AskAndAnswer component created')
        console.log('Cookies:', Cookies.get())
        this.page.userId = 19 // 临时设置为 19，测试用
        this.page.roleId = 3 // 临时设置为 3，学生角色
        this.askForm.sender = 19
        console.log('page:', this.page)
        console.log('askForm:', this.askForm)
        console.log('Calling CheckWork...')
        this.CheckWork(this.page)
    },
    methods: {
        handleSizeChange(size) {
            this.page.pageSize = size;
            this.CheckWork(this.page)
        },
        handleCurrentChange(pageNum) {
            this.page.page = pageNum;
            this.CheckWork(this.page)
        },
        CheckWork(valid) {
            console.log('CheckWork called with:', valid)
            
            // 使用 fetch API 测试
            console.log('Sending fetch request...')
            try {
                fetch('http://localhost:9251/study/askQuestions/list', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(valid)
                })
                .then(resp => {
                    console.log('Response received:', resp)
                    return resp.json()
                })
                .then(data => {
                    console.log('API response:', data)
                    if (data && data.resultData) {
                        this.NotHomeWork = data.resultData.records || data.resultData.data
                        this.total = data.resultData.total
                        console.log('NotHomeWork:', this.NotHomeWork)
                        console.log('Total:', this.total)
                    } else {
                        console.error('Invalid response structure:', data)
                    }
                })
                .catch(error => {
                    console.error('Error in CheckWork:', error)
                })
            } catch (e) {
                console.error('Exception in CheckWork:', e)
            }
        },

        submitQuestion() {
            console.log('submitQuestion called')
            if (!this.askForm.content.trim()) {
                console.log('问题内容不能为空')
                this.$message.error('问题内容不能为空');
                return;
            }
            
            console.log('askForm:', this.askForm)
            
            // 使用 fetch API 发送请求
            console.log('Sending save request...')
            fetch('http://localhost:9251/study/askQuestions/save', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(this.askForm)
            })
            .then(resp => {
                console.log('Response received:', resp)
                return resp.json()
            })
            .then(data => {
                console.log('Save response:', data)
                if (data.code == 200) {
                    console.log('提问成功')
                    this.$message.success('提问成功');
                    this.askForm.content = '';
                    this.CheckWork(this.page);
                } else {
                    console.log('提问失败:', data.message || '未知错误')
                    this.$message.error('提问失败: ' + (data.message || '未知错误'));
                }
            })
            .catch(error => {
                console.error('请求失败:', error);
                this.$message.error('网络错误: 无法连接到服务器');
            })
        },
        
        // 判断是否可以修改或删除
        canEditOrDelete(question) {
            // 只有未回复的问题才能修改和删除
            // status 为 '未回复' 或 '0' 或未定义
            const status = question.status
            return status === '未回复' || status === '0' || !status || status === '2' || question.restore === undefined || question.restore === null || question.restore === 'undefined'
        },
        
        // 开始修改
        startEdit(question) {
            this.editingId = question.id
            this.editContent = question.content
        },
        
        // 确认修改
        confirmEdit(id) {
            if (!this.editContent.trim()) {
                this.$message.error('问题内容不能为空');
                return;
            }
            
            const params = {
                id: id,
                content: this.editContent
            }
            
            // 使用 fetch API 直接调用后端接口
            fetch('/api/study/askQuestions/update', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(params)
            })
            .then(resp => {
                console.log('Response received:', resp)
                return resp.json()
            })
            .then(data => {
                console.log('Update response:', data)
                if (data.code == 200) {
                    this.$message.success('修改成功');
                    this.editingId = null
                    this.editContent = ''
                    this.CheckWork(this.page)
                } else {
                    this.$message.error('修改失败: ' + (data.message || '未知错误'));
                }
            })
            .catch(error => {
                console.error('Update error:', error);
                this.$message.error('网络错误: 无法连接到服务器');
            })
        },
        
        // 取消修改
        cancelEdit() {
            this.editingId = null
            this.editContent = ''
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
                    this.CheckWork(this.page);
                } else {
                    this.$message.error('删除失败');
                }
            })
        }
    }
}
</script>

<style scoped>
/* 页面标题样式 */
.page-header {
    text-align: center;
    padding: 30px 0;
    background: linear-gradient(135deg, #f0f4f8 0%, #e2e8f0 100%);
    border-radius: 8px;
    margin-bottom: 30px;
    box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.page-title {
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28px;
    color: #333;
    margin-bottom: 10px;
}

.title-icon {
    width: 40px;
    height: 40px;
    margin-right: 15px;
    border-radius: 50%;
    box-shadow: 0 2px 5px rgba(0,0,0,0.2);
}

.page-subtitle {
    font-size: 16px;
    color: #666;
    margin: 0;
}

/*  section 头部样式 */
.section-header {
    display: flex;
    align-items: center;
    margin-bottom: 20px;
}

.section-icon {
    width: 24px;
    height: 24px;
    margin-right: 10px;
    border-radius: 50%;
    box-shadow: 0 1px 3px rgba(0,0,0,0.2);
}

.section-header h3 {
    color: #09720e;
    margin: 0;
    font-size: 20px;
}

/* 提问区域样式 */
.ask-section {
    background-color: #f9f9f9;
    padding: 20px;
    border-radius: 8px;
    margin-bottom: 20px;
    box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.ask-section .el-form {
    max-width: 800px;
}

/* 我的提问区域样式 */
.my-questions-section {
    margin-top: 20px;
}

/* 问题项样式 */
.question-item {
    background-color: #fafafa;
    padding: 20px;
    border-radius: 8px;
    margin-bottom: 20px;
    box-shadow: 0 1px 3px rgba(0,0,0,0.08);
    transition: all 0.3s ease;
}

.question-item:hover {
    box-shadow: 0 4px 12px rgba(0,0,0,0.12);
    transform: translateY(-2px);
}

.jk {
    color: #333;
    margin-bottom: 15px;
    font-size: 16px;
    text-align: center;
}

.question-info {
    margin: 12px 0;
    line-height: 1.6;
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
    background-color: #f0f8f0;
    padding: 15px;
    border-radius: 6px;
    border-left: 4px solid #4caf50;
}

.content-text {
    margin: 0;
    padding-left: 10px;
}

.edit-content {
    margin: 15px 0;
}

.edit-actions {
    margin-top: 10px;
    display: flex;
    gap: 10px;
}

.question-actions {
    margin-top: 15px;
    display: flex;
    gap: 10px;
    align-items: center;
}

.answered-tag {
    background-color: #e8f5e9;
    color: #4caf50;
    padding: 4px 12px;
    border-radius: 4px;
    font-size: 12px;
}

/* 响应式设计 */
@media (max-width: 768px) {
    .page-title {
        font-size: 24px;
    }
    
    .title-icon {
        width: 32px;
        height: 32px;
    }
    
    .ask-section {
        padding: 15px;
    }
    
    .question-item {
        padding: 15px;
    }
}
</style>
