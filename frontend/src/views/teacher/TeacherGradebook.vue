<template>
    <div class="teacher-gradebook">
        <div class="page-header">
            <h2><i class="el-icon-s-data"></i> 成绩与报表</h2>
            <div class="header-actions">
                <el-select v-model="selectedClassId" placeholder="选择班级" @change="loadGradebook" style="width:200px">
                    <el-option v-for="cls in classList" :key="cls.id" :label="cls.className" :value="cls.id"></el-option>
                </el-select>
                <el-button type="success" icon="el-icon-download" @click="exportReport">导出报表</el-button>
            </div>
        </div>

        <el-row :gutter="20">
            <!-- 左侧：权重设置 -->
            <el-col :span="8">
                <el-card shadow="never" class="main-card">
                    <div slot="header" class="card-header">
                        <span><i class="el-icon-setting"></i> 权重设置</span>
                        <el-button type="text" size="small" @click="saveWeights">保存权重</el-button>
                    </div>
                    <div class="weight-form">
                        <div class="weight-item" v-for="(item, idx) in weightConfig" :key="idx">
                            <div class="weight-header">
                                <span class="weight-label">{{ item.label }}</span>
                                <el-tag size="mini" type="primary">{{ item.percent }}%</el-tag>
                            </div>
                            <el-slider v-model="item.percent" :min="0" :max="100" :step="5"
                                :format-tooltip="v => v + '%'"
                                @change="rebalanceWeights"></el-slider>
                        </div>
                        <div class="weight-total">
                            <span>总计</span>
                            <span :style="{ color: totalWeight === 100 ? '#67C23A' : '#F56C6C', fontWeight: 700 }">
                                {{ totalWeight }}%
                            </span>
                        </div>
                        <div class="weight-hint" v-if="totalWeight !== 100">
                            <i class="el-icon-warning" style="color:#E6A23C"></i>
                            权重之和必须为100%
                        </div>
                    </div>
                </el-card>
            </el-col>

            <!-- 右侧：成绩统计表 -->
            <el-col :span="16">
                <el-card shadow="never" class="main-card">
                    <div slot="header" class="card-header">
                        <span><i class="el-icon-document"></i> 成绩统计</span>
                        <div>
                            <el-tag size="medium" style="margin-right:8px">平均分：{{ averageScore }}</el-tag>
                            <el-tag size="medium" type="success" style="margin-right:8px">最高分：{{ maxScore }}</el-tag>
                            <el-tag size="medium" type="warning">最低分：{{ minScore }}</el-tag>
                        </div>
                    </div>
                    <el-table :data="gradeList" v-loading="loading" stripe style="width:100%" max-height="500">
                        <el-table-column type="index" label="序号" width="60" align="center"></el-table-column>
                        <el-table-column prop="studentName" label="学生姓名" min-width="100"></el-table-column>
                        <el-table-column prop="username" label="学号" min-width="110"></el-table-column>
                        <el-table-column v-for="(item, idx) in weightConfig" :key="idx"
                            :label="item.label" min-width="90" align="center">
                            <template slot-scope="scope">
                                <span>{{ scope.row[item.key] || 0 }}</span>
                            </template>
                        </el-table-column>
                        <el-table-column label="综合得分" min-width="100" align="center" fixed="right">
                            <template slot-scope="scope">
                                <el-tag :type="getScoreType(scope.row.totalScore)" size="medium">
                                    {{ scope.row.totalScore || 0 }}
                                </el-tag>
                            </template>
                        </el-table-column>
                    </el-table>
                    <div class="empty-hint" v-if="gradeList.length === 0 && selectedClassId">
                        <i class="el-icon-document"></i>
                        <p>暂无成绩数据，请先选择班级</p>
                    </div>
                </el-card>
            </el-col>
        </el-row>
    </div>
</template>

<script>
import * as teacherApi from '@/api/teacher/teacherApi'

