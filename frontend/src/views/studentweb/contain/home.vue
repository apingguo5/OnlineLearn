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

        <!-- 课程广场：按课程名称分组展示班级 -->
        <div class="course-plaza">
            <h2 class="section-title">📚 课程广场</h2>
            <div v-if="courseGroups.length === 0" class="empty-tip">
                暂无课程数据
            </div>
            <div v-for="(group, index) in courseGroups" :key="index" class="course-group">
                <h3 class="course-title">{{ group.courseName }}</h3>
                <el-row :gutter="20">
                    <el-col :span="6" v-for="cls in group.classes" :key="cls.id" class="class-card-col">
                        <el-card shadow="hover" class="class-card" @click.native="goToCourse(cls)">
                            <div class="class-card-header">
                                <i class="el-icon-school"></i>
                                <span class="class-name">{{ cls.className }}</span>
                            </div>
                            <div class="class-card-body">
                                <p><i class="el-icon-user"></i> 教师：{{ cls.userName }}</p>
                                <p><i class="el-icon-date"></i> {{ cls.academicYear }} / {{ cls.semester === 1 ? '第一学期' : '第二学期' }}</p>
                                <p><i class="el-icon-s-grid"></i> 容量：{{ cls.maxStudents || '不限' }} 人</p>
                            </div>
                        </el-card>
                    </el-col>
                </el-row>
            </div>
        </div>
    </div>
</template>

<script>
import { getAllClasses } from "@/api/studentweb/studentClass";

export default {
    name: "home",
    data() {
        return {
            courseGroups: []
        }
    },
    created() {
        this.fetchClasses();
    },
    methods: {
        fetchClasses() {
            getAllClasses({}).then(res => {
                const list = res.data.resultData || [];
                const map = {};
                list.forEach(item => {
                    const key = item.courseName || '未分类';
                    if (!map[key]) {
                        map[key] = [];
                    }
                    map[key].push(item);
                });
                this.courseGroups = Object.keys(map).map(courseName => ({
                    courseName,
                    classes: map[courseName]
                }));
            }).catch(() => {
                this.courseGroups = [];
            });
        },
        goToCourse(cls) {
            // 跳转到课程详情页（如果已实现）
            this.$router.push('/studentcourses');
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
.course-group {
    margin-bottom: 32px;
}
.course-title {
    font-size: 18px;
    color: #085e03;
    margin-bottom: 12px;
    padding-left: 10px;
    border-left: 4px solid #085e03;
}
.class-card-col {
    margin-bottom: 20px;
}
.class-card {
    cursor: pointer;
    transition: all 0.3s;
    border-radius: 8px;
}
.class-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 6px 20px rgba(8, 94, 3, 0.15);
}
.class-card-header {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: bold;
    color: #085e03;
    margin-bottom: 12px;
}
.class-card-header .el-icon-school {
    font-size: 24px;
}
.class-name {
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}
.class-card-body p {
    font-size: 13px;
    color: #606266;
    line-height: 1.8;
}
.class-card-body i {
    margin-right: 4px;
}
</style>
