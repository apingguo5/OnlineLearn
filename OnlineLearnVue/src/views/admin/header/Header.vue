<template>
    <div>
        <el-menu :default-active="1" mode="horizontal" style="line-height: 60px">
            <div class="cn">
                <div class="title">
                  <img src="@/assets/我的学习.png" alt="" style="width: 40px; height: 40px;position: relative;top: 5px;margin-left: 10px">

                  <div style="display: inline">
                    《智能终端应用开发》线上课程系统
                  </div>
                </div>

                <div class="blockl">
                    <el-submenu index="2">
                        <template slot="title">
                            <div class="demo-fit">
                                <div class="block">
                                    <i class="el-icon-user-solid"
                                        style="color: white;margin-right: 25px;font-size: 32px;"></i>
                                </div>
                            </div>
                        </template>
                        <el-menu-item index="/checkhomework" @click="logout()">退出</el-menu-item>
                        <el-menu-item @click="change()"> 修改密码</el-menu-item>
                    </el-submenu>
                </div>
            </div>
        </el-menu>


        <div>
            <el-dialog title="修改密码" :visible.sync="dialogFormVisible" width="30%" :before-close="handleClose">
                <el-input placeholder="请输入原密码" v-model="changePassword.password" show-password></el-input>
                <el-input placeholder="请输入新密码" v-model="changePassword.newPassword" show-password></el-input>
                <el-input placeholder="请重新输入新密码" v-model="changePassword.conPassword" show-password></el-input>
                <span slot="footer" class="dialog-footer">
                    <el-button @click="dialogFormVisible = false"> 取 消</el-button>
                    <el-button type="primary" @click="submit(changePassword)"> 确 定</el-button>
                </span>
            </el-dialog>


        </div>

    </div>
</template>

<script>
import Cookies from "js-cookie";
import { password } from '../../../api/personal.js'

export default {
    name: "Header.vue",
    data() {
        return {

            changePassword: {
                password: '',
                conPassword: '',
                newPassword: '',
                id: ''
            },
            dialogFormVisible: false,
            info: {
                password: '',
                newPassword: '',
                id: ''
            },
            drawer: false
        }
    },
    created() {
        this.changePassword.id = Cookies.get("userId")
    },
    methods: {
        handleClose(done) {
            this.$confirm('确认关闭？')
                .then(() => {
                    done();
                })
                .catch(() => { });
        },
        change() {
            this.dialogFormVisible = true
        },

        submit(da) {
            if (this.changePassword.password.trim() == '') {

                this.$message({
                    message: '请输入原密码',
                    type: 'error'
                });
                return;
            }
            if (this.changePassword.newPassword.trim() == '') {

                this.$message({
                    message: '请输入新密码',
                    type: 'error'
                });
                return;
            }
            if (this.changePassword.conPassword != this.changePassword.newPassword) {

                this.$message({
                    message: '两次新密码不一致',
                    type: 'error'
                });
                return;
            }
            password(da).then(resp => {
                if (resp.data.code == 200) {
                    this.$message({
                        message: '密码修改成功 ',
                        type: 'success'
                    });
                    this.dialogFormVisible = false
                    this.studentquery(this.page)
                } else {
                    this.$message.error('原密码错误');
                }
            })
        },
        logout() {
            Cookies.remove('userId')
            Cookies.remove('classId')
            Cookies.remove('roleId')
            this.$router.push('/login')
            this.$message({
                message: '退出成功',
                type: 'success'
            });
        }
    }
}
</script>

<style scoped>
.block {
    height: 60px;
}

.blockl {
    position: absolute;
    right: 0px;
    line-height: 60px;
}

.ls {
    height: 100%;
}


.title {
    line-height: 100%;
    padding: 0 15px 15px 0;
    font-weight: bold;
    color: #ffffff;
    position: absolute;
    font-size: 24px;
}
</style>