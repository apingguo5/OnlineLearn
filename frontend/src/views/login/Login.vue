<template>
  <div class="container">
    <div class="login-wrapper">
      <!-- 左侧装饰 -->
      <div class="login-left">
        <div class="left-content">
          <div class="logo-area">
            <div class="logo-icon">
              <i class="el-icon-reading"></i>
            </div>
            <h1 class="system-title">Online Learn</h1>
          </div>
          <div class="system-desc">
            <p>统一登录入口</p>
            <p class="desc-sub">根据账号角色自动跳转对应页面</p>
          </div>
        </div>
      </div>

      <!-- 右侧登录表单 -->
      <div class="login-right">
        <div class="login-form-container">
          <!-- 标题 -->
          <div class="form-header">
            <div class="role-icon-badge">
              <i class="el-icon-user"></i>
            </div>
            <h2 class="form-title">用户登录</h2>
            <p class="form-subtitle">请输入账号密码登录</p>
          </div>

          <el-form :model="tabUser" class="login-form" @keyup.enter.native="login(tabUser)">
            <el-form-item>
              <el-input
                placeholder="请输入账号"
                prefix-icon="iconfont icon-r-user2"
                v-model="tabUser.account"
                size="large"
              ></el-input>
            </el-form-item>
            <el-form-item>
              <el-input
                placeholder="请输入密码"
                show-password
                prefix-icon="iconfont icon-r-lock"
                v-model="tabUser.password"
                size="large"
              ></el-input>
            </el-form-item>
            <el-form-item>
              <el-button
                class="login-btn"
                type="primary"
                :loading="loading"
                @click="login(tabUser)"
                size="large"
              >
                {{ loading ? '登录中...' : '登 录' }}
              </el-button>
            </el-form-item>
          </el-form>

          <div class="register-link">
            <span>还没有账号？</span>
            <a href="javascript:;" @click="dialog = true">立即注册</a>
          </div>
        </div>
      </div>
    </div>

    <!-- 注册抽屉 -->
    <el-drawer title="注册账号" :visible.sync="dialog" ref="drawer" size="420px">
      <el-form :model="form" :rules="rules" style="padding: 0 20px;">
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
          <el-select v-model="form.sex" placeholder="请选择性别" style="width:100%">
            <el-option label="男" value="0"></el-option>
            <el-option label="女" value="1"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="角色" :label-width="formLabelWidth">
          <el-select v-model="form.roleId" placeholder="请选择角色" @change="onRoleChange" style="width:100%">
            <el-option v-for="item in roleList" :key="item.value" :label="item.label" :value="item.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="班级" :label-width="formLabelWidth" v-if="form.roleId === 3">
          <el-select v-model="form.classId" placeholder="请选择班级" filterable style="width:100%">
            <el-option v-for="item in classList" :key="item.id" :label="item.className" :value="item.id"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <div style="width: 100%; text-align: center; padding: 10px 0;">
        <el-button type="success" @click="confirm(form)" size="medium" style="width:80%">注册新账号</el-button>
      </div>
    </el-drawer>
  </div>
</template>

