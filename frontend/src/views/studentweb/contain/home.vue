<template>
    <div class="home-container">
        <el-carousel :interval="4000" type="card" height="400px">
            <el-carousel-item>
                <img src="@/assets/11.png" alt="banner1" style="height: 100%">
            </el-carousel-item>
            <el-carousel-item>
                <img src="@/assets/22.jpg" alt="banner2" style="height: 100%">
            </el-carousel-item>
            <el-carousel-item>
                <img src="@/assets/33.png" alt="banner3" style="height: 100%">
            </el-carousel-item>
            <el-carousel-item>
                <img src="@/assets/44.png" alt="banner4" style="height: 100%">
            </el-carousel-item>
            <el-carousel-item>
                <img src="@/assets/44.jpg" alt="banner5" style="height: 100%">
            </el-carousel-item>
            <el-carousel-item>
                <img src="@/assets/66.jpg" alt="banner6" style="height: 100%">
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
                                v-if="resolveCoverUrl(course) && !failedCoverMap[course.id]"
                                :src="String(resolveCoverUrl(course))"
                                :alt="course.courseName || course.course_name"
                                @error="onCoverError(course)"
                            />
                            <div v-else class="cover-fallback" :style="{ background: getCoverColor(course.id) }">
                                <span class="cover-letter">{{ (course.courseName || course.course_name || '课').charAt(0).toUpperCase() }}</span>
                            </div>
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
            courses: [],
            // 记录哪些课程的封面加载失败（id -> true），失败后改为渐变色兜底
            failedCoverMap: {}
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
                // 每次刷新课程列表时重置封面失败记录
                this.failedCoverMap = {};
            }).catch(() => {
                this.courses = [];
                this.failedCoverMap = {};
            });
        },
        /**
         * 将课程的 coverUrl 解析为可加载的完整 URL
         * 兼容三种存储形式：
         *   1) 网络链接：http(s):// 开头 → 直接返回
         *   2) 相对路径：./xxx.png → 拼成 baseApi + /resource/xxx.png
         *   3) 绝对路径：/file/imageFile/xxx.png → 拼成 baseApi + path
         *   4) 纯文件名：xxx.png → 视为 resource 目录下的文件
         * 必须始终返回字符串，避免 :src 绑定到函数引用
         */
        resolveCoverUrl(course) {
            if (!course) return '';
            const raw = course.coverUrl || course.cover_url;
            if (raw == null) return '';
            const url = String(raw).trim();
            if (!url) return '';
            if (url.startsWith('http://') || url.startsWith('https://')) {
                return url;
            }
            const base = (this.$store && this.$store.state && this.$store.state.baseApi) || '';
            if (url.startsWith('./')) {
                return base + '/resource/' + url.substring(2);
            }
            if (url.startsWith('/')) {
                return base + url;
            }
            return base + '/resource/' + url;
        },
        /**
         * 封面加载失败时，标记该课程改用兜底渐变色
         * ⚠️ 不修改 course.coverUrl，避免污染数据
         */
        onCoverError(course) {
            if (course && course.id != null) {
                this.$set(this.failedCoverMap, course.id, true);
            }
        },
        /**
         * 根据课程 id 生成稳定的渐变色（无图时显示）
         */
        getCoverColor(id) {
            const colors = [
                'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
                'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
                'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
                'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
                'linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%)',
                'linear-gradient(135deg, #fccb90 0%, #d57eeb 100%)',
                'linear-gradient(135deg, #e0c3fc 0%, #8ec5fc 100%)'
            ];
            const key = String(id == null ? '' : id);
            let hash = 0;
            for (let i = 0; i < key.length; i++) {
                hash = ((hash << 5) - hash) + key.charCodeAt(i);
            }
            return colors[Math.abs(hash) % colors.length];
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
    border-bottom: 2px solid #4e6ef2;
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
    box-shadow: 0 6px 20px rgba(78, 110, 242, 0.15);
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
.card-cover .cover-fallback {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
}
.card-cover .cover-letter {
    font-size: 56px;
    font-weight: 700;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.18);
    user-select: none;
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
