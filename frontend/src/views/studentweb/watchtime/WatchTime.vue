<template>
    <div class="watch-time-container">
        <!-- 页面标题和装饰 -->
        <div class="page-header">
            <h2 class="page-title">
                <img src="https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=watch%20time%20statistics%20icon%20clock%20time%20learning&image_size=square" alt="观看时长" class="title-icon">
                观看时长统计
            </h2>
            <p class="page-subtitle">记录你的学习旅程，见证每一步成长</p>
        </div>
        
        <div class="card-container">
            <!-- 个人总观看时长 -->
            <el-card class="time-card total-time-card">
                <div slot="header" class="card-header">
                    <img src="https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=personal%20time%20icon%20user%20profile&image_size=square" alt="个人总观看时长" class="card-icon">
                    <span>个人总观看时长</span>
                </div>
                <div class="time-display">
                    <div class="time-icon">
                        <i class="el-icon-time"></i>
                    </div>
                    <div class="time-value">{{ totalWatchTime }} 分钟</div>
                    <div class="time-desc">累计学习时间</div>
                </div>
            </el-card>
            
            <!-- 班级排名 -->
            <el-card class="time-card rank-card">
                <div slot="header" class="card-header">
                    <img src="https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=class%20rank%20icon%20leaderboard%20ranking&image_size=square" alt="班级排名" class="card-icon">
                    <span>班级排名</span>
                </div>
                <div v-if="classRankList.length === 0" class="empty-state">
                    <img src="https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=empty%20state%20illustration%20no%20ranking%20simple%20minimal&image_size=landscape_16_9" alt="暂无排名" class="empty-image">
                    <p>暂无排名数据</p>
                </div>
                <el-table v-else :data="classRankList" border stripe style="width: 100%">
                    <el-table-column prop="rank" label="排名" width="100"></el-table-column>
                    <el-table-column prop="userName" label="学生姓名"></el-table-column>
                    <el-table-column prop="totalWatchTime" label="观看时长（分钟）"></el-table-column>
                    <el-table-column prop="grade" label="等级">
                        <template slot-scope="scope">
                            <el-tag :type="scope.row.grade === 'A' ? 'success' : (scope.row.grade === 'B' ? 'warning' : 'danger')">
                                {{ scope.row.grade }}
                            </el-tag>
                        </template>
                    </el-table-column>
                </el-table>
            </el-card>
            
            <!-- 各视频观看时长 -->
            <el-card class="time-card video-time-card">
                <div slot="header" class="card-header">
                    <img src="https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=video%20time%20icon%20play%20video%20learning&image_size=square" alt="各视频观看时长" class="card-icon">
                    <span>各视频观看时长</span>
                </div>
                <div v-if="watchTimeList.length === 0" class="empty-state">
                    <img src="https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=empty%20state%20illustration%20no%20videos%20simple%20minimal&image_size=landscape_16_9" alt="暂无视频" class="empty-image">
                    <p>暂无视频观看数据</p>
                </div>
                <el-table v-else :data="watchTimeList" border stripe style="width: 100%">
                    <el-table-column prop="videoName" label="视频名称" width="200"></el-table-column>
                    <el-table-column prop="watchTime" label="观看时长（分钟）"></el-table-column>
                    <el-table-column prop="lastWatchTime" label="最后观看时间"></el-table-column>
                </el-table>
            </el-card>
        </div>
    </div>
</template>

<script>
import { getWatchTimeByUserId, getTotalWatchTimeByUserId, getWatchTimeByClassId } from "../../../api/studentweb/watchTime";
import Cookies from "js-cookie";

