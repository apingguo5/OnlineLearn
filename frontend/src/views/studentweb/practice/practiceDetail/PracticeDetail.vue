<template>
    <div style="padding: 10px;">
        <el-table :data="ApplicantHistory" style="width: 100%">
            <el-table-column prop="createTime" label="提交时间" width="180">
            </el-table-column>
            <el-table-column prop="title" label="作业名称" width="180">
            </el-table-column>
            <el-table-column prop="userName" label="批改教师">
            </el-table-column>
            <el-table-column prop="userName" label="分数">
                <template slot-scope="scope">
                    <span style="margin-left: 10px" v-if="scope.row.score == null">未批改</span>
                    <span style="margin-left: 10px" v-if="scope.row.score != null">{{scope.row.score}}</span>
                </template>
            </el-table-column>
            <el-table-column label="操作" width="260" fixed="right">
                <template slot-scope="scope">
                    <el-button @click="handleEdit(scope.$index, scope.row)" type="primary"> 查看详情</el-button>
                </template>
            </el-table-column>
        </el-table>

        <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange" :current-page="page.page"
            :page-sizes="[10, 20, 30, 40]" :page-size="page.pageSize" layout="total, sizes, prev, pager, next, jumper"
            :total="ApplicantHistory.length">
        </el-pagination>
    </div>
</template>

<script>
import { practicesde } from "../../../../api/studentweb/pradetail.js";
import Cookies from "js-cookie";

export default {
    name: "PracticeDetail",
    data() {
        return {
            ApplicantHistory: [],
            page: {
                page: 1, //初始页
                pageSize: 10,    //    每页的数据
                userId: ''
            },
        }
    },
    created() {
        this.page.userId = Cookies.get('userId')
        this.detailHistory(this.page)
    },
    methods: {
        handleEdit(index, row) {
            this.$router.push({
                name: 'pDetail',
                params: {
                    data2: row,

                }
            })
        },
        handleSizeChange(size) {
            this.page.pageSize = size;
            this.detailHistory(this.page)
            // console.log(this.pageSize,'888')
            console.log(`每页 ${size} 条`);
        },
        handleCurrentChange(pageNum) {
            this.page.pageNum = pageNum;
            this.detailHistory(this.page)
            console.log(`当前页: ${pageNum}`);
        },

        detailHistory(page) {
            practicesde(page).then(resp => {
                this.ApplicantHistory = resp.data.resultData.data;
            })
        }
    }
}
</script>

<style scoped></style>