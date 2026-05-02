<template>
    <div>
        <el-button type="primary" @click="addTeacher()"> 新增教师</el-button><br><br>
        <el-table :data="TeacherData" :default-sort="{ prop: 'date', order: 'descending' }" border style="width: 100%">
            <el-table-column sortable fixed prop="account" label="账号" width="180">
            </el-table-column>
            <el-table-column prop="userName" label="名称" width="180">
            </el-table-column>
            <el-table-column prop="sexName" label="性别" width="100">
            </el-table-column>
            <el-table-column label="班级" prop="className">
            </el-table-column>
            <el-table-column label="科目" prop="subjectName">
            </el-table-column>



            <el-table-column prop="createTime" label="入职时间">
            </el-table-column>

            <el-table-column label="操作" width="260" fixed="right">
                <template slot-scope="scope">
                    <el-button @click="handleEdit(scope.$index, scope.row)" type="success">
                        编辑
                    </el-button>
                    <el-button @click="handleDelete(scope.$index, scope.row)" type="danger"> 
                        删除
                    </el-button>
                </template>
            </el-table-column>
        </el-table>



        <el-dialog title="新增/编辑教师" :visible.sync="dialogFormVisible">
            <el-form :model="form">
                <el-form-item label="教师姓名" :label-width="formLabelWidth" prop="userName">
                    <el-input v-model="form.userEntity.userName" autocomplete="off"></el-input>
                </el-form-item>
                <el-form-item label="账号" :label-width="formLabelWidth" prop="account">
                    <el-input v-model="form.userEntity.account" autocomplete="off"></el-input>
                </el-form-item>
                <el-form-item label="教师电话" :label-width="formLabelWidth" prop="phone">
                    <el-input v-model="form.userEntity.phone" autocomplete="off"></el-input>
                </el-form-item>
                <el-form-item label="教师性别" :label-width="formLabelWidth" prop="sex">
                    <el-select v-model="form.userEntity.sex" placeholder="选择教师性别">
                        <el-option v-for="f in sexAll" :label="f.sexName" :value="f.sex" :key="f.sex"></el-option>

                    </el-select>
                </el-form-item>
                <el-form-item label="请选择班级" :label-width="formLabelWidth" prop="className">
                    <el-select v-model="form.classId" placeholder="选择班级">
                        <el-option v-for="f in queryCla" :label="f.className" :value="f.id" :key="f.id"></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="选择任教科目" :label-width="formLabelWidth" prop="className">
                    <el-select v-model="form.subjectId" placeholder="请选择科目">
                        <el-option v-for="f in AllSubject" :label="f.subjectName" :value="f.id" :key="f.id"></el-option>
                    </el-select>
                </el-form-item>

            </el-form>
            <div slot="footer" class="dialog-footer">
                <el-button @click="dialogFormVisible = false"> 取 消</el-button>
                <el-button type="primary" @click="submitadd(form)"> 确 定</el-button>
            </div>
        </el-dialog>




        <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange" :current-page="page.pageNum"
            :page-sizes="[10, 20, 30, 40]" :page-size="page.pageSize" layout="total, sizes, prev, pager, next, jumper"
            :total="TeacherData.length">
        </el-pagination>
    </div>
</template>

<script>
import { addStudent } from '../../../api/admin/astudent.js'
import { teacher, deleteTeacher, listSubject, updateSubject } from "../../../api/admin/teacher.js";
import { quclass } from "../../../api/admin/queryclass";

