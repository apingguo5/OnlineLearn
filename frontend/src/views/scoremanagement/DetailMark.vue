<template>
    <div style="padding: 20px 20px 200px 20px;">
        <router-link to="/scoreManagment">
            <el-button type="success"> 返回</el-button>
        </router-link>
        <hr>
        <div style="">
            <mavon-editor :subfield="false" :autofocus="false" v-model=$route.params.data1 ref="md"
                style="width: 99%;height: 90vh;">
            </mavon-editor>
        </div>

        <div class="last" v-if="userDoExercise.id != null" style="margin-top: 35px">
            评分
            <el-tooltip class="item" effect="dark" :content="userDoExercise.score != null ? '该测验已批改！' : '输入分数评分'"
                placement="top-start">
                <el-input :disabled="userDoExercise.score != null" placeholder="请输入内容" v-model="updateUserDoExercise.score">

                </el-input>
            </el-tooltip>
        </div>
        <div style="float: right;margin-right: 3%;margin-top: 10px;padding-bottom: 30px;" v-if="userDoExercise.id != null">
            <el-tooltip class="item" effect="dark" :content="userDoExercise.score != null ? '该测验已批改！' : '提交'"
                placement="top-start">
                <el-button :disabled="userDoExercise.score != null" type="success" @click="updateScore()">
                    提交</el-button>
            </el-tooltip>
        </div>
    </div>

</template>


<script>

import { updateExerciseScore } from '../../api/teacher/test.js'
export default {
    name: "DetailMark",
    data() {
        return {
            check: false,
            userDoExercise: {
                id: "",
                score: null
            },
            updateUserDoExercise: {
                id: "",
                score: null
            }
        }
    },
    methods: {
        updateScore() {
            // 评分
            var pattern = /^\d+$/;
            if(!pattern.test(this.updateUserDoExercise.score) || this.updateUserDoExercise.score.trim() == "") {
                this.$message.error('请填写正确分数！');
                return
            }
            updateExerciseScore( this.userDoExercise.id, this.updateUserDoExercise.score).then(resp => {
                if (resp.data.code == 200) {
                    this.userDoExercise.score = this.updateUserDoExercise.score
                    this.$message.success('提交成功');
                } else {
                    this.$message.error('发布失败');
                }
            })
        }
    },
    mounted() {
        this.userDoExercise.score = this.$route.params.userDoExerciseScore
        this.userDoExercise.id = this.$route.params.userDoExerciseId
        this.updateUserDoExercise.score = this.$route.params.userDoExerciseScore
        this.updateUserDoExercise.id = this.$route.params.userDoExerciseId
    }
}
</script>

<style scoped>
</style>