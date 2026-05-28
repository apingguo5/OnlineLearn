<template>
    <div class="ai-assistant-page">
        <div class="page-header">
            <h2><i class="el-icon-magic-stick"></i> AI助学</h2>
            <p class="header-desc">智能分析学生成绩，为进度落后的学生提供个性化学习建议</p>
        </div>

        <el-row :gutter="20">
            <el-col :span="24">
                <!-- 班级选择 + 操作 -->
                <el-card shadow="never" class="tool-card">
                    <div class="tool-bar">
                        <div class="tool-left">
                            <el-select v-model="selectedClassId" placeholder="选择班级" @change="onClassChange" style="width:260px" size="medium">
                                <el-option v-for="cls in classList" :key="cls.id" :label="(cls.courseName || '') + ' - ' + cls.className" :value="Number(cls.id)"></el-option>
                            </el-select>
                        </div>
                        <div class="tool-right">
                            <el-button type="primary" icon="el-icon-data-analysis" @click="analyze" :disabled="!selectedClassId" :loading="analyzing">
                                智能分析
                            </el-button>
                            <el-button type="success" icon="el-icon-magic-stick" @click="aiRecommend" :disabled="!selectedClassId || analysisResult.warningCount === 0" :loading="aiLoading">
                                AI学习建议
                            </el-button>
                        </div>
                    </div>
                </el-card>
            </el-col>
        </el-row>

        <!-- 概览统计 -->
        <el-row :gutter="20" v-if="analysisResult.totalStudents > 0">
            <el-col :span="6">
                <div class="stat-card total">
                    <div class="stat-num">{{ analysisResult.totalStudents }}</div>
                    <div class="stat-label">班级总人数</div>
                </div>
            </el-col>
            <el-col :span="6">
                <div class="stat-card avg">
                    <div class="stat-num">{{ analysisResult.averageScore }}</div>
                    <div class="stat-label">班级平均分</div>
                </div>
            </el-col>
            <el-col :span="6">
                <div class="stat-card warn">
                    <div class="stat-num">{{ analysisResult.warningCount }}</div>
                    <div class="stat-label">需关注学生</div>
                </div>
            </el-col>
            <el-col :span="6">
                <div class="stat-card safe">
                    <div class="stat-num">{{ safeCount }}</div>
                    <div class="stat-label">达标学生</div>
                </div>
            </el-col>
        </el-row>

        <!-- 空状态 -->
        <div v-if="!selectedClassId" class="empty-hint">
            <i class="el-icon-magic-stick"></i>
            <p>请先选择班级，然后点击「智能分析」查看学生情况</p>
        </div>
        <div v-else-if="analysisResult.totalStudents > 0 && analysisResult.warningCount === 0" class="empty-hint success">
            <i class="el-icon-circle-check"></i>
            <p>该班级所有学生表现良好，无需特别关注 🎉</p>
        </div>

        <!-- 需关注学生列表 -->
        <el-row :gutter="20" v-if="analysisResult.warningCount > 0">
            <el-col :span="24">
                <el-card shadow="never" class="main-card" v-loading="analyzing">
                    <div slot="header" class="card-header">
                        <span><i class="el-icon-warning" style="color:#E6A23C"></i> 需关注学生（{{ analysisResult.warningCount }}人）</span>
                    </div>
                    <el-table :data="analysisResult.strugglingStudents" stripe style="width:100%" max-height="500">
                        <el-table-column type="index" label="序号" width="60" align="center"></el-table-column>
                        <el-table-column prop="studentName" label="姓名" min-width="100"></el-table-column>
                        <el-table-column prop="studentNo" label="学号" min-width="110"></el-table-column>
                        <el-table-column prop="videoScore" label="视频观看" width="90" align="center">
                            <template slot-scope="scope">
                                <span :style="{ color: Number(scope.row.videoScore) < 60 ? '#F56C6C' : '#67C23A' }">
                                    {{ scope.row.videoScore }}
                                </span>
                            </template>
                        </el-table-column>
                        <el-table-column prop="homeworkScore" label="作业成绩" width="90" align="center">
                            <template slot-scope="scope">
                                <span :style="{ color: Number(scope.row.homeworkScore) < 60 ? '#F56C6C' : '#67C23A' }">
                                    {{ scope.row.homeworkScore }}
                                </span>
                            </template>
                        </el-table-column>
                        <el-table-column prop="examScore" label="考试成绩" width="90" align="center">
                            <template slot-scope="scope">
                                <span :style="{ color: Number(scope.row.examScore) < 60 ? '#F56C6C' : '#67C23A' }">
                                    {{ scope.row.examScore }}
                                </span>
                            </template>
                        </el-table-column>
                        <el-table-column prop="discussionScore" label="讨论成绩" width="90" align="center">
                            <template slot-scope="scope">
                                <span :style="{ color: Number(scope.row.discussionScore) < 60 ? '#F56C6C' : '#67C23A' }">
                                    {{ scope.row.discussionScore }}
                                </span>
                            </template>
                        </el-table-column>
                        <el-table-column label="综合得分" width="100" align="center">
                            <template slot-scope="scope">
                                <el-tag :type="getTagType(scope.row.totalScore)" size="medium">
                                    {{ scope.row.totalScore }}
                                </el-tag>
                            </template>
                        </el-table-column>
                    </el-table>
                </el-card>
            </el-col>
        </el-row>

        <!-- AI学习建议列表 -->
        <el-row :gutter="20" v-if="aiResults.length > 0">
            <el-col :span="24">
                <el-card shadow="never" class="main-card">
                    <div slot="header" class="card-header">
                        <span><i class="el-icon-magic-stick" style="color:#4e6ef2"></i> AI学习建议</span>
                    </div>
                    <div class="ai-advice-list">
                        <div v-for="(item, idx) in aiResults" :key="idx" class="ai-advice-card">
                            <div class="ai-advice-head">
                                <el-avatar :size="40" style="background:#4e6ef2;">{{ (item.studentName || '?')[0] }}</el-avatar>
                                <div class="ai-advice-student">
                                    <div class="ai-student-name">{{ item.studentName }} <span class="ai-student-no">({{ item.studentNo }})</span></div>
                                    <div class="ai-student-scores">
                                        <span class="score-tag">视频{{ item.videoScore }}</span>
                                        <span class="score-tag">作业{{ item.homeworkScore }}</span>
                                        <span class="score-tag">考试{{ item.examScore }}</span>
                                        <span class="score-tag">讨论{{ item.discussionScore }}</span>
                                    </div>
                                </div>
                                <el-tag :type="getTagType(item.totalScore)" size="medium">总分{{ item.totalScore }}</el-tag>
                            </div>
                            <div class="ai-advice-body">
                                <i class="el-icon-chat-dot-square" style="color:#4e6ef2;font-size:18px;margin-right:6px;"></i>
                                <pre class="ai-advice-text">{{ item.aiAdvice }}</pre>
                            </div>
                        </div>
                    </div>
                </el-card>
            </el-col>
        </el-row>
    </div>
