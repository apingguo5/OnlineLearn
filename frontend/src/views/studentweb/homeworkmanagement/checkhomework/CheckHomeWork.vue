<template>
    <div>
        <div class="subject">
            <h1 class="nothomework">已完成作业详情</h1>
        </div>
        <el-divider></el-divider>
        <div v-for=" h in HomeWork " :key="h.id">
            <h2>{{h.subjectName}}</h2>
            <h2>{{h.userName}}</h2>
            <p>作业标题：{{h.title}}</p>
            <p> 完成时间：{{h.completionTime}}</p>
            <p> 批改状态:{{h.mode=='0'?'未批改':'已批改'}}</p>
<!--            <p>{{h.content}}</p>-->
            <!-- <router-link to="/markdown"> -->
                <div class="bt">
                    <el-button  type="primary" plain @click="addDetail(h,'zuoye')"> 查看详情</el-button> 
                    <el-button  type="primary" plain @click="addDetail(h,'daan')"> 查看参考答案</el-button>
                </div>
            <!-- </router-link> -->

            <el-divider> 提交时间：</el-divider>
        </div>
        <el-pagination
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
                :current-page="page.page"
                :page-sizes="[10, 20, 30, 40]"
                :page-size="page.pageSize"
                layout="total, sizes, prev, pager, next, jumper"
                :total="HomeWork.length">
        </el-pagination>

    </div>
</template>

<script>
    import {workdo} from '../../../../api/studentweb/dohomework.js'
    import Cookies from 'js-cookie'
    export default {
        name: "CheckHomeWork",
        data(){
            return{
                page:{
                    page:1, //初始页
                    pageSize:10,    //    每页的数据
                    userId:0,
                },
                HomeWork:[],
            }

        },
        created() {
            this.page.userId=Cookies.get('userId')
            this.CheckWork(this.page)
        },
        methods:{
            handleSizeChange(size) {
                this.page.pageSize = size;
                this.listAllStudentsScore(this.page)
                // console.log(this.pageSize,'888')

                console.log(`每页 ${size} 条`);
            },
            handleCurrentChange(pageNum) {
                this.page.pageNum = pageNum;
                this.listAllStudentsScore(this.page)
                console.log(`当前页: ${pageNum}`);
            },
            CheckWork(valid){
                workdo(valid).then(resp=>{
                    this.HomeWork=resp.data.resultData.data
                    console.log(this.HomeWork) 
                })
            },
            addDetail(h,type){
                this.$router.push({
                    name:'MarkDown',
                    params:{'homework':h,type:type}   
                })
                this.$store.dispatch('add',h)
            }
        }
    }
</script>

<style scoped>
.right{
    display: flex;
    justify-content: center;
}
    .bt{
        display: flex;
        justify-content: center;
    }
    .nothomework{
        display: flex;
        justify-content: center;
    }
</style>