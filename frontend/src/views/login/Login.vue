<template>
  <div class="container">
    <div style="width: 60%; height: 60%; display: flex;background-color: #ffffff;box-shadow: 0 0 10px rgba(0,0,0,0.1);">
      <div style="width: 50%;" >
        <img src="../../assets/fang.png" width="100%" height="100%" style="overflow: hidden;box-sizing: border-box ;">
      </div>
      <div style="flex: 1;width: 50%; padding: 40px;display: flex;flex-direction: column;justify-content: center;" >
        <div style="text-align: center; font-size: 20px; margin-bottom: 30px; color: #09720e;font-weight: bold;">《智能终端应用开发》线上课程管理系统</div>

        <el-form :model="tabUser" style="background-color: #fff;opacity: 0.95;">

          <el-form-item>
            <el-input placeholder="账号" prefix-icon="iconfont icon-r-user2" v-model="tabUser.account"></el-input>
          </el-form-item>
          <!-- 密码 -->
          <el-form-item>
            <el-input placeholder="密码" show-password prefix-icon="iconfont icon-r-lock" v-model="tabUser.password"></el-input>
          </el-form-item>
          <div style="margin-top: 30px;"></div>
          <el-button type="" style="background-color: #09720e;color: #ffffff" @click="login(tabUser)">
            登录
          </el-button>
          <el-button type="" style="background-color: #09720e;color: #ffffff" @click="dialog = true">
            注册
          </el-button>
        </el-form>
      </div>
    </div>



        <el-drawer title="注册账号" :visible.sync="dialog" ref="drawer">
            <el-form :model="form" :rules="rules" style="padding: 0 20px 0 20px;">
                <el-form-item label="姓名" :label-width="formLabelWidth">
                    <el-input v-model="form.userName" autocomplete="off"></el-input>
                </el-form-item>
                <el-form-item label="账号" :label-width="formLabelWidth">
                    <el-input v-model="form.account" autocomplete="off"></el-input>
                </el-form-item>
                <el-form-item label="电话" :label-width="formLabelWidth">
                    <el-input v-model="form.phone" autocomplete="off"></el-input>
                </el-form-item>

                <el-form-item label="密码" :label-width="formLabelWidth" prop="password">
                    <el-input v-model="form.password" type="password" placeholder="请输入密码"></el-input>
                </el-form-item>
                <el-form-item label="确认密码" :label-width="formLabelWidth" prop="confirmPassword">
                    <el-input type="password" placeholder="请再次输入密码"
                        v-model="form.confirmPassword"></el-input>
                </el-form-item>
                <el-form-item label="性别" :label-width="formLabelWidth">
                    <el-select v-model="form.sex" placeholder="请选择性别">
                        <el-option label="男" value="0"></el-option>
                        <el-option label="女" value="1"></el-option>
                    </el-select>
                </el-form-item>
            </el-form>
            <div style="width: 100%; text-align: center">
                <el-button type="success" @click="confirm(form)"> 注册新账号</el-button>
            </div>
        </el-drawer>



    </div>
</template>
<script>