export default {
    name: "WatchTime",
    data() {
        return {
            watchTimeList: [],
            totalWatchTime: 0,
            classRankList: [],
            userId: Cookies.get('userId'),
            classId: Cookies.get('classId') || 1
        };
    },
    created() {
        this.loadWatchTimeData();
        this.loadClassRankData();
    },
    methods: {
        loadWatchTimeData() {
            // 获取个人总观看时长
            getTotalWatchTimeByUserId(this.userId).then(resp => {
                if (resp.data.code == 200) {
                    this.totalWatchTime = resp.data.resultData.totalWatchTimeMinutes || 0;
                }
            });
            
            // 获取各视频观看时长
            getWatchTimeByUserId(this.userId).then(resp => {
                if (resp.data.code == 200) {
                    this.watchTimeList = resp.data.resultData.map(item => {
                        return {
                            videoName: item.videoName || '未知视频',
                            watchTime: Math.floor((item.watchTime || 0) / 60),
                            lastWatchTime: item.lastWatchTime || '暂无记录'
                        };
                    });
                }
            });
        },
        loadClassRankData() {
            // 获取班级所有学生观看时长
            getWatchTimeByClassId(this.classId).then(resp => {
                if (resp.data.code == 200) {
                    const data = resp.data.resultData.map(item => {
                        return {
                            userId: item.userId,
                            userName: item.userName || '未知学生',
                            totalWatchTime: Math.floor((item.totalWatchTime || 0) / 60)
                        };
                    });
                    
                    // 按观看时长降序排序
                    data.sort((a, b) => b.totalWatchTime - a.totalWatchTime);
                    
                    // 分配等级
                    const totalStudents = data.length;
                    const rankedData = data.map((item, index) => {
                        let grade;
                        if (totalStudents === 1) {
                            grade = 'A';
                        } else {
                            // 使用0-based索引计算百分比
                            const percentage = index / (totalStudents - 1);
                            if (percentage <= 0.3) {
                                grade = 'A';
                            } else if (percentage <= 0.8) {
                                grade = 'B';
                            } else {
                                grade = 'C';
                            }
                        }
                        return {
                            ...item,
                            rank: index + 1,
                            grade: grade
                        };
                    });
                    
                    // 只显示当前用户的记录
                    const currentUserRecord = rankedData.find(item => item.userId == this.userId);
                    this.classRankList = currentUserRecord ? [currentUserRecord] : [];
                }
            });
        }
    }
}
</script>

<style scoped>
/* 容器样式 */
.watch-time-container {
    padding: 20px;
    background: linear-gradient(135deg, #f5f7fa 0%, #e4eaf1 100%);
    min-height: 100vh;
}

/* 页面标题样式 */
.page-header {
    text-align: center;
    padding: 30px 0;
    background: rgba(255, 255, 255, 0.9);
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

/* 卡片容器样式 */
.card-container {
    display: flex;
    flex-direction: column;
    gap: 20px;
}

/* 卡片样式 */
.time-card {
    background: rgba(255, 255, 255, 0.95);
    border-radius: 8px;
    box-shadow: 0 4px 12px rgba(0,0,0,0.1);
    transition: all 0.3s ease;
}

.time-card:hover {
    box-shadow: 0 8px 20px rgba(0,0,0,0.15);
    transform: translateY(-2px);
}

/* 卡片头部样式 */
.card-header {
    display: flex;
    align-items: center;
}

.card-icon {
    width: 24px;
    height: 24px;
    margin-right: 10px;
    border-radius: 50%;
    box-shadow: 0 1px 3px rgba(0,0,0,0.2);
}

/* 个人总观看时长样式 */
.total-time-card .time-display {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 30px 0;
}

.time-icon {
    font-size: 48px;
    color: #4caf50;
    margin-bottom: 20px;
}

.time-value {
    font-size: 36px;
    font-weight: bold;
    color: #333;
    margin-bottom: 10px;
}

.time-desc {
    font-size: 16px;
    color: #666;
}

/* 空状态样式 */
.empty-state {
    text-align: center;
    padding: 40px 0;
}

.empty-image {
    width: 200px;
    height: 150px;
    margin-bottom: 20px;
    border-radius: 8px;
}

.empty-state p {
    color: #999;
    font-size: 16px;
    margin: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
    .watch-time-container {
        padding: 10px;
    }
    
    .page-title {
        font-size: 24px;
    }
    
    .title-icon {
        width: 32px;
        height: 32px;
    }
    
    .total-time-card .time-display {
        padding: 20px 0;
    }
    
    .time-icon {
        font-size: 36px;
        margin-bottom: 15px;
    }
    
    .time-value {
        font-size: 28px;
    }
    
    .empty-image {
        width: 150px;
        height: 120px;
    }
}
</style>