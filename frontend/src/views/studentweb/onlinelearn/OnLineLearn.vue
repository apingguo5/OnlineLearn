<template>
    <div>
        <!-- 页面标题和装饰 -->
        <div class="page-header">
            <h2 class="page-title">
                <img src="https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=online%20learning%20icon%20education%20school%20study%20book&image_size=square" alt="在线学习" class="title-icon">
                在线课程学习
            </h2>
            <p class="page-subtitle">探索知识的海洋，开启学习之旅</p>
        </div>
        
        <div class="dem">
            <div v-if="iData.length == 0" style="font-size: 26px;color: grey; text-align: center; padding: 50px; background: url('https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=empty%20state%20illustration%20no%20content%20simple%20minimal&image_size=landscape_16_9') no-repeat center; background-size: contain;">
                <div style="background: rgba(255,255,255,0.8); padding: 20px; border-radius: 8px; display: inline-block;">
                    - 当前未加入班级或者任教老师未发布视频 -
                </div>
            </div>
            <div class='demo' v-for="(url, idx) in iData" :key="url.id || idx">
                <div class="course-card">
                    <router-link
                        :to="{ path: '/detailonlineweb', query: { videoTotalId: url.id, userId: url.userId } }">
                        <div class="course-cover">
                            <!-- 真实封面：URL 解析非空且未失败时显示 -->
                            <img
                                v-show="resolveCoverUrl(url) && !failedCoverMap[url.id || idx]"
                                :src="resolveCoverUrl(url)"
                                class="cover-img"
                                alt="封面"
                                @error="onCoverError(url, idx)"
                                @load="onCoverLoad(url, idx)"
                            />
                            <!-- 兜底封面：URL 为空 或 加载失败时显示 -->
                            <div
                                v-show="!resolveCoverUrl(url) || failedCoverMap[url.id || idx]"
                                class="cover-fallback"
                                :style="{ background: getCoverColor(url.id || idx) }"
                            >
                                <span class="cover-letter">{{ (url.topic || '课').charAt(0).toUpperCase() }}</span>
                            </div>
                            <div class="course-overlay">
                                <i class="el-icon-video-play"></i>
                            </div>
                        </div>
                    </router-link>
                    <h3 class="course-title">{{ url.topic }}</h3>
                    <div class="ta">
                        <span class="teacher-name">
                            <i class="el-icon-user"></i> {{ url.userName }}
                        </span>
                    </div>
                </div>
            </div>
        </div>
        <div style="height: 40px"></div>
        <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange" :current-page="page.page"
            :page-sizes="[10, 20, 30, 40]" :page-size="page.pageSize" layout="total, sizes, prev, pager, next, jumper"
            :total="total">
        </el-pagination>
    </div>
