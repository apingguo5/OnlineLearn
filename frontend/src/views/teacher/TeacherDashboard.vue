<template>
    <div class="teacher-dashboard">
        <!-- 顶部问候 -->
        <div class="welcome-header">
            <div class="welcome-text">
                <h1>欢迎回来，{{ teacherName }}</h1>
                <p class="welcome-subtitle">{{ currentDate }} · 今天是充实教学的一天</p>
            </div>
            <div class="quick-actions">
                <el-button type="primary" icon="el-icon-plus" @click="openCreateCourse">创建课程</el-button>
                <el-button type="warning" icon="el-icon-edit" @click="openPublishHomework">发布作业</el-button>
                <el-button type="info" icon="el-icon-bell" @click="openSendNotice">发布通知</el-button>
            </div>
        </div>

        <!-- 统计卡片 -->
        <el-row :gutter="20" class="stats-row">
            <el-col :span="6" v-for="stat in statistics" :key="stat.label">
                <el-card shadow="hover" class="stat-card" :style="{ borderLeft: `4px solid ${stat.color}` }">
                    <div class="stat-content">
                        <div class="stat-info">
                            <p class="stat-label">{{ stat.label }}</p>
                            <p class="stat-value" :style="{ color: stat.color }">{{ stat.value }}</p>
                        </div>
                        <div class="stat-icon" :style="{ background: stat.bgColor }">
                            <i :class="stat.icon" :style="{ color: stat.color }"></i>
                        </div>
                    </div>
                    <div class="stat-footer">
                        <span>{{ stat.trend }}</span>
                        <i :class="stat.trendIcon" :style="{ color: stat.trendColor }"></i>
                    </div>
                </el-card>
            </el-col>
        </el-row>

        <!-- 待办事项与趋势图两栏布局 -->
        <el-row :gutter="20" class="content-row">
            <!-- 左侧：待办事项 -->
            <el-col :span="14">
                <el-card shadow="never" class="content-card">
                    <div slot="header" class="card-header">
                        <span><i class="el-icon-warning"></i> 待办提醒</span>
                        <el-button type="text" icon="el-icon-more">查看全部</el-button>
                    </div>
                    <div class="todo-list">
                        <div class="todo-item" v-for="item in pendingTasks" :key="item.id">
                            <div class="todo-icon" :class="item.type">
                                <i :class="item.icon"></i>
                            </div>
                            <div class="todo-info">
                                <p class="todo-title">{{ item.title }}</p>
                                <p class="todo-desc">{{ item.desc }}</p>
                            </div>
                            <div class="todo-action">
                                <el-tag :type="item.tagType" size="small" effect="plain">{{ item.tag }}</el-tag>
                                <el-button type="text" size="small" @click="handleTodo(item)">去处理</el-button>
                            </div>
                        </div>
                        <div class="empty-todo" v-if="pendingTasks.length === 0">
                            <i class="el-icon-success"></i>
                            <p>暂无待办事项，您已处理完所有任务</p>
                        </div>
                    </div>
                </el-card>

                <!-- 课程与班级概览 -->
                <el-card shadow="never" class="content-card">
                    <div slot="header" class="card-header">
                        <span><i class="el-icon-reading"></i> 进行中的课程</span>
                        <el-button type="text" icon="el-icon-plus" @click="openCreateCourse">创建课程</el-button>
                    </div>
                    <div class="course-list">
                        <div class="course-item" v-for="course in activeCourses" :key="course.id">
                            <div class="course-cover" :style="{ background: course.color }">
                                <span>{{ course.abbr }}</span>
                            </div>
                            <div class="course-info">
                                <p class="course-name">{{ course.name }}</p>
                                <p class="course-meta">{{ course.classCount }}个班级 · {{ course.studentCount }}名学生</p>
                            </div>
                            <el-button type="text" icon="el-icon-arrow-right" @click="goToCourse(course.id)">进入</el-button>
                        </div>
                        <div class="empty-hint" v-if="activeCourses.length === 0">
                            <i class="el-icon-folder-opened"></i>
                            <p>暂无进行中的课程，点击上方按钮创建</p>
                        </div>
                    </div>
                </el-card>
            </el-col>

            <!-- 右侧：活跃度趋势 + 班级概览 -->
            <el-col :span="10">
                <el-card shadow="never" class="content-card">
                    <div slot="header" class="card-header">
                        <span><i class="el-icon-data-line"></i> 学生活跃度趋势</span>
                        <el-radio-group v-model="trendPeriod" size="mini">
                            <el-radio-button label="week">本周</el-radio-button>
                            <el-radio-button label="month">本月</el-radio-button>
                        </el-radio-group>
                    </div>
                    <div class="trend-chart">
                        <div class="chart-placeholder">
                            <div class="bar-chart">
                                <div class="bar" v-for="(item, idx) in activityData" :key="idx"
                                    :style="{ height: item.value + '%', background: item.color }">
                                    <span class="bar-label">{{ item.value }}%</span>
                                </div>
                            </div>
                            <div class="chart-labels">
                                <span v-for="(item, idx) in activityData" :key="'l' + idx">{{ item.day }}</span>
                            </div>
                        </div>
                    </div>
                </el-card>

                <el-card shadow="never" class="content-card">
                    <div slot="header" class="card-header">
                        <span><i class="el-icon-s-grid"></i> 班级概况</span>
                        <el-button type="text" icon="el-icon-right" @click="goToClassMgmt">管理班级</el-button>
                    </div>
                    <div class="class-overview">
                        <div class="class-stat-item" v-for="cls in classOverview" :key="cls.name">
                            <div class="class-stat-header">
                                <span class="class-name">{{ cls.name }}</span>
                                <el-tag size="mini" :type="cls.status === 'active' ? 'success' : 'info'">
                                    {{ cls.status === 'active' ? '进行中' : '已结课' }}
                                </el-tag>
                            </div>
                            <el-progress :percentage="cls.completionRate" :color="cls.barColor" :stroke-width="8">
                            </el-progress>
                            <p class="class-stat-meta">{{ cls.studentCount }}人 · 完成{{ cls.completionRate }}%</p>
                        </div>
                        <div class="empty-hint" v-if="classOverview.length === 0">
                            <i class="el-icon-school"></i>
                            <p>暂无班级数据</p>
                        </div>
                    </div>
                </el-card>
            </el-col>
        </el-row>
    </div>
