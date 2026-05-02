<template>
    <div style="overflow-y: auto;">

        <div>

            <div class="cls">
                <div v-if="tag = 'homework'">
                    <p>班级:</p>
                    <div>
                        <el-select v-model="paramData.classId" filterable placeholder="请选择">
                            <el-option v-for="item in queryCla" :key="item.classId" :label="item.className"
                                :value="item.classId">
                            </el-option>
                        </el-select>
                    </div>
                </div>

                <div>
                    <p>{{ type }}标题：</p>
                    <div>
                        <el-input style="width: 80%" placeholder="请输入标题" v-model="paramData.title" clearable>
                        </el-input>
                    </div>
                </div>

                <div v-if="tag != 'test'">
                    <p class="demonstration">完成时间</p>
                    <div>
                        <el-date-picker v-model="paramData.commitTime" value-format="yyyy-MM-dd HH:mm:ss" type="date"
                            placeholder="选择日期时间" align="right">
                        </el-date-picker>
                    </div>
                </div>
            </div>
            <div class="ce">


                <div style="width: 45%;">
                    <p>正题</p>
                    <div>
                        <mavon-editor :subfield="false" :autofocus="false" v-model="paramData.content" ref="md"
                            style="width: 99%;height: 50vh;">
                        </mavon-editor>
                    </div>
                </div>

                <div style="width: 45%">
                    <p> 参考答案</p>
                    <div>
                        <mavon-editor :subfield="false" :autofocus="false" v-model="paramData.answer" ref="md"
                            style="width: 99%;height: 50vh;">
                        </mavon-editor>
                    </div>
                </div>
            </div>
            <div class="last" v-if="check">

                评分
                <el-tooltip class="item" effect="dark" :content="mode == '已批改' ? '该作业已批改！' : '输入分数评分'" placement="top-start">
                    <el-input :disabled="mode == '已批改'" placeholder="请输入内容" v-model="paramData.score">

                    </el-input>
                </el-tooltip>
                <span style="margin-top: 10px;display: block;"> 备注</span>


                <el-input :disabled="mode == '已批改'" type="textarea" :rows="3" style="" placeholder="请输入"
                    v-model="paramData.remark">
                </el-input>
            </div>
            <div style="float: right;margin-right: 3%;margin-top: 10px;padding-bottom: 30px;">
                <el-button type="danger" @click="out()"> 返回</el-button>
                <el-tooltip class="item" effect="dark" :content="mode == '已批改' ? '该作业已批改！' : '提交'" placement="top-start">
                    <el-button :disabled="mode == '已批改'" type="success" @click="addTest(paramData)"> 提交</el-button>
                </el-tooltip>
            </div>
        </div>
    </div>
</template>

<script>
import { pclass } from "../../api/admin/queryclass";
import { addt, saveHomework } from '../../api/teacher/test.js'
import { put } from '../../api/request'
import Cookies from 'js-cookie'

export default {
    name: "Detail",
    data() {
        return {
            check: false,

            mode: 0,
            queryCla: [],
            paramData: {
                id: '',
                classId: '',
                title: '',
                content: '',
                commitTime: '',
                answer: '',
                creator: '',
                userId: '',
                score: 0,
                remark: ""
            },

            param: {
                userId: '',
            },
            tag: '',
            studenttag: '',
            type: "试题"
        }
    },
    computed: {

    },
    created() {
        this.paramData.creator = Cookies.get("userId")
        this.param.userId = Cookies.get("userId")
        this.queryClass(this.param)
        this.type = this.$route.query.type
        this.tag = this.$route.params.data1
        this.check = this.$route.params.check
        if (this.$route.params.data2) {
            this.paramData.userId = this.$route.params.data2.userId
            // this.paramData.id = this.$route.params.data2.id
            this.paramData.id = this.$route.params.data2.homeworkId
            this.paramData.title = this.$route.params.data2.title
            this.paramData.content = this.$route.params.data2.content
            this.paramData.answer = this.$route.params.data2.answer
            this.paramData.commitTime = this.$route.params.data2.commitTime
            this.paramData.commitTime = this.$route.params.data2.completionTime
            this.paramData.classId = this.$route.params.data2.classId
            this.studenttag = this.$route.params.data4
            this.mode = this.$route.params.data2.mode
            this.paramData.remark = this.$route.params.data2.remark
            this.paramData.score = this.$route.params.data2.score
        }
        // 处理修改试题的逻辑
        if (this.$route.params.data1 && this.$route.query.id) {
            this.paramData.id = this.$route.query.id
            this.paramData.title = this.$route.params.data1.title
            this.paramData.content = this.$route.params.data1.content
            this.paramData.answer = this.$route.params.data1.answer
            this.paramData.classId = this.$route.params.data1.classId
        }

    },
    methods: {
        out() {
            this.tag = this.$route.params.data1
            if (this.tag == 'homework') {
                console.log(this.tag, '888')
                this.$router.push('/classmanagement')
            } else {
                this.$router.push('/scoremanagment')
            }
        },
        queryClass(pa) {
            pclass(pa).then(resp => {
                this.queryCla = resp.data.resultData
            })
        },
        addTest(paramData) {
            this.tag = this.$route.params.data1
            if (this.tag == 'homework') {

                saveHomework(paramData).then(resp => {
                    if (resp.data.code == 200) {
                        this.$router.push("/classmanagement")
                        this.$message({
                            message: '发布成功',
                            type: 'success'
                        });
                    } else {
                        this.$message.error('发布失败');
                    }
                })
            } else {
                // 处理修改试题的逻辑
                if (paramData.id) {
                    // 只传递后端实体中存在的字段
                    const exerciseData = {
                        id: paramData.id,
                        title: paramData.title,
                        content: paramData.content,
                        answer: paramData.answer,
                        creator: paramData.creator,
                        classId: paramData.classId
                    }
                    put('/study/exercises/update', exerciseData).then(resp => {
                        if (resp.data.code == 200) {
                            this.$router.push("/scoremanagment")
                            this.$message({
                                message: '修改成功',
                                type: 'success'
                            });
                        } else {
                            this.$message.error('修改失败');
                        }
                    })
                } else {
                    addt(paramData).then(resp => {
                        if (resp.data.code == 200) {
                            this.$router.push("/scoremanagment")
                            this.$message({
                                message: '发布成功',
                                type: 'success'
                            });
                        } else {
                            this.$message.error('发布失败');
                        }
                    })
                }
            }

        },
    }
}
</script>

<style scoped>
.el-input__inner {
    margin-top: 10px;
}

.el-textarea__inner {
    margin-top: 10px;
}

.last {
    width: 95%;
    height: 200px;
    margin: 20px auto;

}

.ce {
    display: flex;
    justify-content: space-around;


}

.cls {
    display: flex;
    justify-content: space-around;
    flex-wrap: wrap;
}
</style>