</template>

<script>
import * as teacherApi from '@/api/teacher/teacherApi'
import Cookies from 'js-cookie'

export default {
    name: "TeacherAiAssistant",
    data() {
        return {
            classList: [],
            selectedClassId: null,
            analyzing: false,
            aiLoading: false,
            analysisResult: {
                strugglingStudents: [],
                totalStudents: 0,
                warningCount: 0,
                averageScore: 0
            },
            aiResults: []
        }
    },
    computed: {
        safeCount() {
            return this.analysisResult.totalStudents - (this.analysisResult.warningCount || 0)
        }
    },
    created() {
        this.loadClasses()
    },
    methods: {
        async loadClasses() {
            try {
                const userId = Cookies.get('userId')
                const res = await teacherApi.getMyClasses({ userId: Number(userId) })
                if (res.data.code === 200) {
                    this.classList = (res.data.resultData || []).map(c => ({
                        ...c,
                        id: Number(c.id)
                    }))
                } else {
                    this.classList = []
                }
            } catch (e) { this.classList = [] }
        },
        onClassChange() {
            this.analysisResult = { strugglingStudents: [], totalStudents: 0, warningCount: 0, averageScore: 0 }
            this.aiResults = []
        },
        async analyze() {
            if (!this.selectedClassId) return
            this.analyzing = true
            this.aiResults = []
            try {
                const res = await teacherApi.analyzeClass({ classId: this.selectedClassId })
                if (res.data.code === 200) {
                    this.analysisResult = res.data.resultData || this.analysisResult
                } else {
                    this.$message.error('分析失败')
                }
            } catch (e) { this.$message.error('分析请求失败') }
            this.analyzing = false
        },
        async aiRecommend() {
            if (!this.selectedClassId) return
            this.aiLoading = true
            try {
                const res = await teacherApi.getAiRecommend({ classId: this.selectedClassId })
                if (res.data.code === 200) {
                    this.aiResults = res.data.resultData || []
                    if (this.aiResults.length === 0) {
                        this.$message.info('没有需要特别关注的学生')
                    }
                } else {
                    this.$message.error('获取AI建议失败')
                }
            } catch (e) { this.$message.error('AI请求失败') }
            this.aiLoading = false
        },
        getTagType(score) {
            const s = Number(score)
            if (!s && s !== 0) return 'info'
            if (s >= 80) return 'success'
            if (s >= 60) return 'warning'
            return 'danger'
        }
    }
}
</script>

