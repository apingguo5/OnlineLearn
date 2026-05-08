<template>
    <div class="teacher-course-mgmt">
        <div class="page-header">
            <div class="page-title">
                <i class="el-icon-reading"></i>
                <span>课程管理</span>
            </div>
            <div class="page-actions">
                <el-button type="primary" icon="el-icon-plus" @click="dialogVisible = true">创建课程</el-button>
            </div>
        </div>

        <!-- 统计卡片 -->
        <el-row :gutter="20" class="stats-row">
            <el-col :span="8">
                <el-card shadow="hover" class="stat-card">
                    <div class="stat-content">
                        <div class="stat-info">
                            <p class="stat-label">课程总数</p>
                            <p class="stat-value" style="color: #409EFF">{{ courseList.length }}</p>
                        </div>
                        <div class="stat-icon" style="background: #ecf5ff">
                            <i class="el-icon-reading" style="color: #409EFF"></i>
                        </div>
                    </div>
                </el-card>
            </el-col>
            <el-col :span="8">
                <el-card shadow="hover" class="stat-card">
                    <div class="stat-content">
                        <div class="stat-info">
                            <p class="stat-label">所属班级</p>
                            <p class="stat-value" style="color: #67C23A">{{ totalClasses }}</p>
                        </div>
                        <div class="stat-icon" style="background: #f0f9eb">
                            <i class="el-icon-s-grid" style="color: #67C23A"></i>
                        </div>
                    </div>
                </el-card>
            </el-col>
            <el-col :span="8">
                <el-card shadow="hover" class="stat-card">
                    <div class="stat-content">
                        <div class="stat-info">
                            <p class="stat-label">关联学生</p>
                            <p class="stat-value" style="color: #E6A23C">{{ totalStudents }}</p>
                        </div>
                        <div class="stat-icon" style="background: #fdf6ec">
                            <i class="el-icon-user-solid" style="color: #E6A23C"></i>
                        </div>
                    </div>
                </el-card>
            </el-col>
        </el-row>

        <!-- 课程列表 -->
        <el-card shadow="never" class="course-card">
            <div slot="header" class="card-header">
                <span>所有课程</span>
                <el-input
                    v-model="searchKeyword"
                    placeholder="搜索课程名称..."
                    prefix-icon="el-icon-search"
                    size="small"
                    style="width: 240px"
                    clearable
                />
            </div>

            <div v-if="filteredCourses.length === 0" class="empty-state">
                <i class="el-icon-folder-opened"></i>
                <p>暂无课程数据，点击上方按钮创建课程</p>
            </div>

            <el-table v-else :data="filteredCourses" stripe style="width: 100%">
                <el-table-column prop="courseName" label="课程名称" min-width="180">
                    <template slot-scope="scope">
                        <div class="course-name-cell">
                            <span class="course-avatar" :style="{ background: scope.row._color }">
                                {{ (scope.row.courseName || '?').charAt(0) }}
                            </span>
                            <span class="course-name-text">{{ scope.row.courseName }}</span>
                        </div>
                    </template>
                </el-table-column>
                <el-table-column prop="description" label="课程描述" min-width="200" show-overflow-tooltip />
                <el-table-column label="创建时间" width="160" prop="createTime">
                    <template slot-scope="scope">
                        {{ scope.row.createTime ? formatDate(scope.row.createTime) : '-' }}
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="200" fixed="right">
                    <template slot-scope="scope">
                        <el-button type="primary" size="mini" icon="el-icon-edit" @click="editCourse(scope.row)">编辑</el-button>
                        <el-button type="danger" size="mini" icon="el-icon-delete" @click="handleDelete(scope.row)">删除</el-button>
                    </template>
                </el-table-column>
            </el-table>
        </el-card>

        <!-- 创建课程对话框 -->
        <el-dialog
            title="创建新课程"
            :visible.sync="dialogVisible"
            width="500px"
            :close-on-click-modal="false"
        >
            <el-form :model="courseForm" :rules="formRules" ref="courseForm" label-width="100px">
                <el-form-item label="课程名称" prop="courseName">
                    <el-input v-model="courseForm.courseName" placeholder="请输入课程名称" maxlength="100" show-word-limit />
                </el-form-item>
                <el-form-item label="课程描述" prop="description">
                    <el-input
                        v-model="courseForm.description"
                        type="textarea"
                        :rows="3"
                        placeholder="请输入课程描述（可选）"
                        maxlength="500"
                        show-word-limit
                    />
                </el-form-item>
            </el-form>
            <span slot="footer" class="dialog-footer">
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button type="primary" :loading="submitting" @click="submitCreate">确认创建</el-button>
            </span>
        </el-dialog>
    </div>
</template>

<script>
import teacherApi from '@/api/teacher/teacherApi'
import Cookies from 'js-cookie'

