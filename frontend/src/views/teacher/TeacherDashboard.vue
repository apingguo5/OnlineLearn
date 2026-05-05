<template>
    <div class="teacher-dashboard">
        <!-- 页面头部 -->
        <div class="page-header">
            <h2>教师工作台</h2>
            <div class="header-tabs">
                <el-tabs v-model="activeTab">
                    <el-tab-pane label="班级学生管理" name="students">
                        <el-button type="primary" size="small" icon="el-icon-plus" @click="showAddStudentDialog">
                            添加学生到班级
                        </el-button>
                    </el-tab-pane>
                    <el-tab-pane label="作业批改" name="homework"></el-tab-pane>
                </el-tabs>
            </div>
        </div>

        <!-- ======================== 班级学生管理 ======================== -->
        <div v-show="activeTab === 'students'" class="tab-content">
            <!-- 班级选择器（仅显示我负责的班级） -->
            <div class="section-bar">
                <span class="section-title">选择班级</span>
                <el-select v-model="selectedClassId" placeholder="请选择班级" @change="onClassChange" style="width: 260px;">
                    <el-option
                            v-for="item in classList"
                            :key="item.id"
                            :label="item.className"
                            :value="item.id">
                    </el-option>
                </el-select>
            </div>

            <!-- 学生列表 -->
            <div v-if="selectedClassId" class="student-list-section">
                <div class="section-bar">
                    <span class="section-title">班级学生列表</span>
                    <span class="student-count">共 {{ studentsTotal }} 人</span>
                    <el-button type="danger" size="mini" icon="el-icon-delete" @click="showBatchRemoveDialog"
                               :disabled="multipleSelection.length === 0">
                        批量移出
                    </el-button>
                </div>
                <el-table
                        :data="studentsData"
                        border
                        stripe
                        style="width: 100%"
                        @selection-change="onSelectionChange">
                    <el-table-column type="selection" width="55"></el-table-column>
                    <el-table-column prop="id" label="ID" width="80"></el-table-column>
                    <el-table-column prop="account" label="账号" width="150"></el-table-column>
                    <el-table-column prop="userName" label="姓名" width="150"></el-table-column>
                    <el-table-column prop="phone" label="电话" width="150"></el-table-column>
                    <el-table-column label="操作" width="150">
                        <template slot-scope="scope">
                            <el-button type="danger" size="mini" icon="el-icon-remove-outline"
                                       @click="handleRemoveStudent(scope.row)">移出</el-button>
                        </template>
                    </el-table-column>
                </el-table>
                <el-pagination
                        background
                        layout="prev, pager, next"
                        :total="studentsTotal"
                        :page-size="studentPageSize"
                        @current-change="onStudentPageChange">
                </el-pagination>
            </div>
            <div v-else class="empty-hint">
                <i class="el-icon-info"></i> 请先选择一个班级
            </div>
        </div>

        <!-- ======================== 作业批改 ======================== -->
        <div v-show="activeTab === 'homework'" class="tab-content">
            <!-- 班级选择（仅显示我负责的班级） -->
            <div class="section-bar">
                <span class="section-title">选择班级</span>
                <el-select v-model="hwClassId" placeholder="请选择班级" @change="onHwClassChange" style="width: 260px;">
                    <el-option
                            v-for="item in classList"
                            :key="item.id"
                            :label="item.className"
                            :value="item.id">
                    </el-option>
                </el-select>
            </div>

            <div v-if="hwClassId">
                <!-- 作业列表 -->
                <div class="section-bar">
                    <span class="section-title">作业列表</span>
                </div>
                <el-table :data="homeworkList" border stripe style="width: 100%">
                    <el-table-column prop="id" label="ID" width="80"></el-table-column>
                    <el-table-column prop="title" label="作业标题" width="200"></el-table-column>
                    <el-table-column label="操作" width="200">
                        <template slot-scope="scope">
                            <el-button type="primary" size="mini" icon="el-icon-edit"
                                       @click="showSubmissions(scope.row)">查看提交</el-button>
                        </template>
                    </el-table-column>
                </el-table>

                <!-- 提交列表 -->
                <div v-if="selectedHomework" class="submissions-section">
                    <div class="section-bar" style="margin-top: 20px;">
                        <span class="section-title">《{{ selectedHomework.title }}》提交列表</span>
                        <span class="student-count">共 {{ submissionsTotal }} 份</span>
                    </div>
                    <el-table :data="submissionsData" border stripe style="width: 100%">
                        <el-table-column prop="studentName" label="学生姓名" width="120"></el-table-column>
                        <el-table-column prop="studentAccount" label="学生账号" width="120"></el-table-column>
                        <el-table-column label="提交内容" min-width="200">
                            <template slot-scope="scope">
                                <el-popover placement="bottom" width="400" trigger="click">
                                    <div style="max-height: 300px; overflow-y: auto; white-space: pre-wrap;">{{ scope.row.reply }}</div>
                                    <el-button slot="reference" type="text">{{ scope.row.reply ? scope.row.reply.substring(0, 30) + '...' : '无内容' }}</el-button>
                                </el-popover>
                            </template>
                        </el-table-column>
                        <el-table-column label="提交时间" width="180">
                            <template slot-scope="scope">{{ formatDate(scope.row.completionTime) }}</template>
                        </el-table-column>
                        <el-table-column label="当前评分" width="100">
                            <template slot-scope="scope">
                                <span :style="{ color: scope.row.score ? '#67C23A' : '#999' }">
                                    {{ scope.row.score != null ? scope.row.score : '未批改' }}
                                </span>
                            </template>
                        </el-table-column>
                        <el-table-column label="评语" width="200">
                            <template slot-scope="scope">{{ scope.row.remark || '无' }}</template>
                        </el-table-column>
                        <el-table-column label="操作" width="160" fixed="right">
                            <template slot-scope="scope">
                                <el-button type="warning" size="mini" icon="el-icon-edit-outline"
                                           @click="openGradeDialog(scope.row)">批改</el-button>
                            </template>
                        </el-table-column>
                    </el-table>
                    <el-pagination
                            background
                            layout="prev, pager, next"
                            :total="submissionsTotal"
                            :page-size="submissionPageSize"
                            @current-change="onSubmissionPageChange">
                    </el-pagination>
                </div>
            </div>
            <div v-else class="empty-hint">
                <i class="el-icon-info"></i> 请先选择一个班级
            </div>
        </div>

        <!-- ======================== 添加学生对话框 ======================== -->
        <el-dialog title="添加学生到班级" :visible.sync="addDialogVisible" width="700px">
            <div class="dialog-body">
                <div class="dialog-section">
                    <label>选择目标班级（我负责的班级）：</label>
                    <el-select v-model="addClassId" placeholder="请选择班级" style="width: 100%;">
                        <el-option v-for="item in classList" :key="item.id" :label="item.className" :value="item.id"></el-option>
                    </el-select>
                </div>
                <div class="dialog-section">
                    <label>搜索学生：</label>
                    <el-input v-model="searchKeyword" placeholder="输入学生账号或姓名搜索" style="width: 300px; margin-right: 10px;"
                              @keyup.enter.native="doSearchStudent"></el-input>
                    <el-button type="primary" icon="el-icon-search" @click="doSearchStudent">搜索</el-button>
                </div>
                <div v-if="searchResults.length > 0" class="search-results">
                    <el-table :data="searchResults" border stripe @selection-change="onSearchSelectionChange">
                        <el-table-column type="selection" width="55"></el-table-column>
                        <el-table-column prop="id" label="ID" width="80"></el-table-column>
                        <el-table-column prop="account" label="账号" width="150"></el-table-column>
                        <el-table-column prop="userName" label="姓名" width="150"></el-table-column>
                        <el-table-column prop="phone" label="电话" width="150"></el-table-column>
                    </el-table>
                    <div style="margin-top: 10px; display: flex; justify-content: space-between; align-items: center;">
                        <span>已选 {{ searchSelection.length }} 名学生</span>
                        <div>
                            <el-button type="success" icon="el-icon-plus" @click="handleAddSelected"
                                       :disabled="searchSelection.length === 0 || !addClassId">
                                添加选中学生
                            </el-button>
                        </div>
                    </div>
                </div>
                <div v-else class="empty-hint" style="margin-top: 20px;">
                    <i class="el-icon-search"></i> 搜索后显示学生列表
                </div>
            </div>
        </el-dialog>

        <!-- ======================== 批改作业对话框 ======================== -->
        <el-dialog title="批改作业" :visible.sync="gradeDialogVisible" width="500px">
            <div class="dialog-body">
                <div class="dialog-section">
                    <label>学生：</label>
                    <span>{{ currentGradingItem.studentName }}</span>
                </div>
                <div class="dialog-section">
                    <label>提交内容：</label>
                    <div class="reply-content">{{ currentGradingItem.reply }}</div>
                </div>
                <div class="dialog-section">
                    <label>评分（满分100）：</label>
                    <el-input-number v-model="gradeScore" :min="0" :max="100" :precision="1" style="width: 200px;"></el-input-number>
                </div>
                <div class="dialog-section">
                    <label>评语：</label>
                    <el-input type="textarea" v-model="gradeRemark" rows="3" placeholder="输入评语（可选）"></el-input>
                </div>
            </div>
            <span slot="footer">
                <el-button @click="gradeDialogVisible = false">取消</el-button>
                <el-button type="primary" @click="submitGrade">提交批改</el-button>
            </span>
        </el-dialog>
    </div>