</template>

<script>
import * as dashboardApi from '@/api/teacher/dashboard'
import * as teacherApi from '@/api/teacher/teacherApi'

export default {
    name: "TeacherDashboard",
    data() {
        return {
            teacherName: '教师',
            currentDate: '',
            trendPeriod: 'week',
            statistics: [
                { label: '进行中课程', value: 0, icon: 'el-icon-reading', color: '#409EFF', bgColor: '#ecf5ff', trend: '较上月', trendIcon: 'el-icon-top', trendColor: '#67C23A' },
                { label: '班级总数', value: 0, icon: 'el-icon-s-grid', color: '#67C23A', bgColor: '#f0f9eb', trend: '较上月', trendIcon: 'el-icon-top', trendColor: '#67C23A' },
                { label: '学生总数', value: 0, icon: 'el-icon-user-solid', color: '#E6A23C', bgColor: '#fdf6ec', trend: '较上月', trendIcon: 'el-icon-bottom', trendColor: '#F56C6C' },
                { label: '待办事项', value: 0, icon: 'el-icon-bell', color: '#F56C6C', bgColor: '#fef0f0', trend: '待处理', trendIcon: 'el-icon-warning', trendColor: '#F56C6C' },
            ],
            pendingTasks: [],
            activeCourses: [],
            activityData: [
                { day: '一', value: 65, color: '#409EFF' },
                { day: '二', value: 72, color: '#409EFF' },
                { day: '三', value: 58, color: '#409EFF' },
                { day: '四', value: 85, color: '#67C23A' },
                { day: '五', value: 78, color: '#67C23A' },
                { day: '六', value: 45, color: '#E6A23C' },
                { day: '日', value: 30, color: '#909399' },
            ],
            classOverview: [],
        }
    },
    created() {
        this.initDate()
        this.loadData()
    },
    methods: {
        initDate() {
            const now = new Date()
            const days = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
            this.currentDate = `${now.getFullYear()}年${now.getMonth() + 1}月${now.getDate()}日 ${days[now.getDay()]}`
            const user = localStorage.getItem('user')
            if (user) {
                try {
                    const userObj = JSON.parse(user)
                    this.teacherName = userObj.realName || userObj.username || '教师'
                } catch (e) {
                    this.teacherName = '教师'
                }
            }
        },
        async loadData() {
            try {
                // 加载课程列表
                const res = await teacherApi.getMySubjects({})
                if (res.data && res.data.list) {
                    const courses = res.data.list || []
                    this.activeCourses = courses.map((c, idx) => ({
                        id: c.id || idx,
                        name: c.subjectName || c.name || '未命名课程',
                        abbr: (c.subjectName || c.name || '课').charAt(0),
                        classCount: c.classCount || 0,
                        studentCount: c.studentCount || 0,
                        color: ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#9B59B6'][idx % 6]
                    }))
                    this.statistics[0].value = this.activeCourses.length
                }

                // 统计计数（模拟或从接口获取）
                this.statistics[1].value = this.activeCourses.reduce((sum, c) => sum + (c.classCount || 0), 0) || 0
                this.statistics[2].value = this.activeCourses.reduce((sum, c) => sum + (c.studentCount || 0), 0) || 0

                // 待办任务模拟（后续对接真实接口）
                this.pendingTasks = [
                    { id: 1, title: '待批改作业', desc: '你有 5 份作业等待批改', icon: 'el-icon-document-checked', type: 'homework', tag: '5份', tagType: 'warning' },
                    { id: 2, title: '待回复提问', desc: '有 3 个学生提问等待回复', icon: 'el-icon-chat-dot-round', type: 'question', tag: '3条', tagType: 'danger' },
                    { id: 3, title: '即将截止', desc: '《高等数学》作业将于明天截止', icon: 'el-icon-time', type: 'deadline', tag: '紧急', tagType: 'danger' },
                ]
                this.statistics[3].value = this.pendingTasks.length

                // 班级概况
                try {
                    const clsRes = await teacherApi.getMyClasses({})
                    if (clsRes.data && clsRes.data.list) {
                        this.classOverview = clsRes.data.list.map(cls => ({
                            name: cls.className || cls.name || '未命名班级',
                            studentCount: cls.studentCount || 0,
                            completionRate: cls.completionRate || Math.floor(Math.random() * 40) + 30,
                            status: cls.status || 'active',
                            barColor: cls.status === 'active' ? '#409EFF' : '#909399'
                        }))
                    }
                } catch (e) {
                    console.log('班级数据加载跳过')
                }
            } catch (e) {
                console.error('加载数据失败', e)
            }
        },
        openCreateCourse() {
            this.$router.push('/teachercourse')
        },
        openPublishHomework() {
            this.$router.push('/teacher-publish')
        },
        openSendNotice() {
            this.$router.push('/teacher-notice')
        },
        handleTodo(item) {
            if (item.type === 'homework') {
                this.$router.push('/teacher-grading')
            } else if (item.type === 'question') {
                this.$router.push('/teacher-qa')
            } else {
                this.$router.push('/teachercourse')
            }
        },
        goToCourse(id) {
            this.$router.push('/teachercourse')
        },
        goToClassMgmt() {
            this.$router.push('/teacherclass')
        }
    }
}
</script>