</template>
<script>
import { onlineweb, onlinecourse } from '../../../api/studentweb/online.js'
import { videos } from "../../../api/studentweb/video.js";
import Cookies from "js-cookie";
export default {
    name: "OnLineLearn",
    data() {
        return {
            page: {
                page: 1, //初始页
                pageSize: 10,    //    每页的数据
                classId: ''
            },
            iData: [],
            videos: [],
            videoss: {
                videoTotalId: ''
            },
            course: {
                classId: ''
            },
            onLineCou: [],

            total: 0,
            // 记录哪些卡片的封面加载失败（id -> true）
            failedCoverMap: {}

        }
    },
    created() {
        this.page.classId = Cookies.get('classId')
        this.course.classId = Cookies.get('classId')
        this.listAllCourse(this.course)
        this.listAllStudentsScore(this.page)
    },
    methods: {


        detailvideo(video) {
            this.videoss.videoTotalId = video
            videos(this.videos).then(resp => {
                this.videos = resp.data.resultData.data

            })
        },
        handleSizeChange(size) {
            this.page.pageSize = size;
            this.listAllStudentsScore(this.page)
            // console.log(this.pageSize,'888')

            console.log(`每页 ${size} 条`);
        },
        handleCurrentChange(pageNum) {
            this.page.page = pageNum;
            this.listAllStudentsScore(this.page)
            console.log(`当前页: ${pageNum}`);
        },

        listAllCourse(page) {
            onlinecourse(page).then(resp => {
                this.onLineCou = resp.data.resultData
            })
        },
        listAllStudentsScore(page) {

            if (Cookies.get('classId') == 'undefined') {
                return;
            } else {
                page.classId = Cookies.get('classId')
            }
            onlineweb(page).then(resp => {
                this.iData = resp.data.resultData.records
                this.total = resp.data.resultData.total
                this.failedCoverMap = {}
                // 调试输出：检查后端返回的 coverUrl 字段实际形态
                if (this.iData && this.iData.length > 0) {
                    console.log('[OnLineLearn] 第一条数据：', this.iData[0])
                    console.log('[OnLineLearn] 解析后的封面 URL：', this.resolveCoverUrl(this.iData[0]))
                }
            })
        },
        /**
         * 将封面 URL 解析为可加载的完整地址
         * 兼容：① http(s):// 直接返回；② /xxx 拼 baseApi；③ ./xxx 视为 resource/xxx；④ 纯文件名视为 resource/xxx
         */
        resolveCoverUrl(item) {
            if (!item) return ''
            const raw = item.coverUrl || item.cover_url
            if (raw == null) return ''
            const url = String(raw).trim()
            if (!url) return ''
            if (url.startsWith('http://') || url.startsWith('https://')) {
                return url
            }
            const base = (this.$store && this.$store.state && this.$store.state.baseApi) || ''
            if (url.startsWith('./')) {
                return base + '/resource/' + url.substring(2)
            }
            if (url.startsWith('/')) {
                return base + url
            }
            return base + '/resource/' + url
        },
        onCoverError(item, idx) {
            const key = (item && item.id != null) ? item.id : idx
            console.warn('[OnLineLearn] 封面加载失败:', this.resolveCoverUrl(item), 'key=', key)
            this.$set(this.failedCoverMap, key, true)
        },
        onCoverLoad(item, idx) {
            const key = (item && item.id != null) ? item.id : idx
            // 加载成功时清理失败标记（防止重复请求时的状态残留）
            if (this.failedCoverMap[key]) {
                this.$set(this.failedCoverMap, key, false)
            }
        },
        getCoverColor(id) {
            const colors = [
                'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
                'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
                'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
                'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
                'linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%)'
            ]
            const key = String(id == null ? '' : id)
            let hash = 0
            for (let i = 0; i < key.length; i++) {
                hash = ((hash << 5) - hash) + key.charCodeAt(i)
            }
            return colors[Math.abs(hash) % colors.length]
        }
    }

}
</script>
<style scoped>
/* 页面标题样式 */
.page-header {
    text-align: center;
    padding: 30px 0;
    background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
    border-radius: 8px;
    margin-bottom: 30px;
    box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.page-title {
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28px;
    color: #333;
    margin-bottom: 10px;
}

.title-icon {
    width: 40px;
    height: 40px;
    margin-right: 15px;
    border-radius: 50%;
    box-shadow: 0 2px 5px rgba(0,0,0,0.2);
}

.page-subtitle {
    font-size: 16px;
    color: #666;
    margin: 0;
}

/* 课程卡片样式 */
.demo {
    width: 280px;
    margin: 0 20px 20px 0;
}

.course-card {
    background: #fff;
    border-radius: 8px;
    overflow: hidden;
    box-shadow: 0 4px 12px rgba(0,0,0,0.1);
    transition: all 0.3s ease;
}

.course-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 8px 20px rgba(0,0,0,0.15);
}

.course-cover {
    position: relative;
    overflow: hidden;
    height: 160px;
    /* 避免图片与 fallback 同时占位时叠加 */
    background: #f5f7fa;
}

.course-cover .cover-img {
    width: 100%;
    height: 160px;
    object-fit: cover;
    display: block;
    transition: transform 0.3s ease;
    position: absolute;
    top: 0;
    left: 0;
    z-index: 2;
}

.course-cover:hover .cover-img {
    transform: scale(1.05);
}

.course-cover .cover-fallback {
    width: 100%;
    height: 160px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    position: absolute;
    top: 0;
    left: 0;
    z-index: 1;
}

.course-cover .cover-letter {
    font-size: 64px;
    font-weight: 700;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.18);
    user-select: none;
}

.course-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0,0,0,0.3);
    display: flex;
    align-items: center;
    justify-content: center;
    opacity: 0;
    transition: opacity 0.3s ease;
}

.course-cover:hover .course-overlay {
    opacity: 1;
}

.course-overlay i {
    font-size: 40px;
    color: #fff;
    text-shadow: 0 2px 5px rgba(0,0,0,0.5);
}

.course-title {
    font-size: 16px;
    font-weight: bold;
    color: #333;
    margin: 15px 10px 10px;
    line-height: 1.4;
    height: 48px;
    overflow: hidden;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
}

.ta {
    padding: 0 10px 15px;
    display: flex;
    justify-content: center;
}

.teacher-name {
    color: #666;
    font-size: 14px;
    display: flex;
    align-items: center;
}

.teacher-name i {
    margin-right: 5px;
    color: #999;
}

.dem {
    display: flex;
    justify-content: flex-start;
    flex-wrap: wrap;
    padding: 10px 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
    .demo {
        width: 100%;
        margin: 0 0 20px 0;
    }
    
    .page-title {
        font-size: 24px;
    }
    
    .title-icon {
        width: 32px;
        height: 32px;
    }
}
</style>