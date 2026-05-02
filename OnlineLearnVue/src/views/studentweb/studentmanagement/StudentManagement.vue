<template>
    <div>
        <el-button type="primary" @click="$router.push('/applicanthistory')"> 申请记录</el-button><br><hr>
        <div v-for="s in StudentClassData" :key="s.id">
            <div>
                <span>
                    <el-tag type="success"> 班级</el-tag>
                    {{ s.className }}
                </span>
                <el-popover placement="top-start" width="100" class="right" trigger="hover"
                    :content="!s.flag ? '加入该班级' : '无法加入，您当前已经加入班级或加入班级审核中'">
                    <el-button type="primary" :disabled="s.flag" @click="studentAddClass(s.id, s.userId)" slot="reference">
                        加入</el-button>

                </el-popover>
            </div>
            <el-divider></el-divider>
        </div>
        <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange" :current-page="page.page"
            :page-sizes="[10, 20, 30, 40]" :page-size="page.pageSize" layout="total, sizes, prev, pager, next, jumper"
            :total="StudentClassData.length">
        </el-pagination>
    </div>
</template>

<script>
import { smanamgent, addClass, detailhistory, isFlage } from "../../../api/studentweb/studentmanagement.js";
import Cookies from "js-cookie";

export default {
    name: "StudentManagement",
    data() {
        return {
            StudentClassData: [],
            page: {
                page: 1, //初始页
                pageSize: 10,    //    每页的数据
            },
            addClassStudent: {
                classId: '',
                userId: ''
            },

            applicantHistory: [],
            param: {
                userId: ''
            }


        }
    },
    created() {
        this.param.userId = Cookies.get("userId")
        this.StudentClass(this.page)
        this.addClassStudent.userId = Cookies.get('userId')
    },
    methods: {
        handleSizeChange(size) {
            this.page.pageSize = size;
            this.StudentClass(this.page)
            // console.log(this.pageSize,'888')
            console.log(`每页 ${size} 条`);
        },
        handleCurrentChange(pageNum) {
            this.page.pageNum = pageNum;
            this.StudentClass(this.page)
            console.log(`当前页: ${pageNum}`);
        },

        StudentClass(page) {
            console.log('请求班级列表，参数:', page);
            smanamgent(page).then(resp => {
                console.log('后端响应:', resp);
                // 检查后端返回的数据结构
                if (resp.data && resp.data.data) {
                    this.StudentClassData = resp.data.data.filter(item => item.className === '计算机1班' || item.className === '软件工程1班')
                } else if (resp.data && resp.data.resultData) {
                    if (resp.data.resultData.data) {
                        this.StudentClassData = resp.data.resultData.data.filter(item => item.className === '计算机1班' || item.className === '软件工程1班')
                    } else if (resp.data.resultData.records) {
                        this.StudentClassData = resp.data.resultData.records.filter(item => item.className === '计算机1班' || item.className === '软件工程1班')
                    } else {
                        this.StudentClassData = []
                    }
                } else {
                    this.StudentClassData = []
                }
                // 为每个班级设置flag属性，默认值为false
                this.StudentClassData.forEach(item => {
                    item.flag = false
                })
                console.log('处理后的班级列表:', this.StudentClassData);
                // state.ClassData=resp.data.data.records
                // this.$store.dispatch('classAction',this.ClassData)
            }).catch(error => {
                console.error('请求班级列表失败:', error);
                this.$message.error('网络错误，请稍后重试');
            })
        },
        studentAddClass(classId, userId) {
            console.log('点击加入班级按钮，classId:', classId, 'userId:', userId);
            this.addClassStudent.classId = classId;
            this.addClassStudent.userId = Cookies.get('userId');
            this.addClassStudent.status = 1; // 1：待审核
            console.log('提交的申请数据:', this.addClassStudent);

            // this.$alert('这是一段内容', '标题名称', {
            //     confirmButtonText: '确定',
            //     callback: action => {
            //         this.$message({
            //             type: 'info',
            //             message: `action: ${ action }`
            //         });
            //     }
            // });

            addClass(this.addClassStudent).then(resp => {
                console.log('后端响应:', resp);
                if (resp.data.code == 200) {
                    this.$message({
                        message: '申请成功，待老师审核',
                        type: 'success'
                    });
                    // 只禁用当前申请的班级的加入按钮
                    this.StudentClassData.forEach(item => {
                        if (item.id === classId) {
                            item.flag = true
                        }
                    })
                } else {
                    this.$message.error('不可加入该班级');
                }
            }).catch(error => {
                console.error('请求失败:', error);
                this.$message.error('网络错误，请稍后重试');
            })
        },



    },
}
</script>

<style scoped>
.right {
    display: flex;
    justify-content: center;
}
</style>