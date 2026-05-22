<template>
    <div style="min-height: 1080px; padding: 10px;">
        <el-button type="primary" @click="addVideo()" v-if="roleId == 2"> 新增视频</el-button>
        <div style="height: 20px"></div>
        <div class="containvideo">

            <el-dialog title="请填写视频内容" :visible.sync="goDealDialogVisible" width="50%" :show-close="false"
                :before-close="goDealHandleClose">
                <template>
                    <div>
                        <div style="margin: 10px;"></div>
                        <el-form label-width="100px">
                            <el-form-item label="视频标题">
                                <el-input v-model="addVideoDetail.topic" placeholder="请输入"
                                    :disabled="startUpload"></el-input>
                            </el-form-item>
                            <el-form-item label="视频排序">
                                <el-input v-model="addVideoDetail.sort" placeholder="请输入"
                                    :disabled="startUpload"></el-input>
                            </el-form-item>
                            <el-form-item label="上传视频">

                                <el-upload ref="videoRef" style="margin-left:14%;margin-top:5%"
                                    class="avatar-uploader el-upload--text" :drag="Plus"
                                    action="http://127.0.0.1:9251/study/videos/save" multiple :show-file-list="true"
                                    :data="addVideoDetail" :on-success="handleVideoSuccess"
                                    :before-upload="beforeUploadVideo">
                                    <i v-if="Plus" class="el-icon-upload"></i>
                                    <div v-if="Plus" class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
                                    <div class="el-upload__tip" slot="tip">只能上传mp4/flv/avi文件，且不超过700MB</div>
                                </el-upload>
                            </el-form-item>

                        </el-form>
                    </div>
                </template>

                <span slot="footer" class="dialog-footer">
                    <el-button @click="goDealDialogVisible = false"> 取 消</el-button>
                    <!-- <el-button type="primary" @click="submit()" :disabled="!uploadSuccess"> 确 定</el-button> -->
                </span>
            </el-dialog>
            <el-row :gutter="24" style="width: 100%;">
                <el-col :span="18">
                    <div>
                        <div v-if="playerOptions.length <= 0"
                            style="width: 100%;text-align: center;font-size: 34px;color: grey;height: 250px;border: 1px solid grey;padding-top: 150px;">
                            暂无视频 请先上传视频
                        </div>
                        <video-player v-if="playerOptions.length > 0" class="video-player vjs-custom-skin"
                            ref="videoPlayer" :playsinline="true" :options="playerOptions[count]"
                            @play="onPlayerPlay($event, count)" @pause="onPlayerPause($event)"></video-player>
                        
                        <!-- 实时观看时长显示 -->
                        <div v-if="roleId == 3" style="margin-top: 10px; text-align: center;">
                            <span style="color: #4e6ef2; font-weight: bold;">累计观看时长：{{ displayWatchTime }} 秒</span>
                        </div>

                        <div style="margin: 20px 0;"></div>
                        <span v-if="roleId == 3">

                            提问： <p><el-input type="textarea" placeholder="请输入内容" v-model="Ask.content" maxlength="300"
                                    show-word-limit></el-input>
                            </p>
                            <el-button type="success" @click="answer(Ask)"> 提问</el-button>
                        </span>

                        <div style="margin: 200px 0;"></div>
                    </div>
                </el-col>
                <el-col :span="6">
                    <div class="video-catalog">
                        <div id="video-title">
                            <i class="el-icon-files"></i>
                            视频目录
                            <span class="catalog-count" v-if="videolist.length > 0">（共 {{ videolist.length }} 节）</span>
                        </div>
                        <hr>
                        <div v-if="videolist.length <= 0" class="empty-videos">
                            <i class="el-icon-warning-outline"></i>
                            <p>暂无视频</p>
                            <p class="empty-hint">老师还没有上传课程视频</p>
                        </div>
                        <div v-else class="video-list-wrap">
                            <div
                                v-for="(item, index) in videolist"
                                :key="index"
                                class="video-list-item"
                                :class="{ active: count == index }"
                            >
                                <div class="video-item-content" @click="choose(index)">
                                    <span class="video-index">{{ index + 1 }}</span>
                                    <span class="video-name" :title="item.topic">{{ item.topic }}</span>
                                </div>
                                <el-button
                                    v-if="roleId == 2"
                                    type="danger"
                                    size="mini"
                                    icon="el-icon-delete"
                                    circle
                                    @click.stop="deletOneVideo(item.id)"
                                ></el-button>
                            </div>
                        </div>

                    </div>
                </el-col>
            </el-row>



        </div>
    </div>