export default {
    name: "TeacherCourseManagement",
    data() {
        return {
            courseList: [],
            searchKeyword: '',
            dialogVisible: false,
            submitting: false,
            courseForm: {
                courseName: '',
                description: ''
            },
            formRules: {
                courseName: [
                    { required: true, message: '请输入课程名称', trigger: 'blur' },
                    { min: 1, max: 100, message: '长度在 1 到 100 个字符', trigger: 'blur' }
                ]
            },
            totalClasses: 0,
            totalStudents: 0
        }
    },
    computed: {
        filteredCourses() {
            const keyword = this.searchKeyword.trim().toLowerCase()
            if (!keyword) return this.courseList
            return this.courseList.filter(c =>
                (c.courseName || '').toLowerCase().includes(keyword)
            )
        }
    },
    created() {
        this.loadCourses()
    },
    methods: {
        formatDate(dateStr) {
            if (!dateStr) return '-'
            const d = new Date(dateStr)
            const year = d.getFullYear()
            const month = String(d.getMonth() + 1).padStart(2, '0')
            const day = String(d.getDate()).padStart(2, '0')
            const hour = String(d.getHours()).padStart(2, '0')
            const min = String(d.getMinutes()).padStart(2, '0')
            return `${year}-${month}-${day} ${hour}:${min}`
        },
        getUserId() {
            // 登录时存储在 localStorage：userId, roleId, userName
            let uid = localStorage.getItem('userId')
            if (uid) return uid
            // 后备：从 Cookie 读取
            uid = Cookies.get('userId')
            if (uid) return uid
            // 后备：从 user JSON 对象读取
            try {
                const userStr = sessionStorage.getItem('user') || localStorage.getItem('user')
                if (userStr) {
                    const user = JSON.parse(userStr)
                    return user.id || user.userId
                }
            } catch (e) {}
            return null
        },
        async loadCourses() {
            try {
                const userId = this.getUserId()
                if (!userId) return

                const res = await teacherApi.getMyCourses(userId)
                const body = res.data
                if (body.code === 200) {
                    let courseList = body.resultData || []
                    if (!Array.isArray(courseList)) {
                        courseList = []
                    }
                    const colors = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#9B59B6', '#1ABC9C', '#3498DB']
                    this.courseList = courseList.map((c, idx) => ({
                        ...c,
                        _color: colors[idx % colors.length]
                    }))
                } else {
                    console.warn('加载课程列表失败，返回码:', body.code)
                }
            } catch (e) {
                console.error('加载课程列表失败', e)
                this.$message.error('加载课程列表失败')
            }
        },
        submitCreate() {
            this.$refs.courseForm.validate(async (valid) => {
                if (!valid) return
                this.submitting = true
                try {
                    const userId = this.getUserId()
                    if (!userId) {
                        this.$message.error('用户未登录，请重新登录')
                        this.submitting = false
                        return
                    }
                    const params = {
                        courseName: this.courseForm.courseName.trim(),
                        userId: userId
                    }
                    if (this.courseForm.description) {
                        params.description = this.courseForm.description.trim()
                    }
                    const res = await teacherApi.createCourse(params)
                    const body = res.data
                    if (body.code === 200) {
                        this.$message.success('课程创建成功')
                        this.dialogVisible = false
                        this.courseForm = { courseName: '', description: '' }
                        await this.loadCourses()
                    } else {
                        this.$message.error(body.resultData || '创建失败')
                    }
                } catch (e) {
                    console.error('创建课程失败', e)
                    this.$message.error('创建课程失败')
                } finally {
                    this.submitting = false
                }
            })
        },
        handleDelete(row) {
            this.$confirm(`确定要删除课程「${row.courseName}」吗？此操作不可恢复。`, '确认删除', {
                confirmButtonText: '确定删除',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(async () => {
                try {
                    const res = await teacherApi.deleteCourse(row.id)
                    const body = res.data
                    if (body.code === 200) {
                        this.$message.success('课程已删除')
                        await this.loadCourses()
                    } else {
                        this.$message.error(body.resultData || '删除失败')
                    }
                } catch (e) {
                    console.error('删除课程失败', e)
                    this.$message.error('删除课程失败')
                }
            }).catch(() => {})
        },
        editCourse(row) {
            this.$message.info('课程编辑功能正在开发中')
        }
    }
}
</script>

<style scoped>
.teacher-course-mgmt {
    padding: 24px;
    background: #f5f7fa;
    min-height: 100vh;
}

.page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
}

.page-title {
    font-size: 22px;
    font-weight: 600;
    color: #303133;
}

.page-title i {
    margin-right: 8px;
    color: #409EFF;
}

.stats-row {
    margin-bottom: 20px;
}

.stat-card {
    border-radius: 8px;
}

.stat-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.stat-info {
    flex: 1;
}

.stat-label {
    margin: 0 0 6px;
    font-size: 14px;
    color: #909399;
}

.stat-value {
    margin: 0;
    font-size: 28px;
    font-weight: 700;
}

.stat-icon {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
}

.course-card {
    border-radius: 8px;
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.course-name-cell {
    display: flex;
    align-items: center;
    gap: 10px;
}

.course-avatar {
    width: 36px;
    height: 36px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-weight: 700;
    font-size: 16px;
    flex-shrink: 0;
}

.course-name-text {
    font-weight: 500;
    color: #303133;
}

.empty-state {
    text-align: center;
    padding: 60px 0;
    color: #c0c4cc;
}

.empty-state i {
    font-size: 48px;
    margin-bottom: 16px;
}

.empty-state p {
    font-size: 14px;
    color: #909399;
}
</style>