</template>

<script>
    import Cookies from 'js-cookie'
    import {
        searchStudent,
        addStudentToClass,
        batchAddStudents,
        removeStudent,
        getClassStudents,
        gradeHomework,
        getHomeworkSubmissions
    } from '@/api/teacher/dashboard'

    export default {
        name: "TeacherDashboard",
        data() {
            return {
                // 标签页
                activeTab: 'students',

                // 班级列表（仅显示我负责的班级）
                classList: [],

                // ---- 学生管理 ----
                selectedClassId: null,
                studentsData: [],
                studentsTotal: 0,
                studentPage: 1,
                studentPageSize: 10,
                multipleSelection: [],

                // 添加学生对话框
                addDialogVisible: false,
                addClassId: null,
                searchKeyword: '',
                searchResults: [],
                searchSelection: [],

                // ---- 作业批改 ----
                hwClassId: null,
                homeworkList: [],
                selectedHomework: null,
                submissionsData: [],
                submissionsTotal: 0,
                submissionPage: 1,
                submissionPageSize: 10,

                // 批改对话框
                gradeDialogVisible: false,
                currentGradingItem: {},
                gradeScore: 0,
                gradeRemark: '',
            }
        },
        created() {
            this.loadClassList()
        },
        methods: {
            // 加载当前教师负责的班级列表
            loadClassList() {
                // 从 Cookies 获取当前登录用户的 userId（登录时已设置）
                const userId = Cookies.get('userId')
                if (!userId) {
                    this.$message.error('未获取到用户信息，请重新登录')
                    return
                }
                this.$axios.post('/study/class/findList', { userId: parseInt(userId) }).then(res => {
                    if (res.data && res.data.code === 200) {
                        this.classList = res.data.resultData || []
                    } else {
                        this.classList = []
                    }
                }).catch(() => {
                    this.$message.error('获取班级列表失败')
                })
            },

            // ---- 学生管理 ----
            onClassChange() {
                this.studentPage = 1
                this.loadStudents()
            },
            loadStudents() {
                if (!this.selectedClassId) return
                getClassStudents({
                    classId: this.selectedClassId,
                    page: this.studentPage,
                    pageSize: this.studentPageSize
                }).then(res => {
                    if (res.data && res.data.code === 200) {
                        const data = res.data.resultData
                        this.studentsData = data.data || []
                        this.studentsTotal = data.total || 0
                    }
                }).catch(() => {
                    this.$message.error('获取学生列表失败')
                })
            },
            onStudentPageChange(page) {
                this.studentPage = page
                this.loadStudents()
            },
            onSelectionChange(val) {
                this.multipleSelection = val
            },

            // 添加学生
            showAddStudentDialog() {
                this.addDialogVisible = true
                this.searchKeyword = ''
                this.searchResults = []
                this.searchSelection = []
                // 默认选中当前管理的班级，若没有则从列表中选第一个
                this.addClassId = this.selectedClassId || (this.classList.length > 0 ? this.classList[0].id : null)
            },
            doSearchStudent() {
                if (!this.searchKeyword.trim()) {
                    this.$message.warning('请输入搜索关键词')
                    return
                }
                searchStudent({ keyword: this.searchKeyword }).then(res => {
                    if (res.data && res.data.code === 200) {
                        const data = res.data.resultData
                        this.searchResults = data.data || []
                    }
                }).catch(() => {
                    this.$message.error('搜索失败')
                })
            },
            onSearchSelectionChange(val) {
                this.searchSelection = val
            },
            handleAddSelected() {
                if (!this.addClassId) {
                    this.$message.warning('请选择目标班级')
                    return
                }
                if (this.searchSelection.length === 0) {
                    this.$message.warning('请选择要添加的学生')
                    return
                }
                const studentIds = this.searchSelection.map(s => s.id)
                batchAddStudents({
                    studentIds: studentIds,
                    classId: this.addClassId
                }).then(res => {
                    if (res.data && res.data.code === 200) {
                        this.$message.success(`成功添加 ${res.data.resultData.successCount || studentIds.length} 名学生`)
                        this.addDialogVisible = false
                        // 如果当前管理的是同一个班级，刷新列表
                        if (this.selectedClassId === this.addClassId) {
                            this.loadStudents()
                        }
                    } else {
                        this.$message.error(res.data.resultData || '添加失败')
                    }
                }).catch(() => {
                    this.$message.error('添加失败')
                })
            },

            // 移出学生
            handleRemoveStudent(row) {
                this.$confirm(`确定将学生「${row.userName}」从班级移出？`, '确认', {
                    type: 'warning'
                }).then(() => {
                    removeStudent({
                        studentId: row.id,
                        classId: this.selectedClassId
                    }).then(res => {
                        if (res.data && res.data.code === 200) {
                            this.$message.success('移出成功')
                            this.loadStudents()
                        } else {
                            this.$message.error(res.data.resultData || '移出失败')
                        }
                    })
                }).catch(() => {})
            },
            showBatchRemoveDialog() {
                if (this.multipleSelection.length === 0) return
                this.$confirm(`确定批量移出 ${this.multipleSelection.length} 名学生？`, '确认', {
                    type: 'warning'
                }).then(() => {
                    const promises = this.multipleSelection.map(s =>
                        removeStudent({ studentId: s.id, classId: this.selectedClassId })
                    )
                    Promise.all(promises).then(() => {
                        this.$message.success('批量移出完成')
                        this.loadStudents()
                    }).catch(() => {
                        this.$message.error('批量移出失败')
                    })
                }).catch(() => {})
            },

            // ---- 作业批改 ----
            onHwClassChange() {
                this.selectedHomework = null
                this.submissionsData = []
                this.loadHomeworkList()
            },
            loadHomeworkList() {
                if (!this.hwClassId) return
                this.$axios.post('/study/homework/list', {
                    classId: this.hwClassId,
                    page: 1,
                    pageSize: 100
                }).then(res => {
                    if (res.data && res.data.code === 200) {
                        const pageData = res.data.resultData
                        this.homeworkList = pageData.list || []
                    }
                }).catch(() => {
                    this.$message.error('获取作业列表失败')
                })
            },
            showSubmissions(row) {
                this.selectedHomework = row
                this.submissionPage = 1
                this.loadSubmissions()
            },
            loadSubmissions() {
                if (!this.selectedHomework) return
                getHomeworkSubmissions({
                    homeworkId: this.selectedHomework.id,
                    page: this.submissionPage,
                    pageSize: this.submissionPageSize
                }).then(res => {
                    if (res.data && res.data.code === 200) {
                        const data = res.data.resultData
                        this.submissionsData = data.data || []
                        this.submissionsTotal = data.total || 0
                    }
                }).catch(() => {
                    this.$message.error('获取提交列表失败')
                })
            },
            onSubmissionPageChange(page) {
                this.submissionPage = page
                this.loadSubmissions()
            },

            // 批改作业
            openGradeDialog(row) {
                this.currentGradingItem = row
                this.gradeScore = row.score != null ? row.score : 0
                this.gradeRemark = row.remark || ''
                this.gradeDialogVisible = true
            },
            submitGrade() {
                gradeHomework({
                    id: this.currentGradingItem.id,
                    score: this.gradeScore,
                    remark: this.gradeRemark
                }).then(res => {
                    if (res.data && res.data.code === 200) {
                        this.$message.success('批改成功')
                        this.gradeDialogVisible = false
                        this.loadSubmissions()
                    } else {
                        this.$message.error(res.data.resultData || '批改失败')
                    }
                }).catch(() => {
                    this.$message.error('批改失败')
                })
            },

            // 工具
            formatDate(dateStr) {
                if (!dateStr) return ''
                const d = new Date(dateStr)
                const pad = n => n.toString().padStart(2, '0')
                return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
            }
        }
    }