</template>

<script>
import { videos, deleteVideo, askandanswer } from "../../../api/studentweb/video.js";
import { post } from "../../../api/request";
import Cookies from "js-cookie";
import { WatchTimeService } from "../../../services/WatchTimeService.js";
export default {
    name: "DetailOnlineWeb",
    data() {
        return {
            uploadSuccess: false,
            startUpload: false,
            roleId: 0,
            goDealDialogVisible: false,
            Plus: true,
            videoFlag: false,
            addVideoDetail: {
                topic: '',
                sort: 0,
                videoTotalId: '',
            },


            Ask: {
                content: '',
                sender: '',
                recipient: '',
                videoId: '',
            },
            videolist: [],
            playsinline: true,
            playerOptions: [],
            options: [],
            count: '0',
            roleId: '',
            deleteOneVideoName: {
                id: ''
            },
            video: {
                videoTotalId: '',
            },
            ops: {
                vuescroll: {},
                scrollPanel: {},
                rail: {
                    keepShow: true,
                },
                bar: {
                    hoverStyle: true,
                    onlyShowBarOnScroll: false, //是否只有滚动的时候才显示滚动条
                    background: '#F5F5F5', //滚动条颜色
                    opacity: 0.5, //滚动条透明度
                    'overflow-x': 'hidden',
                },
                videoData: [],
                tagRoute: '',
            },
            // 观看时长服务实例
            watchTimeService: null,
            // 显示的观看时长
            displayWatchTime: 0,
        };

    },




    created() {
        this.roleId = Cookies.get('roleId')
        this.Ask.sender = Cookies.get('userId')
        this.Ask.recipient = this.$route.query.userId;
        this.video.videoTotalId = this.$route.query.videoTotalId;
        this.addVideoDetail.videoTotalId = this.$route.query.videoTotalId;
        this.Ask.videoId = this.$route.query.videoTotalId;
        
        // 初始化观看时长服务
        this.watchTimeService = new WatchTimeService({
            onWatchTimeUpdate: (watchTime) => {
                this.displayWatchTime = watchTime;
            }
        });
        
        this.listAllStudentsScore(this.video)
        // this.getMovieList();
        this.roleId = Cookies.get('roleId')
        console.log(111)
        console.log(this.$route.path)
        if (this.$route.path == '/teachervideo' && this.roleId != 2) {
            this.$router.push("/login")
        }
        if (this.$route.path == '/detailonlineweb' && this.roleId != 3) {
            this.$router.push("/login")

        }
    },
    methods: {


        submit() {
            this.goDealDialogVisible = false;
            this.listAllStudentsScore(this.video);
            this.uploadSuccess = false
        },


        // 视频上传前执行
        beforeUploadVideo(file) {
            if (this.addVideoDetail.topic == null || this.addVideoDetail.topic.trim() == "") {
                this.$message.error('请先填写视频标题！')
                return false
            }
            this.startUpload = true
            let max = 700 // 最大不能超过MB
            const maxMemory = file.size / 1024 / 1024 < max
            if (['video/mp4', 'video/ogg', 'video/flv', 'video/avi', 'video/wmv', 'video/rmvb'].indexOf(file.type) === -1) {
                this.$message.error('请上传正确的视频格式')
                return false
            }
            if (!maxMemory) {
                this.$message.error('上传视频大小不能超过700MB哦!')
                return false
            }
        },
        // 视频上传过程中执行
        uploadVideoProcess(event, file, fileList) {
            this.Plus = false
            this.videoFlag = true
            this.videoUploadPercent = file.percentage.toFixed(0)
        },
        // 视频上传成功是执行
        handleVideoSuccess(res, file) {
            this.Plus = false
            this.videoUploadPercent = 100
            // 如果为200代表视频保存成功
            if (res.resCode === '200') {
                // 接收视频传回来的名称和保存地址
                this.videoForm.videoId = res.newVidoeName
                this.videoForm.videoUrl = res.VideoUrl
                this.$message.success('视频上传成功！')
            } else {
                this.$message.success('视频上传成功')

            }
            this.goDealDialogVisible = false
            this.uploadSuccess = true
            this.listAllStudentsScore(this.video);
        },


        addVideo() {
            this.addVideoDetail = {
                topic: '',
                sort: 0,
                videoTotalId: '',
            }
            this.uploadSuccess = false
            this.startUpload = false
            this.goDealDialogVisible = true
            this.addVideoDetail.videoTotalId = this.$route.query.videoTotalId;
            setTimeout(() => {
                this.$refs.videoRef.clearFiles()
                this.listAllStudentsScore(this.video)

                this.Plus = true
            }, 1);
        },

        goDealHandleClose() {

        },
        answer(ask) {
            askandanswer(ask).then(resp => {
                if (resp.data.code == 200) {
                    this.$message({
                        message: '留言成功，请到问答社区查看结果',
                        type: 'success'
                    });
                    this.$router.push("/askandanswer")
                } else {
                    this.$message.error('留言失败');
                }
            })
        },
        choose(index) {
            this.count = index;
            console.log(index, '9999')
            
            // 切换视频时保存之前的观看时长并开始新的计时
            if (this.roleId == 3) {
                this.watchTimeService.switchVideo(this.videolist[index].id, this.video.videoTotalId);
            }
        },


        deletOneVideo(id) {
            this.deleteOneVideoName.id = id
            deleteVideo(this.deleteOneVideoName).then(resp => {
                if (resp.data.code == 200) {
                    this.$message({
                        message: '删除视频',
                        type: 'success'
                    });
                    this.listAllStudentsScore(this.video)
                } else {
                    this.$message.error('删除失败');
                }
            })
        },


        getMovieList() {
            // 每次重新构建播放器选项数组，避免重复累加导致索引错乱
            this.playerOptions = [];
            // 这里正常来说应该是从后台获取的数据，以下操作都是在成功的回调函数里
            for (var i = 0; i < this.videolist.length; i++) {
                let arrs = {
                    playbackRates: [1.0, 2.0, 3.0], //播放速度
                    autoplay: false, //如果true,浏览器准备好时开始回放。
                    muted: false, // 默认情况下将会消除任何音频。
                    loop: false, // 导致视频一结束就重新开始。
                    preload: "auto", // 建议浏览器在<video>加载元素后是否应该开始下载视频数据。auto浏览器选择最佳行为,立即开始加载视频（如果浏览器支持）
                    language: "zh-CN",
                    aspectRatio: "16:9", // 将播放器置于流畅模式，并在计算播放器的动态大小时使用该值。值应该代表一个比例 - 用冒号分隔的两个数字（例如"16:9"或"4:3"）
                    fluid: true, // 当true时，Video.js player将拥有流体大小。换句话说，它将按比例缩放以适应其容器。
                    sources: [
                        {
                            type: "video/mp4",
                            src: this.$store.state.baseApi + this.videolist[i].videoUrl //url地址
                        }
                    ],
                    poster: "", //封面地址
                    notSupportedMessage: "此视频暂无法播放，请稍后再试", //允许覆盖Video.js无法播放媒体源时显示的默认信息。
                    controlBar: {
                        timeDivider: true,
                        durationDisplay: true,
                        remainingTimeDisplay: false,
                        fullscreenToggle: true //全屏按钮
                    }
                };
                this.playerOptions.push(arrs);
            }
            console.log(this.videolist.size, '888')
        },
        onPlayerPlay(player, index) {
            var that = this.$refs.videoPlayer;
            for (let i = 0; i < that.length; i++) {
                if (i != index)
                    that[i].player.pause()
            }
            
            // 视频开始播放时记录开始时间
            if (this.roleId == 3 && this.videolist[index]) {
                // 检查是否已经在观看当前视频
                if (this.watchTimeService.currentVideoId == this.videolist[index].id) {
                    // 如果是同一视频，调用恢复方法
                    this.watchTimeService.resumeWatch();
                } else {
                    // 如果是不同视频，调用开始方法
                    this.watchTimeService.startWatch(this.videolist[index].id, this.video.videoTotalId);
                }
            }
        },
        onPlayerPause(player) {
            // 暂停时停止计时
            if (this.roleId == 3) {
                this.watchTimeService.pauseWatch();
            }
        },

        listAllStudentsScore(page) {
            console.log("9998")
            console.log(page)
            videos(page).then(resp => {
                this.videolist = resp.data.resultData || [];
                // 重置当前播放索引，避免越界
                this.count = 0;
                this.getMovieList();
                
                // 视频列表加载完成后不自动启动计时，只在用户开始播放时才记录
            })
        },




    },

    filters: {
        ellipsis(value) {
            if (!value) return '';
            if (value.length > 15) {
                return value.slice(0, 15) + '...'
            }
            return value
        }
    },
    
    mounted() {
        // 页面加载时不自动启动计时，只在用户开始播放时才记录
    },
    
    beforeDestroy() {
        // 页面离开时保存观看时长
        if (this.watchTimeService) {
            this.watchTimeService.destroy();
        }
    }
}
</script>