export default {
    name: "TeacherManagement",
    data() {
        return {
            dialogFormVisible: false,
            AllSubject: [],
            form: {
                userEntity: {
                    id: '',
                    userName: '',
                    phone: '',
                    sex: 0,
                    account: ''
                },
                subjectId: '',
                classId: '',
                roleId: 2,

            },
            sexAll: [
                {
                    sex: 1,
                    sexName: '女'
                },
                {
                    sex: 0,
                    sexName: '男'
                }
            ],
            queryCla: [],
            formLabelWidth: '120px',
            rules: {
                userName: [
                    { required: true, message: '请输入姓名', trigger: 'blur' },
                ],
                phone: [
                    { required: true, message: '请输入电话', trigger: 'change' },
                    { min: 11, max: 11, message: '电话长度为11位', trigger: 'blur' }
                ],

                sex: [
                    { required: true, message: '选择性别', trigger: 'change' },
                ],

                account: [
                    { required: true, message: '请输入账号', trigger: 'change' },
                    { min: 11, max: 11, message: '账号长度位8位', trigger: 'blur' }
                ],

                className: [
                    { required: true, message: '选择班级', trigger: 'change' },
                ],


            },
            TeacherData: [],
            page: {
                page: 1, //初始页
                pageSize: 10, //    每页的数据
                roleId: 2
            },
            paramId: {
                id: '',
            },
            param: {
                par: ''
            },
            tag: '123'
        }
    },

    created() {
        this.homework(this.page)
        this.queryClass(this.param)
        this.listAllSubject()
    },
    methods: {
        addTeacher() {
            this.dialogFormVisible = true
            this.tag = '888'
        },
        handleEdit(index, row) {
            this.dialogFormVisible = true
            this.form.userEntity.id = row.id
            this.form.userEntity.userName = row.userName
            this.form.userEntity.sex = row.sex
            this.form.userEntity.account = row.account
            this.form.userEntity.phone = row.phone
            this.form.subjectId = row.subjectId
            this.form.classId = row.classId
            this.tag = 'edit'

        },
        listAllSubject() {
            listSubject().then(resp => {
                this.AllSubject = resp.data.resultData
            })
        },
        handleDelete(index, row) {
            this.paramId.id = row.id
            deleteTeacher(this.paramId).then(resp => {
                if (resp.data.code == 200) {
                    this.$message({
                        message: '删除成功 ',
                        type: 'success'
                    });
                    this.homework(this.page)
                } else {
                    this.$message.error('删除失败');
                }
            })
        },
        check(form) {
            if(form.userEntity.userName.trim() == '') {
                this.$message.error('姓名不能为空');
                return false
            }
            if(form.userEntity.phone.trim() == '') {
                this.$message.error('手机号码不能为空');
                return false
            }
            if(form.userEntity.account.trim() == '') {
                this.$message.error('账号不能为空');
                return false
            }
            return true
        },
        submitadd(data1) {
            //进行表单验证
            if(!this.check(data1)) {
                return
            }
            this.dialogFormVisible = false
            console.log(data1, '123')
            if (this.tag == 'edit') {
                updateSubject(data1).then(resp => {
                    if (resp.data.code == 200) {
                        this.homework(this.page)
                        this.$message({
                            message: '编辑成功',
                            type: 'success'
                        });
                    } else {
                        this.$message.error('编辑失败');
                    }
                })
            } else {
                addStudent(data1).then(resp => {
                    if (resp.data.code == 200) {
                        this.homework(this.page)
                        this.$message({
                            message: '新增成功',
                            type: 'success'
                        });
                    } else {
                        this.$message.error('新增失败');
                    }
                })
            }

        },
        toggleSelection(rows) {
            this.$refs.multipleTable.clearSelection();
        },
        handleSelectionChange(val) {
            console.log(val, '8888')
            console.log(val.length, '8888999')
            for (let i = 0; i < val.length; i++) {
                console.log(val[i].id, '5555')
                this.form.classIds[i] = val[i].id;
            }

        },
        queryClass(pa) {
            quclass(pa).then(resp => {
                this.queryCla = resp.data.resultData
                console.log(this.queryCla, '7777')
            })
        },

        handleSizeChange(size) {
            this.page.pageSize = size;
            this.homework(this.page)
            // console.log(this.pageSize,'888')
            console.log(`每页 ${size} 条`);
        },
        handleCurrentChange(pageNum) {
            this.page.pageNum = pageNum;
            this.homework(this.page)
            console.log(`当前页: ${pageNum}`);
        },

        homework(page) {
            teacher(page).then(resp => {
                this.TeacherData = resp.data.resultData.data
                console.log(resp, '123')
                // state.ClassData=resp.data.data.records
                // this.$store.dispatch('classAction',this.ClassData)
            })
        }
    },
}
</script>

<style scoped>
.demo-table-expand {
    font-size: 0;
}

.demo-table-expand label {
    width: 90px;
    color: #99a9bf;
}

.demo-table-expand .el-form-item {
    margin-right: 0;
    margin-bottom: 0;
    width: 50%;
}
</style>