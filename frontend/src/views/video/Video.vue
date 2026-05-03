<template>
    <div>
        <el-button type="primary" @click="uploadddVideo()"> 新增教学视频集</el-button>

        <div style="height: 20px"></div>
        <el-dialog title="请输入视频集的内容" :visible.sync="goDealDialogVisible" width="50%">
            <template>
                <div>
                    <div style="margin: 10px;"></div>
                    <el-form label-width="100px">
                        <el-form-item label="视频集标题">
                            <el-input v-model="uploadVideo.topic" placeholder="请输入"></el-input>
                        </el-form-item>
                        <el-form-item label="上传图片">
                            <el-upload class="upload-demo" ref="upload" :data="uploadVideo"
                                action="http://127.0.0.1:9251/study/videoTotal/save" :on-preview="handlePreview"
                                :on-remove="handleRemove" :before-remove="beforeRemove" multiple :limit="3"
                                :on-exceed="handleExceed" :on-success="handleUploadSuccess" :file-list="uploadVideo.fileList">
                                <el-button size="small" type="primary"> 上传封面</el-button>
                                <div slot="tip" class="el-upload__tip">只能上传jpg/png文件</div>
                            </el-upload>
                        </el-form-item>



                    </el-form>
                </div>
            </template>

            <span slot="footer" class="dialog-footer">
                <el-button @click="goDealDialogVisible = false"> 取 消</el-button>
                <el-button type="primary" @click="submit(videoParam)"> 确 定</el-button>
            </span>
        </el-dialog>



        <div class="dem">

            <div class='demo' v-for="url in videoData" :key="url">
                <div>
                    <router-link :to="{ path: '/teachervideo', query: { videoTotalId: url.id } }">
                        <el-image :src="$store.state.baseApi + url.coverUrl" style="height: 120px"></el-image>
                    </router-link>
                    <h3>{{ url.topic }}</h3>
                    <el-button type="danger" @click="deleteAllVideos(url.id)"> 删除教学视频集</el-button>
                </div>
            </div>
        </div>
        <div style="height: 20px"></div>
        <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange" :current-page="page.page"
            :page-sizes="[10, 20, 30, 40]" :page-size="page.pageSize" layout="total, sizes, prev, pager, next, jumper"
            :total="total">
        </el-pagination>
    </div>
</template>

<script>
import { listVideo, savaVideo, deleteVideos, pageVideo } from '../../api/video.js'
import Cookies from "js-cookie";
export default {
    name: "Video",
    data() {
        return {
            goDealDialogVisible: false,
            page: {
                page: 1, //初始页
                pageSize: 10,    //    每页的数据
                userId: '',
                roleId: '',
            },

            uploadVideo: {
                topic: '',
                userId: 0,
            },


            videoData: [],
            total: 0,
            videoParam: {
                fileList: '',
                title: '',
                userId: '',
            },
            deleteparam: {
                id: ''
            }

        }
    },
    created() {
        this.page.userId = Cookies.get('userId')
        this.page.roleId = Cookies.get('roleId')
        this.videoParam.userId = Cookies.get('userId')
        this.listAllVideos(this.page)

        console.log(this.$store.state.baseApi + this.url.coverUrl)
    },
    methods: {
        goDealHandleClose() {

        },
        deleteAllVideos(id) {
            this.deleteparam.id = id
            this.$confirm('此操作将永久删除该视频集下的所有视频, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                deleteVideos(this.deleteparam).then(resp => {
                    this.$message({
                        type: 'success',
                        message: '删除成功!'
                    });
                    this.listAllVideos(this.page)
                })
                this.listAllVideos(this.page)
            }).catch(() => {
                this.$message({
                    type: 'info',
                    message: '已取消删除'
                });
            });



        },


        uploadddVideo() {
            this.goDealDialogVisible = true
            this.uploadVideo.userId = Cookies.get('userId')
            this.listAllVideos(this.page)
        },


        submit(videoParam) {
            this.$refs.upload.submit();
            // 不再立即关闭对话框，等待上传成功回调处理
        },
        listAllVideos(page) {
            page.userId = Cookies.get('userId')
            pageVideo(page).then(resp => {
                this.videoData = resp.data.resultData.records
                this.total = resp.data.resultData.total
            })
        },
        handleSizeChange(size) {
            this.page.pageSize = size;

        },
        handleCurrentChange(pageNum) {
            console.log(pageNum)
            this.page.page = pageNum;
            this.listAllVideos(this.page)
        },

        handleRemove(file, fileList) {
            console.log(file, fileList);
        },
        handlePreview(file) {
            console.log(file);
        },
        handleExceed(files, fileList) {
            this.$message.warning(`当前限制选择 3 个文件，本次选择了 ${files.length} 个文件，共选择了 ${files.length + fileList.length} 个文件`);
        },
        beforeRemove(file, fileList) {
            return this.$confirm(`确定移除 ${file.name}？`);
        },
        handleUploadSuccess(response, file, fileList) {
            if (response.resCode === '200') {
                this.$message({
                    message: '视频集上传成功',
                    type: 'success'
                });
                this.goDealDialogVisible = false;
                this.listAllVideos(this.page);
                // 清空表单
                this.uploadVideo.topic = '';
                this.$refs.upload.clearFiles();
            } else {
                this.$message.error('视频集上传失败');
            }
        }

    }
}
</script>

<style scoped>
.demo {
    width: 200px;
    /*height: 20%;*/
    text-align: center;
    line-height: 20%;
    border: 1px solid transparent;
    border-radius: 4px;
    overflow: hidden;
    background: #fff;
    position: relative;
    box-shadow: 0 1px 1px rgba(0, 0, 0, .2);
    margin-right: 4px;
    display: flex;
    justify-content: space-between;
    flex-wrap: wrap;
    margin-bottom: 20px;

}

el-button {
    display: inline-block;
}

.course {
    display: inline-block;
}

.ta {
    display: flex;
    justify-content: space-between;
}

.dem {
    display: flex;
    justify-content: flex-start;
    flex-wrap: wrap;
}

.el-image__inner {
    vertical-align: top;
    height: 200px;
}

.demo:hover {
    display: block;
}
</style>