</script>

<style scoped>
.teacher-dashboard {
    padding: 20px;
    background: #f5f7fa;
    min-height: calc(100vh - 60px);
}

.page-header {
    background: #fff;
    padding: 15px 20px;
    border-radius: 6px;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
    margin-bottom: 20px;
}

.page-header h2 {
    margin: 0 0 10px 0;
    font-size: 20px;
    color: #303133;
}

.tab-content {
    background: #fff;
    padding: 20px;
    border-radius: 6px;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.section-bar {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 16px;
    flex-wrap: wrap;
}

.section-title {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    margin-right: 10px;
}

.student-count {
    color: #909399;
    font-size: 13px;
    margin-right: auto;
}

.student-list-section {
    margin-top: 10px;
}

.empty-hint {
    text-align: center;
    color: #909399;
    padding: 40px 0;
    font-size: 14px;
}

.empty-hint i {
    font-size: 20px;
    vertical-align: middle;
    margin-right: 6px;
}

/* 对话框样式 */
.dialog-body {
    padding: 0 10px;
}

.dialog-section {
    margin-bottom: 16px;
}

.dialog-section label {
    display: block;
    font-weight: 600;
    color: #606266;
    margin-bottom: 6px;
}

.reply-content {
    background: #f5f7fa;
    padding: 12px;
    border-radius: 4px;
    white-space: pre-wrap;
    max-height: 200px;
    overflow-y: auto;
    font-size: 14px;
    line-height: 1.6;
    border: 1px solid #e4e7ed;
}

.search-results {
    margin-top: 16px;
}
</style>