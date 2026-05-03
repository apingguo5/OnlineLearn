<template>
    <div>
        <el-table
                :data="tableData"
                :default-sort="{prop: 'date', order: 'descending'}"
                style="width: 100%">
            <el-table-column
                    prop="createTime"
                    label="创建日期"
                    width="180">
            </el-table-column>
            <el-table-column
                    sortable
                    prop="account"
                    label="账号">
            </el-table-column>
            <el-table-column
                    prop="userName"
                    label="姓名"
                    width="180">
            </el-table-column>
            <el-table-column
                    prop="studentPhone"
                    label="学生电话">
            </el-table-column>
            <el-table-column
                    prop="status"
                    label="状态">
            </el-table-column>
            <el-table-column
                    prop="charger"
                    label="审核人">
            </el-table-column>


            <el-table-column label="操作" width="260" fixed="right">
                <template slot-scope="scope" v-if="scope.row.status=='待审核'">
                    <el-button type="success"
                            @click="handleEdit(scope.$index, scope.row)"> 同意
                    </el-button>
                    <el-button
                            type="danger"
                            @click="handleDelete(scope.$index, scope.row)"> 驳回
                    </el-button>
                </template>
            </el-table-column>
        </el-table>
        <el-pagination
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
                :current-page="page.page"
                :page-sizes="[10, 20, 30, 40]"
                :page-size="page.pageSize"
                layout="total, sizes, prev, pager, next, jumper"
                :total="tableData.length">
        </el-pagination>

    </div>
</template>

<script>
    import {tapplicant,alertapplicant} from "../../api/teacher/teacherapplicant.js";
    import Cookies from 'js-cookie'
    export default {
        name: "TeacherApplicant",
        data(){
            return{
                tableData: [],
                page: {
                    page: 1, //初始页
                    pageSize: 10,    //    每页的数据
                    userId:'',
                },

                alertData:{},
                flag:true
            }
        },
        created() {
            this.page.userId=Cookies.get('userId')
            this.listUnApplicant(this.page)
            if(this.tableData.status=='待审核'){
                this.flag=false

            }
        },
        methods: {
            handleEdit(index,row){
                console.log('点击同意按钮，row:', row);
                this.alertData = {
                    id: row.id,
                    userId: row.userId,
                    classId: row.classId,
                    status: 2
                };
                console.log('提交的审核数据:', this.alertData);
                alertapplicant(this.alertData).then(resp=>{
                    console.log('后端响应:', resp);
                    if(resp.data.code==200){
                        this.$message({
                            message: '您已同意该学生加入您的班级',
                            type: 'success'
                        });
                        
                        this.listUnApplicant(this.page)
                    }else {
                        this.$message.error('新增失败');
                    }
                }).catch(error => {
                    console.error('请求失败:', error);
                    this.$message.error('网络错误，请稍后重试');
                })

            },

            handleDelete(index,row){
                console.log('点击驳回按钮，row:', row);
                this.alertData = {
                    id: row.id,
                    userId: row.userId,
                    classId: row.classId,
                    status: 3
                };
                console.log('提交的审核数据:', this.alertData);
                alertapplicant(this.alertData).then(resp=>{
                    console.log('后端响应:', resp);
                    if(resp.data.code==200){
                        this.$message({
                            message: '您已驳回该学生加入您的班级',
                            type: 'success'
                        });
                        this.listUnApplicant(this.page)
                    }else {
                        this.$message.error('新增失败');
                    }
                }).catch(error => {
                    console.error('请求失败:', error);
                    this.$message.error('网络错误，请稍后重试');
                })

            },

            handleSizeChange(size) {
            this.page.pageSize = size;
            // console.log(this.pageSize,'888')
            this.listUnApplicant(this.page)
            console.log(`每页 ${size} 条`);
        },
        handleCurrentChange(pageNum) {
            this.page.page = pageNum;
            this.listUnApplicant(this.page)
            console.log(`当前页: ${pageNum}`);
        },

        listUnApplicant(page) {
            console.log('请求申请列表，参数:', page);
            tapplicant(page).then(resp => {
                console.log('后端响应:', resp);
                // 检查后端返回的数据结构
                if (resp.data && resp.data.data) {
                    this.tableData = resp.data.data
                } else if (resp.data && resp.data.resultData) {
                    if (resp.data.resultData.records) {
                        this.tableData = resp.data.resultData.records
                    } else if (resp.data.resultData.data) {
                        this.tableData = resp.data.resultData.data
                    } else {
                        this.tableData = []
                    }
                } else {
                    this.tableData = []
                }
                console.log('处理后的申请列表:', this.tableData);
            }).catch(error => {
                console.error('请求申请列表失败:', error);
                this.$message.error('网络错误，请稍后重试');
            })
        },
        }
    }
</script>

<style scoped>

</style>