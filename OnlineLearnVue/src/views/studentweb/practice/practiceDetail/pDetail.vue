<template>
    <div style="width: 90%;height: 200%; text-align: center;overflow-x: hidden;padding: 40px 40px 60vh 40px;">
        <router-link to="/practicedetail"> <el-button type="primary"> 返回</el-button></router-link>
        <hr>
        <p>试题</p>
        <div style="width: 95%;">
            <mavon-editor :subfield="false" :autofocus="false" v-model="paramData.reply" ref="md" style="width: 99%;">
            </mavon-editor>
        </div>
        <br>
        <p> 参考答案</p>
        <div style="width: 95%">
            <mavon-editor :subfield="false" :autofocus="false" v-model="paramData.answer" ref="md" style="width: 99%;">
            </mavon-editor>
        </div>
    </div>
</template>

<script>
export default {
    name: "pDetail",
    data() {
        return {
            paramData: {
                reply: '',
                answer: '',
            },
        }
    },
    created() {
        this.paramData.reply = this.$route.params.data2.reply
        this.paramData.answer = this.$route.params.data2.answer
    }
}
</script>

<style scoped>
.es {
    display: flex;
    justify-content: space-between;
    padding: 10px;
}
</style>