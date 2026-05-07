<template>
    <div class="teacher-class-mgmt">
        <div class="page-header">
            <h2><i class="el-icon-s-custom"></i> 班级与学生管理</h2>
            <el-button type="primary" icon="el-icon-plus" @click="showCreateDialog = true">创建班级</el-button>
        </div>

        <!-- 班级列表 -->
        <el-card shadow="never" class="main-card">
            <el-table :data="classList" v-loading="loading" stripe style="width:100%">
                <el-table-column prop="className" label="班级名称" min-width="160">
                    <template slot-scope="scope">
                        <div class="class-cell">
                            <i class="el-icon-s-grid" style="color:#409EFF; font-size:20px; margin-right:8px"></i>
                            <span class="class-name-text">{{ scope.row.className }}</span>
                        </div>
                    </template>
                </el-table-column>
                <el-table-column prop="subjectName" label="所属课程" min-width="140"></el-table-column>
                <el-table-column prop="studentCount" label="学生人数" width="100" align="center">
                    <template slot-scope="scope">
                        <el-tag>{{ scope.row.studentCount || 0 }}人</el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="100" align="center">
                    <template slot-scope="scope">
                        <el-tag :type="scope.row.status === 'active' ? 'success' : 'info'" size="small">
                            {{ scope.row.status === 'active' ? '进行中' : '已结课' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="260" align="center" fixed="right">
                    <template slot-scope="scope">
                        <el-button type="text" icon="el-icon-user" @click="manageStudents(scope.row)">学生管理</el-button>
                        <el-button type="text" icon="el-icon-monitor" @click="viewMonitor(scope.row)">学情监控</el-button>
                        <el-button type="text" icon="el-icon-delete" style="color:#F56C6C" @click="deleteClass(scope.row)">删除</el-button>
                    </template>
                </el-table-column>
            </el-table>
        </el-card>

        <!-- 创建班级对话框 -->
        <el-dialog title="创建班级" :visible.sync="showCreateDialog" width="500px">
            <el-form :model="classForm" :rules="classRules" ref="classForm" label-width="100px">
                <el-form-item label="选择课程" prop="subjectId">
                    <el-select v-model="classForm.subjectId" placeholder="请选择课程" style="width:100%">
                        <el-option v-for="c in subjectList" :key="c.id" :label="c.subjectName" :value="c.id"></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="班级名称" prop="className">
                    <el-input v-model="classForm.className" placeholder="如：2024春季1班"></el-input>
                </el-form-item>
                <el-form-item label="开课时间">
                    <el-date-picker v-model="classForm.startTime" type="date" placeholder="选填" style="width:100%"></el-date-picker>
                </el-form-item>
                <el-form-item label="结课时间">
                    <el-date-picker v-model="classForm.endTime" type="date" placeholder="选填" style="width:100%"></el-date-picker>
                </el-form-item>
            </el-form>
            <span slot="footer">
                <el-button @click="showCreateDialog = false">取消</el-button>
                <el-button type="primary" @click="createClass" :loading="saving">创建</el-button>
            </span>
        </el-dialog>

        <!-- 学生管理对话框 -->
        <el-dialog :title="'学生管理 - ' + currentClassName" :visible.sync="showStudentDialog" width="800px">
            <div class="student-mgmt-header">
                <el-button type="primary" icon="el-icon-plus" size="small" @click="showImportDialog = true">批量导入</el-button>
                <el-input v-model="studentSearch" placeholder="搜索学生" prefix-icon="el-icon-search" style="width:220px; margin-left:10px"></el-input>
            </div>
            <el-table :data="filteredStudents" v-loading="studentLoading" stripe style="width:100%; margin-top:12px">
                <el-table-column prop="realName" label="姓名" min-width="120"></el-table-column>
                <el-table-column prop="username" label="学号/账号" min-width="120"></el-table-column>
                <el-table-column prop="progress" label="学习进度" width="160">
                    <template slot-scope="scope">
                        <el-progress :percentage="scope.row.progress || 0" :stroke-width="6" style="width:120px"></el-progress>
                    </template>
                </el-table-column>
                <el-table-column prop="activeDuration" label="活跃时长(h)" width="110" align="center"></el-table-column>
                <el-table-column label="操作" width="100" align="center">
                    <template slot-scope="scope">
                        <el-button type="text" icon="el-icon-delete" style="color:#F56C6C" @click="removeStudent(scope.row)">移出</el-button>
                    </template>
                </el-table-column>
            </el-table>
        </el-dialog>

        <!-- 批量导入学生对话框 -->
        <el-dialog title="批量导入学生" :visible.sync="showImportDialog" width="400px">
            <div class="import-hint">
                <p><i class="el-icon-info" style="color:#409EFF"></i> 请输入学生学号（每行一个）</p>
            </div>
            <el-input type="textarea" v-model="importStudentsText" :rows="8" placeholder="例如：&#10;2024001&#10;2024002&#10;2024003"></el-input>
            <span slot="footer">
                <el-button @click="showImportDialog = false">取消</el-button>
                <el-button type="primary" @click="doBatchImport">导入</el-button>
            </span>
        </el-dialog>

        <!-- 学情监控对话框 -->
        <el-dialog :title="'学情监控 - ' + currentClassName" :visible.sync="showMonitorDialog" width="900px">
            <div class="monitor-summary">
                <el-row :gutter="16">
                    <el-col :span="8" v-for="stat in monitorStats" :key="stat.label">
                        <div class="monitor-stat-card">
                            <p class="monitor-stat-label">{{ stat.label }}</p>
                            <p class="monitor-stat-value" :style="{ color: stat.color }">{{ stat.value }}</p>
                        </div>
                    </el-col>
                </el-row>
            </div>
            <h4 style="margin:20px 0 12px">学生学习详情</h4>
            <el-table :data="monitorStudents" v-loading="monitorLoading" stripe style="width:100%">
                <el-table-column prop="realName" label="姓名" min-width="100"></el-table-column>
                <el-table-column prop="username" label="学号" min-width="110"></el-table-column>
                <el-table-column label="视频完成率" min-width="140">
                    <template slot-scope="scope">
                        <el-progress :percentage="scope.row.videoCompletion || 0" :stroke-width="8"></el-progress>
                    </template>
                </el-table-column>
                <el-table-column label="作业完成率" min-width="140">
                    <template slot-scope="scope">
                        <el-progress :percentage="scope.row.homeworkCompletion || 0" :stroke-width="8" :color="scope.row.homeworkCompletion >= 80 ? '#67C23A' : '#E6A23C'"></el-progress>
                    </template>
                </el-table-column>
                <el-table-column prop="activeHours" label="活跃时长(h)" width="110" align="center"></el-table-column>
                <el-table-column prop="lastActive" label="最近活跃" width="150"></el-table-column>
            </el-table>
        </el-dialog>
    </div>
</template>

<script>
import * as teacherApi from '@/api/teacher/teacherApi'

export default {
    name: "TeacherClassManagement",
    data() {
        return {
            loading: false,
            saving: false,
            classList: [],
            subjectList: [],
            showCreateDialog: false,
            classForm: {
                subjectId: null,
                className: '',
                startTime: null,
                endTime: null
            },
            classRules: {
                subjectId: [{ required: true, message: '请选择课程', trigger: 'change' }],
                className: [{ required: true, message: '请输入班级名称', trigger: 'blur' }]
            },
            // 学生管理
            showStudentDialog: false,
            currentClassId: null,
            currentClassName: '',
            studentLoading: false,
            studentList: [],
            studentSearch: '',
            // 批量导入
            showImportDialog: false,
            importStudentsText: '',
            // 学情监控
            showMonitorDialog: false,
            monitorLoading: false,
            monitorStats: [
                { label: '平均视频完成率', value: '0%', color: '#409EFF' },
                { label: '平均作业完成率', value: '0%', color: '#67C23A' },
                { label: '总活跃时长', value: '0h', color: '#E6A23C' }
            ],
            monitorStudents: []
        }
    },
    computed: {
        filteredStudents() {
            if (!this.studentSearch) return this.studentList
            return this.studentList.filter(s =>
                (s.realName || '').includes(this.studentSearch) ||
                (s.username || '').includes(this.studentSearch)
            )
        }
    },
    created() {
        this.loadClasses()
        this.loadSubjects()
    },
    methods: {
        async loadClasses() {
            this.loading = true
            try {
                const res = await teacherApi.getMyClasses({})
                this.classList = (res.data && res.data.list) ? res.data.list : []
            } catch (e) {
                console.error(e)
                this.classList = []
            }
            this.loading = false
        },
        async loadSubjects() {
            try {
                const res = await teacherApi.getSubjects()
                this.subjectList = (res.data && res.data.list) ? res.data.list : []
            } catch (e) {
                this.subjectList = []
            }
        },
        createClass() {
            this.$refs.classForm.validate(async valid => {
                if (!valid) return
                this.saving = true
                try {
                    await teacherApi.createClass(this.classForm)
                    this.$message.success('创建成功')
                    this.showCreateDialog = false
                    this.classForm = { subjectId: null, className: '', startTime: null, endTime: null }
                    this.loadClasses()
                } catch (e) {
                    this.$message.error('创建失败')
                }
                this.saving = false
            })
        },
        deleteClass(row) {
            this.$confirm(`确定删除班级"${row.className}"吗？`, '提示', { type: 'warning' })
                .then(async () => {
                    try {
                        await teacherApi.deleteClass({ id: row.id })
                        this.$message.success('删除成功')
                        this.loadClasses()
                    } catch (e) {
                        this.$message.error('删除失败')
                    }
                }).catch(() => {})
        },
        async manageStudents(row) {
            this.currentClassId = row.id
            this.currentClassName = row.className
            this.showStudentDialog = true
            this.studentLoading = true
            try {
                const res = await teacherApi.getClassStudents({ classId: row.id })
                this.studentList = (res.data && res.data.list) ? res.data.list : []
            } catch (e) {
                this.studentList = []
            }
            this.studentLoading = false
        },
        removeStudent(row) {
            this.$confirm(`确定将学生"${row.realName}"移出班级吗？`, '提示', { type: 'warning' })
                .then(async () => {
                    try {
                        await teacherApi.removeStudent({ studentId: row.id, classId: this.currentClassId })
                        this.$message.success('已移出')
                        this.manageStudents({ id: this.currentClassId, className: this.currentClassName })
                    } catch (e) {
                        this.$message.error('操作失败')
                    }
                }).catch(() => {})
        },
        doBatchImport() {
            if (!this.importStudentsText.trim()) {
                this.$message.warning('请输入学生学号')
                return
            }
            const ids = this.importStudentsText.split('\n').filter(s => s.trim()).map(s => s.trim())
            teacherApi.batchAddStudents({ classId: this.currentClassId, studentIds: ids })
                .then(() => {
                    this.$message.success(`成功导入 ${ids.length} 名学生`)
                    this.showImportDialog = false
                    this.importStudentsText = ''
                    this.manageStudents({ id: this.currentClassId, className: this.currentClassName })
                }).catch(() => {
                    this.$message.error('导入失败')
                })
        },
        async viewMonitor(row) {
            this.currentClassId = row.id
            this.currentClassName = row.className
            this.showMonitorDialog = true
            this.monitorLoading = true
            try {
                const res = await teacherApi.getStudentLearningStatus({ classId: row.id })
                if (res.data && res.data.list) {
                    this.monitorStudents = res.data.list
                    // 更新统计
                    const total = this.monitorStudents.length
                    if (total > 0) {
                        this.monitorStats[0].value = Math.round(this.monitorStudents.reduce((s, st) => s + (st.videoCompletion || 0), 0) / total) + '%'
                        this.monitorStats[1].value = Math.round(this.monitorStudents.reduce((s, st) => s + (st.homeworkCompletion || 0), 0) / total) + '%'
                        const hours = this.monitorStudents.reduce((s, st) => s + (st.activeHours || 0), 0)
                        this.monitorStats[2].value = hours.toFixed(1) + 'h'
                    }
                } else {
                    this.monitorStudents = []
                }
            } catch (e) {
                this.monitorStudents = []
            }
            this.monitorLoading = false
        }
    }
}
</script>

<style scoped>
.teacher-class-mgmt {
    padding: 24px;
    background: #f5f7fa;
    min-height: 100vh;
}
.page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}
.page-header h2 {
    margin: 0;
    font-size: 22px;
    color: #303133;
}
.page-header h2 i { margin-right: 8px; color: #409EFF; }
.main-card { border-radius: 8px; }
.class-cell { display: flex; align-items: center; }
.class-name-text { font-weight: 600; color: #303133; }
.student-mgmt-header { display: flex; align-items: center; }
.import-hint p { margin: 0; font-size: 14px; }
.monitor-summary { margin-bottom: 8px; }
.monitor-stat-card {
    background: #f5f7fa;
    border-radius: 6px;
    padding: 14px;
    text-align: center;
}
.monitor-stat-label { margin: 0 0 6px; font-size: 13px; color: #909399; }
.monitor-stat-value { margin: 0; font-size: 22px; font-weight: 700; }
</style>