<style scoped>
.containvideo {
    display: flex;
    justify-content: space-between;

}

.item {
    width: 20px;
    height: 20px;
    display: flex;
    justify-content: flex-start;
}


.videos {
    width: 19%;
    margin: 0 0 0 8px;
    height: 50%;
    border: 3px solid grey;
}

#video-title {
    font-size: 24px;
    color: black;
    font-weight: 600;
}

.catalog-count {
    font-size: 14px;
    color: #909399;
    font-weight: normal;
    margin-left: 6px;
}

.video-catalog {
    background: #fafbfc;
    padding: 14px 12px;
    border-radius: 8px;
    border: 1px solid #ebeef5;
    min-height: 200px;
}

.empty-videos {
    text-align: center;
    color: #909399;
    padding: 40px 10px;
}
.empty-videos i {
    font-size: 42px;
    color: #c0c4cc;
    margin-bottom: 10px;
}
.empty-videos p {
    margin: 4px 0;
    font-size: 14px;
}
.empty-videos .empty-hint {
    font-size: 12px;
    color: #c0c4cc;
}

.video-list-wrap {
    display: flex;
    flex-direction: column;
    gap: 6px;
    max-height: 480px;
    overflow-y: auto;
    padding-right: 4px;
}
.video-list-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 10px;
    border-radius: 6px;
    background: #ffffff;
    border: 1px solid #ebeef5;
    transition: all 0.2s;
}
.video-list-item:hover {
    border-color: #409eff;
    background: #ecf5ff;
}
.video-list-item.active {
    border-color: #409eff;
    background: linear-gradient(90deg, #ecf5ff 0%, #ffffff 100%);
    box-shadow: 0 2px 6px rgba(64, 158, 255, 0.15);
}
.video-list-item.active .video-index {
    background: #409eff;
    color: #fff;
}
.video-list-item.active .video-name {
    color: #409eff;
    font-weight: 600;
}
.video-item-content {
    display: flex;
    align-items: center;
    gap: 8px;
    flex: 1;
    cursor: pointer;
    min-width: 0;
}
.video-index {
    width: 22px;
    height: 22px;
    flex-shrink: 0;
    border-radius: 50%;
    background: #e6e8eb;
    color: #606266;
    font-size: 12px;
    font-weight: 600;
    display: flex;
    align-items: center;
    justify-content: center;
}
.video-name {
    flex: 1;
    font-size: 13px;
    color: #303133;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.vid {
    width: 80%;
    height: 20%;
}
</style>