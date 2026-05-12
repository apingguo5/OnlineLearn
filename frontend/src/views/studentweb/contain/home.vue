<template>
    <div class="home-container">
        <el-carousel :interval="4000" type="card" height="400px">
            <el-carousel-item>
                <img src="@/assets/1111.png" alt="banner1" style="height: 100%">
            </el-carousel-item>
            <el-carousel-item>
                <img src="@/assets/77777.png" alt="banner2" style="height: 100%">
            </el-carousel-item>
            <el-carousel-item>
                <img src="@/assets/2222.png" alt="banner3" style="height: 100%">
            </el-carousel-item>
        </el-carousel>

        <!-- 课程广场：展示所有课程卡片 -->
        <div class="course-plaza">
            <h2 class="section-title">📚 课程广场</h2>
            <div v-if="courses.length === 0" class="empty-tip">
                暂无课程数据
            </div>
            <el-row :gutter="20">
                <el-col :span="6" v-for="course in courses" :key="course.id" class="course-card-col">
                    <el-card shadow="hover" class="course-card" @click.native="goToCourse(course)">
                        <div class="card-cover">
                            <img
                                :src="course.coverUrl || 'https://via.placeholder.com/300x160?text=No+Image'"
                                :alt="course.courseName || course.course_name"
                            />
                        </div>
                        <div class="card-body">
                            <h3 class="card-title">{{ course.courseName || course.course_name }}</h3>
                            <p class="card-desc">{{ course.description || course.courseDescription || course.course_description || '暂无描述' }}</p>
                        </div>
                    </el-card>
                </el-col>
            </el-row>
        </div>
    </div>
</template>

<script>
import { getAllCourses } from "@/api/studentweb/courses";

export default {
    name: "home",
    data() {
        return {
            courses: []
        }
    },
    created() {
        this.fetchCourses();
    },
    methods: {
        fetchCourses() {
            getAllCourses().then(res => {
                // 后端 Result 结构: { resultData: [...], code: 200 }
                if (res && res.data && res.data.code === 200 && Array.isArray(res.data.resultData)) {
                    this.courses = res.data.resultData;
                } else if (res && res.data && res.data.code === 200 && Array.isArray(res.data.data)) {
                    this.courses = res.data.data;
                } else if (res && Array.isArray(res)) {
                    this.courses = res;
                } else if (res && res.data && Array.isArray(res.data)) {
                    this.courses = res.data;
                } else {
                    console.warn('课程数据格式异常:', res);
                    this.courses = [];
                }
            }).catch(() => {
                this.courses = [];
            });
        },
        goToCourse(course) {
            this.$router.push({
                name: 'CourseDetail',
                query: { courseId: course.id, courseName: course.courseName || course.course_name }
            });
        }
    }
}
</script>

<style scoped>
.home-container {
    padding: 0 20px;
}
.section-title {
    font-size: 24px;
    color: #303133;
    margin: 30px 0 20px;
    padding-bottom: 10px;
    border-bottom: 2px solid #085e03;
}
.empty-tip {
    text-align: center;
    color: #909399;
    padding: 40px 0;
    font-size: 16px;
}
.course-card-col {
    margin-bottom: 20px;
}
.course-card {
    cursor: pointer;
    transition: all 0.3s;
    border-radius: 8px;
    overflow: hidden;
}
.course-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 6px 20px rgba(8, 94, 3, 0.15);
}
.card-cover {
    width: 100%;
    height: 140px;
    overflow: hidden;
}
.card-cover img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}
.card-body {
    padding: 12px;
}
.card-title {
    font-size: 16px;
    color: #303133;
    font-weight: 600;
    margin: 0 0 8px 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}
.card-desc {
    font-size: 13px;
    color: #606266;
    line-height: 1.5;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    margin: 0;
    min-height: 39px;
}
</style>