export default {
    name: "TeacherGradebook",
    data() {
        return {
            loading: false,
            classList: [],
            selectedClassId: null,
            weightConfig: [
                { label: '视频观看', key: 'videoScore', percent: 30 },
                { label: '作业成绩', key: 'homeworkScore', percent: 30 },
                { label: '考试成绩', key: 'examScore', percent: 40 }
            ],
            gradeList: []
        }
    },
    computed: {
        totalWeight() {
            return this.weightConfig.reduce((s, i) => s + (i.percent || 0), 0)
        },
        averageScore() {
            if (this.gradeList.length === 0) return '-'
            const total = this.gradeList.reduce((s, r) => s + (r.totalScore || 0), 0)
            return (total / this.gradeList.length).toFixed(1)
        },
        maxScore() {
            if (this.gradeList.length === 0) return '-'
            return Math.max(...this.gradeList.map(r => r.totalScore || 0))
        },
        minScore() {
            if (this.gradeList.length === 0) return '-'
            return Math.min(...this.gradeList.map(r => r.totalScore || 0))
        }
    },
    created() {
        this.loadClasses()
    },
    methods: {
        async loadClasses() {
            try {
                const res = await teacherApi.getMyClasses({})
                this.classList = (res.data && res.data.list) ? res.data.list : []
            } catch (e) { this.classList = [] }
        },
        async loadGradebook() {
            if (!this.selectedClassId) return
            this.loading = true
            try {
                const res = await teacherApi.getGradebook({ classId: this.selectedClassId })
                if (res.data && res.data.list) {
                    this.gradeList = res.data.list.map(r => ({
                        ...r,
                        totalScore: this.calcTotalScore(r)
                    }))
                } else {
                    this.gradeList = []
                }
            } catch (e) { this.gradeList = [] }
            this.loading = false
        },
        calcTotalScore(row) {
            let total = 0
            this.weightConfig.forEach(item => {
                total += ((row[item.key] || 0) * (item.percent || 0)) / 100
            })
            return Math.round(total)
        },
        rebalanceWeights() {
            // 不做自动调整，仅标记
        },
        saveWeights() {
            if (this.totalWeight !== 100) {
                this.$message.warning('权重之和必须为100%')
                return
            }
            teacherApi.saveWeightConfig({ classId: this.selectedClassId, weights: this.weightConfig })
                .then(() => {
                    this.$message.success('保存成功')
                    if (this.selectedClassId) this.loadGradebook()
                }).catch(() => this.$message.error('保存失败'))
        },
        getScoreType(score) {
            if (!score && score !== 0) return 'info'
            if (score >= 80) return 'success'
            if (score >= 60) return 'warning'
            return 'danger'
        },
        exportReport() {
            if (!this.selectedClassId) {
                this.$message.warning('请先选择班级')
                return
            }
            teacherApi.exportGradeReport({ classId: this.selectedClassId })
                .then(() => {
                    this.$message.success('报表导出成功，请查看下载文件')
                }).catch(() => this.$message.error('导出失败'))
        }
    }
}
</script>

<style scoped>
.teacher-gradebook {
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
.header-actions { display: flex; gap: 10px; align-items: center; }
.main-card { border-radius: 8px; margin-bottom: 20px; }
.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}
.card-header span i { margin-right: 6px; }
.weight-form { padding: 4px 0; }
.weight-item { margin-bottom: 18px; }
.weight-header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 6px;
}
.weight-label { font-weight: 600; font-size: 14px; color: #303133; }
.weight-total {
    text-align: center;
    padding: 12px 0;
    font-size: 16px;
    border-top: 1px solid #ebeef5;
    display: flex;
    justify-content: space-between;
}
.weight-hint {
    text-align: center;
    font-size: 13px;
    color: #E6A23C;
    padding: 4px 0;
}
.empty-hint {
    text-align: center;
    padding: 48px 0;
    color: #909399;
}
.empty-hint i { font-size: 28px; }
.empty-hint p { margin: 8px 0 0; }
</style>