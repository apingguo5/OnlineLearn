<template>
    <div>
    <div class="avatar-section">
        <div class="avatar-display">
            <img v-if="avatarUrl" :src="avatarUrl" class="avatar-img" alt="头像" />
            <div v-else class="avatar-placeholder">
                <i class="el-icon-user-solid"></i>
            </div>
        </div>
        <div class="avatar-actions">
            <el-button type="primary" size="small" @click="triggerUpload">
                <i class="el-icon-upload2"></i> 上传头像
            </el-button>
            <el-button v-if="avatarUrl" type="danger" size="small" @click="removeAvatar">
                <i class="el-icon-delete"></i> 删除头像
            </el-button>
            <input ref="avatarInput" type="file" accept="image/*" style="display:none" @change="onAvatarChange" />
            <p class="avatar-tip">支持 jpg/png 格式，建议 2MB 以内</p>
        </div>
    </div>

    <el-descriptions class="margin-top" title="用户信息" :column="3" :size="size" border>
        <template slot="extra">
            <el-button type="primary" size="small" @click="alertpersonal()"> 编辑</el-button>
        </template>
        <el-descriptions-item>
            <template slot="label">
                <i class="el-icon-user"></i>
                用户名
            </template>
            {{Info.userName}}
        </el-descriptions-item>
        <el-descriptions-item>
            <template slot="label">
                <i class="el-icon-mobile-phone"></i>
                手机号
            </template>
            {{Info.phone}}
        </el-descriptions-item>
        <el-descriptions-item v-if="Info.sex==0">
            <template slot="label">
                <i class="el-icon-tickets"></i>
                性别
            </template>
            <el-tag size="small">男</el-tag>
        </el-descriptions-item>
        <el-descriptions-item v-if="Info.sex==1">
            <template slot="label">
                <i class="el-icon-tickets"></i>
                性别
            </template>
            <el-tag size="small">女</el-tag>
        </el-descriptions-item>
        <el-descriptions-item>
            <template slot="label">
                <i class="el-icon-office-building"></i>
                创建时间
            </template>
            {{Info.createTime}}
        </el-descriptions-item>
        <el-descriptions-item>
            <template slot="label">
                <i class="el-icon-s-school"></i>
                班级
            </template>
            {{Info.className || '未加入班级'}}
        </el-descriptions-item>
    </el-descriptions>

        <el-dialog title="修改个人信息" :visible.sync="dialogFormVisible">
            <el-form :model="Info" :rules="rules">
                <el-form-item label="姓名" :label-width="formLabelWidth" prop="userName">
                    <el-input v-model="Info.userName" autocomplete="off"></el-input>
                </el-form-item>
                <el-form-item label="账号" :label-width="formLabelWidth" prop="userName">
                    <el-input v-model="Info.account" autocomplete="off" disabled></el-input>
                </el-form-item>
                <!--                <el-form-item label="账号" :label-width="formLabelWidth" prop="account">-->
                <!--                    <el-input v-model="Info.account" autocomplete="off"></el-input>-->
                <!--                </el-form-item>-->
                <el-form-item label="电话" :label-width="formLabelWidth" prop="phone">
                    <el-input v-model="Info.phone" autocomplete="off"></el-input>
                </el-form-item>
                <!-- <el-form-item label="入校时间" :label-width="formLabelWidth" prop="phone">
                    <el-input v-model="Info.createTime" autocomplete="off"></el-input>
                </el-form-item> -->

            </el-form>
            <div slot="footer" class="dialog-footer">
                <el-button @click="dialogFormVisible = false"> 取 消</el-button>
                <el-button type="primary" @click="submit(Info)"> 确 定</el-button>
            </div>
        </el-dialog>
    </div>
</template>

<script>
    import {personal} from '../../../api/personal.js'
    import Cookies from 'js-cookie'
    import {listAllClass} from "../../../api/alertpersonal";
    export default {
        name: "EssentiaInfo",
        data(){
            return{
                dialogFormVisible:false,
                Info:[],
                userId:{
                    id:'',
                },
                avatarUrl: ''
            }
        },
        created() {
            this.userId.id=Cookies.get("userId")
            this.listPersonal(this.userId)
            this.loadAvatar()
        },
        methods:{
            loadAvatar() {
                const uid = Cookies.get('userId')
                if (uid) {
                    this.avatarUrl = localStorage.getItem('user_avatar_' + uid) || ''
                }
            },
            triggerUpload() {
                this.$refs.avatarInput.value = ''
                this.$refs.avatarInput.click()
            },
            onAvatarChange(e) {
                const file = e.target.files && e.target.files[0]
                if (!file) return
                if (!/^image\//.test(file.type)) {
                    this.$message.error('请选择图片文件')
                    return
                }
                if (file.size > 2 * 1024 * 1024) {
                    this.$message.warning('图片过大，请选择 2MB 以内的图片')
                    return
                }
                const reader = new FileReader()
                reader.onload = () => {
                    const base64 = reader.result
                    const uid = Cookies.get('userId')
                    if (!uid) {
                        this.$message.error('未获取到用户ID，无法保存头像')
                        return
                    }
                    try {
                        localStorage.setItem('user_avatar_' + uid, base64)
                        this.avatarUrl = base64
                        this.$root.$emit('avatar-updated')
                        this.$message.success('头像更新成功')
                    } catch (err) {
                        this.$message.error('保存头像失败：' + err.message)
                    }
                }
                reader.onerror = () => {
                    this.$message.error('读取图片失败')
                }
                reader.readAsDataURL(file)
            },
            removeAvatar() {
                const uid = Cookies.get('userId')
                if (uid) {
                    localStorage.removeItem('user_avatar_' + uid)
                }
                this.avatarUrl = ''
                this.$root.$emit('avatar-updated')
                this.$message.success('头像已删除')
            },
            listPersonal(userId){
                personal(userId).then(resp=>{
                    this.Info=resp.data.resultData
                })
            },
            alertpersonal(){
                this.dialogFormVisible=true
            },
            submit(da){
                listAllClass(da).then(resp=>{
                    if(resp.data.code==200){
                        this.$message({
                            message: '编辑成功 ',
                            type: 'success'
                        });
                        this.dialogFormVisible=false
                        this.studentquery(this.page)
                    }else{
                        this.$message.error('删除失败');
                    }
                })
            },
        }
    }
</script>

<style scoped>
.avatar-section {
    display: flex;
    align-items: center;
    padding: 20px;
    margin-bottom: 20px;
    background: #f7f9fc;
    border-radius: 8px;
}
.avatar-display {
    margin-right: 24px;
}
.avatar-img {
    width: 96px;
    height: 96px;
    border-radius: 50%;
    object-fit: cover;
    border: 2px solid #ffffff;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
}
.avatar-placeholder {
    width: 96px;
    height: 96px;
    border-radius: 50%;
    background: #e6e8eb;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #8e99b0;
    font-size: 56px;
}
.avatar-actions {
    display: flex;
    flex-direction: column;
    gap: 8px;
}
.avatar-tip {
    margin: 8px 0 0 0;
    font-size: 12px;
    color: #909399;
}
</style>