import { loginRequest, register } from "../../api/login";
import Cookies from 'js-cookie'
export default {
    name: "Login",
    data() {
        var validatePass2 = (rule, value, callback) => {
            if (value === '') {
                callback(new Error('请再次输入密码'))
            } else if (value !== this.form.password) {
                callback(new Error('两次输入密码不一致!'))
            } else {
                callback()
            }
        }
        return {
            tabUser: {
                account: '',
                password: '',
            },
            form: {
                userName: '',
                account: '',
                sex: '',
                phone: '',
                password: '',
                confirmPassword: '',
                desc: ''
            },
            formLabelWidth: '80px',
            timer: null,
            dialog: false,
            loading: false,

            rules: {
                password: [
                    { required: true, message: '请输入密码', trigger: 'blur' },
                ],
                confirmPassword: [
                    { required: true, validator: validatePass2, trigger: 'blur' }
                ]
            }
        }

    },
    created() {

    },
    methods: {

        confirm(form) {

            if (this.form.userName.trim() == '') {
                this.$message({
                    message: '姓名不能为空',
                    type: 'error'
                });
                return
            }
            if (this.form.account.trim() == '') {
                this.$message({
                    message: '账号不能为空',
                    type: 'error'
                });
                return
            }
            if (this.form.sex === '' || this.form.sex == null) {
                this.$message({
                    message: '请选择性别',
                    type: 'error'
                });
                return
            }
            if (this.form.phone.trim() == '') {
                this.$message({
                    message: '电话不能为空',
                    type: 'error'
                });
                return
            }
            if (this.form.password.trim() == '') {
                this.$message({
                    message: '密码不能为空',
                    type: 'error'
                });
                return
            }
            if (this.form.password !== this.form.confirmPassword) {
                this.$message({
                    message: '两次输入密码不一致',
                    type: 'error'
                });
                return
            }
            // 构造注册参数，不包含 confirmPassword
            const registerData = {
                userName: this.form.userName,
                account: this.form.account,
                sex: parseInt(this.form.sex),
                phone: this.form.phone,
                password: this.form.password
            };
            register(registerData).then(resp => {
                if (resp.data.code == 200) {
                    this.dialog = false;
                    this.$message({
                        message: '注册成功',
                        type: 'success'
                    });
                    // 清空注册表单
                    this.form = {
                        userName: '',
                        account: '',
                        sex: '',
                        phone: '',
                        password: '',
                        confirmPassword: '',
                        desc: ''
                    };
                } else {
                    this.$message.error(resp.data.resultData || '注册失败，账号已存在');
                }
            }).catch(error => {
                this.$message.error('注册失败：' + error.message);
            })
        },
        cancelForm() {
            this.loading = false;
            this.dialog = false;
            clearTimeout(this.timer);
        },


        sendCode() {

        },
        login(tabUser) {
            // 前端验证
            if (!tabUser.account || tabUser.account.trim() === '') {
                this.$message({
                    message: '请输入账号',
                    type: 'error'
                });
                return;
            }
            if (!tabUser.password || tabUser.password.trim() === '') {
                this.$message({
                    message: '请输入密码',
                    type: 'error'
                });
                return;
            }

            loginRequest(tabUser).then(resp => {
                const data = resp.data;
                if (data.code == 200) {
                    const result = data.resultData;
                    const roleId = result.roleId;
                    Cookies.set('roleId', roleId)
                    Cookies.set('userId', result.userId)
                    Cookies.set('classId', result.classId)

                    this.$message({
                        message: '登录成功',
                        type: 'success'
                    });

                    if (roleId == 1) {
                        this.$router.push("/adminHome")
                    } else if (roleId == 2) {
                        this.$router.push("/teacherHome")
                    } else if (roleId == 3) {
                        this.$router.push("/studentweb")
                    } else {
                        this.$message({
                            message: '未配置角色权限（roleId=' + roleId + '），请联系管理员',
                            type: 'warning'
                        });
                    }
                } else {
                    this.$message.error(data.resultData || '账号或者密码错误');
                }
            }).catch(error => {
                console.error('Login error:', error);
                this.$message({
                    showClose: true,
                    message: error.message,
                    type: "error",
                    duration: 20000,
                });
            })

        },

    }
}
</script>
<style scoped>
.onfont[data-v-2529d779] {
    /* display: flex; */
    /* justify-content: center; */
    position: absolute;
    left: 50%;
    top: 120px;
}

.lr {
    width: 100%;
    display: flex;
    justify-content: space-between;
}




.login_box {
    text-align: center;
    padding-top: 70px;


}

.lf {
    padding: 20px;
    position: absolute;
    bottom: 0;
    width: 100%;
    box-sizing: border-box;
}

.el-button {
    width: 48%;
}

.code {
    width: 45%;
}

.code1 {
    /* style="width: 100px; height: 30px; margin-left:5px;vertical-align: middle;" */
    display: line-inline;
    width: 45%;
    height: 28px;
    margin-left: 10px;
    vertical-align: middle;
    border-radius: 3px;
}

.rememberMe {
    color: #fff;
}

.container {
  height: 100vh;
  overflow: hidden;
  background-image: url("../../assets/jia.png");
  background-size: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #666;
}
</style>