<script>
import { loginRequest, register } from "../../api/login";
import { listAllClass } from "../../api/class";
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
      loading: false,
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
        desc: '',
        roleId: 3,
        classId: ''
      },
      formLabelWidth: '80px',
      timer: null,
      dialog: false,
      classList: [],
      roleList: [
        { value: 3, label: '学生' },
        { value: 2, label: '教师' }
      ],
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
    this.loadClassList();
  },
  methods: {
    onRoleChange(value) {
      if (value === 2) {
        this.form.classId = '';
      }
    },
    loadClassList() {
      listAllClass({}).then(resp => {
        if (resp.data && resp.data.code === 200) {
          this.classList = resp.data.resultData || [];
        }
      }).catch(() => {});
    },
    confirm(form) {
      if (this.form.userName.trim() == '') {
        this.$message({ message: '姓名不能为空', type: 'error' });
        return
      }
      if (this.form.account.trim() == '') {
        this.$message({ message: '账号不能为空', type: 'error' });
        return
      }
      if (this.form.sex === '' || this.form.sex == null) {
        this.$message({ message: '请选择性别', type: 'error' });
        return
      }
      if (this.form.phone.trim() == '') {
        this.$message({ message: '电话不能为空', type: 'error' });
        return
      }
      if (this.form.password.trim() == '') {
        this.$message({ message: '密码不能为空', type: 'error' });
        return
      }
      if (this.form.password !== this.form.confirmPassword) {
        this.$message({ message: '两次输入密码不一致', type: 'error' });
        return
      }
      const registerData = {
        userName: this.form.userName,
        account: this.form.account,
        sex: parseInt(this.form.sex),
        phone: this.form.phone,
        password: this.form.password,
        roleId: parseInt(this.form.roleId)
      };
      if (this.form.roleId === 3 && this.form.classId) {
        registerData.classId = parseInt(this.form.classId);
      }
      register(registerData).then(resp => {
        if (resp.data.code == 200) {
          this.dialog = false;
          this.$message({ message: '注册成功', type: 'success' });
          this.form = {
            userName: '', account: '', sex: '', phone: '',
            password: '', confirmPassword: '', desc: '',
            roleId: 3, classId: ''
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
    sendCode() {},
    login(tabUser) {
      if (!tabUser.account || tabUser.account.trim() === '') {
        this.$message({ message: '请输入账号', type: 'error' });
        return;
      }
      if (!tabUser.password || tabUser.password.trim() === '') {
        this.$message({ message: '请输入密码', type: 'error' });
        return;
      }

      this.loading = true
      loginRequest(tabUser).then(resp => {
        this.loading = false
        const data = resp.data;
        if (data.code == 200) {
          const result = data.resultData;
          const roleId = result.roleId;
          Cookies.set('roleId', roleId)
          Cookies.set('userId', result.userId)
          Cookies.set('classId', result.classId)
          // 同时存储到 localStorage，供其他页面使用
          localStorage.setItem('userId', result.userId)
          localStorage.setItem('roleId', roleId)
          localStorage.setItem('userName', result.userName || '')

          this.$message({ message: '登录成功', type: 'success' });

          if (roleId == 1) {
            this.$router.push("/adminHome")
          } else if (roleId == 2) {
            this.$router.push("/teacherdashboard")
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
        this.loading = false
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
.container {
  height: 100vh;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f2f5;
}

.login-wrapper {
  width: 900px;
  height: 580px;
  display: flex;
  background-color: #ffffff;
  border-radius: 20px;
  box-shadow: 0 4px 40px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

/* ===================== 左侧装饰区域 ===================== */
.login-left {
  width: 420px;
  background: #f8fafc;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  flex-shrink: 0;
  border-right: 1px solid #eef0f4;
}

.left-content {
  text-align: center;
}

.logo-area {
  margin-bottom: 40px;
}

.logo-icon {
  width: 80px;
  height: 80px;
  background: #eef2ff;
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
}

.logo-icon i {
  font-size: 40px;
  color: #4e6ef2;
}

.system-title {
  font-size: 22px;
  font-weight: 700;
  line-height: 1.5;
  margin: 0;
  color: #1a1a2e;
  letter-spacing: 0.5px;
}

.system-desc {
  margin-top: 20px;
}

.system-desc p {
  font-size: 16px;
  font-weight: 600;
  color: #4a5568;
  margin: 0;
}

.system-desc .desc-sub {
  font-size: 13px;
  font-weight: 400;
  color: #8e99b0;
  margin-top: 8px;
}

/* ===================== 右侧登录表单 ===================== */
.login-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.login-form-container {
  width: 100%;
  max-width: 360px;
}

.form-header {
  text-align: center;
  margin-bottom: 32px;
}

.role-icon-badge {
  width: 56px;
  height: 56px;
  background: #f0f5ff;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
}

.role-icon-badge i {
  font-size: 26px;
  color: #4e6ef2;
}

.form-title {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0 0 8px;
  letter-spacing: 0.5px;
}

.form-subtitle {
  font-size: 14px;
  color: #8e99b0;
  margin: 0;
}

.login-form {
  margin-top: 28px;
}

.login-form .el-input {
  width: 100%;
}

.login-btn {
  width: 100%;
  height: 46px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 4px;
  border-radius: 10px;
  margin-top: 4px;
}

.register-link {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: #8e99b0;
}

.register-link a {
  color: #4e6ef2;
  text-decoration: none;
  font-weight: 600;
  margin-left: 4px;
}

.register-link a:hover {
  color: #3b5de7;
}

/* 覆盖elementui的按钮颜色 */
.login-btn.el-button--primary {
  background: #4e6ef2;
  border-color: #4e6ef2;
  box-shadow: 0 2px 8px rgba(78, 110, 242, 0.25);
}
.login-btn.el-button--primary:hover {
  background: #3b5de7;
  border-color: #3b5de7;
  box-shadow: 0 4px 14px rgba(78, 110, 242, 0.35);
}

/* 全屏背景色覆盖 */
.el-drawer {
  overflow-y: auto;
}
</style>