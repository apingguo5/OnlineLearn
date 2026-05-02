<template>
    <div>
        <router-link to="/checkhomework">
            <el-button type="success"> 返回</el-button>
        </router-link>
        <div>
            <mavon-editor :subfield="false" :autofocus="false" v-model="content" ref="md" style="width: 99%;height: 70vh;">
            </mavon-editor>
        </div>
        <div class="last">

            评分
            <el-input :disabled="homework.mode == '1'" placeholder="请输入内容" v-model="homework.score">

            </el-input>
            <span style="margin-top: 10px;display: block;"> 备注</span>

            <el-input :disabled="homework.mode == '1'" type="textarea" :rows="3" style="" placeholder="请输入"
                v-model="homework.remark">
            </el-input>
        </div>
    </div>
</template>

<script>
import { type } from 'os';
import Cookies from 'js-cookie'

export default {
    name: "MarkDown",
    data() {
        return {
            formData: '123',
            homework: {},
            type: "zuoye",
            content: '',
            isStu: false,
        }
    },
    created() {
        if(Cookies.get("roleId") == 3) {
            // 教师可以评分
            this.isStu = true;
        }
        console.log("created");
        console.log(this.$route.params);
        this.homework = this.$route.params.homework
        console.log(this.homework);
        this.type = this.$route.params.type
        console.log(type == 'zuoye');
        if (this.type == 'zuoye') { 
            this.content = this.homework.content
        } else {
            this.content = this.homework.answer 
        }

    }



}
</script>

<style scoped>
/* /deep/ .auto-textarea-wrapper .auto-textarea-input{
    height: 60vh;
    overflow-y: auto;
} */

.last {
    width: 95%;
    height: 200px;
    margin: 20px auto;

}
</style>