<style scoped>
.ai-assistant-page {
    padding: 24px;
    background: #f5f7fa;
    min-height: 100vh;
}
.page-header {
    margin-bottom: 20px;
}
.page-header h2 {
    margin: 0 0 4px;
    font-size: 22px;
    color: #303133;
}
.page-header h2 i {
    margin-right: 8px;
    color: #4e6ef2;
}
.header-desc {
    margin: 0;
    font-size: 14px;
    color: #909399;
}
.tool-card {
    border-radius: 8px;
    margin-bottom: 20px;
}
.tool-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    gap: 12px;
}
.tool-right {
    display: flex;
    gap: 10px;
}
.main-card {
    border-radius: 8px;
    margin-bottom: 20px;
}
.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-weight: 600;
}
.card-header span i {
    margin-right: 6px;
}

/* Stat Cards */
.stat-card {
    background: #fff;
    border-radius: 10px;
    padding: 24px 20px;
    text-align: center;
    box-shadow: 0 1px 6px rgba(0,0,0,0.06);
    margin-bottom: 20px;
}
.stat-num {
    font-size: 32px;
    font-weight: 700;
    margin-bottom: 6px;
}
.stat-label {
    font-size: 13px;
    color: #909399;
}
.total .stat-num { color: #409EFF; }
.avg .stat-num { color: #67C23A; }
.warn .stat-num { color: #E6A23C; }
.safe .stat-num { color: #67C23A; }

.empty-hint {
    text-align: center;
    padding: 80px 20px;
    color: #909399;
    background: #fff;
    border-radius: 10px;
    box-shadow: 0 1px 4px rgba(0,0,0,0.04);
    margin-top: 20px;
}
.empty-hint i { font-size: 48px; margin-bottom: 12px; display: block; }
.empty-hint p { margin: 0; font-size: 15px; }
.empty-hint.success i { color: #67C23A; }

.ai-advice-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
}
.ai-advice-card {
    background: #fafbff;
    border: 1px solid #e8ecf4;
    border-left: 4px solid #4e6ef2;
    border-radius: 8px;
    padding: 18px 20px;
    transition: all 0.2s;
}
.ai-advice-card:hover {
    box-shadow: 0 2px 12px rgba(78,110,242,0.12);
}
.ai-advice-head {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 14px;
}
.ai-advice-student {
    flex: 1;
}
.ai-student-name {
    font-size: 15px;
    font-weight: 600;
    color: #303133;
}
.ai-student-no {
    font-size: 12px;
    color: #909399;
    font-weight: 400;
}
.ai-student-scores {
    display: flex;
    gap: 6px;
    margin-top: 4px;
}
.score-tag {
    font-size: 11px;
    color: #606266;
    background: #f0f2f5;
    padding: 1px 6px;
    border-radius: 4px;
}
.ai-advice-body {
    display: flex;
    align-items: flex-start;
    background: #fff;
    border-radius: 6px;
    padding: 12px 14px;
}
.ai-advice-text {
    margin: 0;
    font-size: 14px;
    color: #303133;
    line-height: 1.7;
    white-space: pre-wrap;
    word-break: break-word;
    font-family: inherit;
}
</style>