<style scoped>
.teacher-dashboard {
    padding: 24px;
    background: #f5f7fa;
    min-height: 100vh;
}

.welcome-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    flex-wrap: wrap;
    gap: 16px;
}

.welcome-text h1 {
    margin: 0;
    font-size: 24px;
    font-weight: 600;
    color: #303133;
}

.welcome-subtitle {
    margin: 6px 0 0;
    color: #909399;
    font-size: 14px;
}

.quick-actions {
    display: flex;
    gap: 10px;
}

.stats-row {
    margin-bottom: 20px;
}

.stat-card {
    border-radius: 8px;
    margin-bottom: 20px;
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

.stat-footer {
    margin-top: 12px;
    font-size: 12px;
    color: #909399;
    display: flex;
    align-items: center;
    gap: 4px;
}

.content-row {
    margin-bottom: 20px;
}

.content-card {
    border-radius: 8px;
    margin-bottom: 20px;
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.card-header span i {
    margin-right: 6px;
}

/* 待办事项 */
.todo-list {
    padding: 0;
}

.todo-item {
    display: flex;
    align-items: center;
    padding: 12px 0;
    border-bottom: 1px solid #f2f2f2;
}

.todo-item:last-child {
    border-bottom: none;
}

.todo-icon {
    width: 36px;
    height: 36px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 18px;
    margin-right: 12px;
    flex-shrink: 0;
}

.todo-icon.homework {
    background: #fdf6ec;
    color: #E6A23C;
}

.todo-icon.question {
    background: #fef0f0;
    color: #F56C6C;
}

.todo-icon.deadline {
    background: #f0f9eb;
    color: #67C23A;
}

.todo-info {
    flex: 1;
}

.todo-title {
    margin: 0 0 2px;
    font-weight: 600;
    font-size: 14px;
    color: #303133;
}

.todo-desc {
    margin: 0;
    font-size: 12px;
    color: #909399;
}

.todo-action {
    display: flex;
    align-items: center;
    gap: 8px;
}

.empty-todo {
    text-align: center;
    padding: 32px 0;
    color: #67C23A;
}

.empty-todo i {
    font-size: 36px;
}

.empty-todo p {
    margin: 8px 0 0;
    color: #909399;
}

/* 课程列表 */
.course-list {
    padding: 0;
}

.course-item {
    display: flex;
    align-items: center;
    padding: 12px 0;
    border-bottom: 1px solid #f2f2f2;
}

.course-item:last-child {
    border-bottom: none;
}

.course-cover {
    width: 44px;
    height: 44px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-weight: 700;
    font-size: 18px;
    margin-right: 12px;
    flex-shrink: 0;
}

.course-info {
    flex: 1;
}

.course-name {
    margin: 0 0 2px;
    font-weight: 600;
    font-size: 14px;
    color: #303133;
}

.course-meta {
    margin: 0;
    font-size: 12px;
    color: #909399;
}

.empty-hint {
    text-align: center;
    padding: 24px 0;
    color: #c0c4cc;
}

.empty-hint i {
    font-size: 28px;
}

.empty-hint p {
    margin: 6px 0 0;
    font-size: 13px;
}

/* 柱状图 */
.trend-chart {
    padding: 8px 0;
}

.chart-placeholder {
    width: 100%;
}

.bar-chart {
    display: flex;
    align-items: flex-end;
    justify-content: space-around;
    height: 160px;
    padding: 0 4px;
    border-bottom: 1px solid #ebeef5;
    margin-bottom: 8px;
}

.bar {
    width: 32px;
    border-radius: 4px 4px 0 0;
    position: relative;
    min-height: 4px;
    transition: height 0.3s;
}

.bar-label {
    position: absolute;
    top: -20px;
    left: 50%;
    transform: translateX(-50%);
    font-size: 11px;
    color: #606266;
    white-space: nowrap;
}

.chart-labels {
    display: flex;
    justify-content: space-around;
    padding: 0 4px;
}

.chart-labels span {
    width: 32px;
    text-align: center;
    font-size: 12px;
    color: #909399;
}

/* 班级概况 */
.class-overview {
    padding: 0;
}

.class-stat-item {
    padding: 10px 0;
    border-bottom: 1px solid #f2f2f2;
}

.class-stat-item:last-child {
    border-bottom: none;
}

.class-stat-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
}

.class-name {
    font-weight: 600;
    font-size: 14px;
    color: #303133;
}

.class-stat-meta {
    margin: 6px 0 0;
    font-size: 12px;
    color: #909399;
}
</style>