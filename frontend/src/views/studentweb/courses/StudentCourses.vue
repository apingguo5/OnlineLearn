<template>
    <div>
        <div class="page-header">
            <h2><i class="el-icon-reading"></i> 所有课程</h2>
            <p>按班级分组显示您所学的全部课程</p>
        </div>

        <div v-for="group in classGroups" :key="group.classId" class="class-group">
            <div class="class-group-header">
                <el-tag type="success" size="medium">{{ group.className }}</el-tag>
            </div>
            <div class="course-list">
                <el-empty v-if="group.courses.length === 0" :image-size="80" description="该班级暂无课程"></el-empty>
                <div v-for="course in group.courses" :key="course.id" class="course-card">
                    <el-card shadow="hover">
                        <div class="course-card-content">
                            <el-image style="width: 100%; height: 140px" :src="$store.state.baseApi + course.coverUrl" fit="cover">
                                <div slot="error" class="image-placeholder">
                                    <i class="el-icon-picture-outline"></i>
                                </div>
                            </el-image>
                            <h3 class="course-title">{{ course.topic }}</h3>
                            <div class="course-meta">
                                <span><i class="el-icon-user"></i> {{ course.userName }}</span>
                            </div>
                            <router-link :to="{ path: '/detailonlineweb', query: { videoTotalId: course.id, userId: course.userId } }">
                                <el-button type="success" size="small" style="width: 100%;">进入学习</el-button>
                            </router-link>
                        </div>
                    </el-card>
                </div>
            </div>
        </div>

        <el-empty v-if="classGroups.length === 0" description="暂无课程数据"></el-empty>
    </div>
</template>

<script>
import Cookies from 'js-cookie'
export default {
    name: "StudentCourses",
    data() {
        return {
            classGroups: []
        }
    },
    created() {
        this.loadCourses()
    },
    methods: {
        loadCourses() {
            // 这里需要调用API获取按班级分组的课程数据
            // 暂时从已有的在线课程接口获取数据
            this.$axios.get('/video/list', {
                params: { userId: Cookies.get('userId') }
            }).then(resp => {
                if (resp.data.code === 200) {
                    const videos = resp.data.resultData || []
                    // 按班级分组
                    const groups = {}
                    videos.forEach(v => {
                        const key = v.classId || 'default'
                        if (!groups[key]) {
                            groups[key] = {
                                classId: key,
                                className: v.className || '默认班级',
                                courses: []
                            }
                        }
                        groups[key].courses.push(v)
                    })
                    this.classGroups = Object.values(groups)
                }
            }).catch(() => {
                this.$message.error('获取课程数据失败')
            })
        }
    }
}
</script>

<style scoped>
.page-header {
    padding: 20px 0;
    border-bottom: 2px solid #085e03;
    margin-bottom: 20px;
}
.page-header h2 {
    margin: 0;
    color: #303133;
    font-size: 24px;
}
.page-header p {
    margin: 8px 0 0;
    color: #909399;
    font-size: 14px;
}
.class-group {
    margin-bottom: 30px;
}
.class-group-header {
    margin-bottom: 15px;
}
.course-list {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
    gap: 20px;
}
.course-card-content {
    text-align: center;
}
.course-title {
    font-size: 16px;
    margin: 10px 0;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}
.course-meta {
    font-size: 13px;
    color: #909399;
    margin-bottom: 10px;
}
.image-placeholder {
    height: 140px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f0f0f0;
    color: #ccc;
    font-size: 40px;
}
</style>