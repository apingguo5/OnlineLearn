<template>
    <div>
        <el-menu
            :default-active="activeMenu"
            class="el-menu-vertical-demo"
            background-color="#ffffff"
            text-color="#4a5568"
            active-text-color="#4e6ef2"
            router
        >
            <el-menu-item index="/studenthome">
                <i class="el-icon-s-home"></i>
                <span slot="title">首页</span>
            </el-menu-item>

            <el-menu-item index="/courses">
                <i class="el-icon-notebook-2"></i>
                <span slot="title">正在学习</span>
            </el-menu-item>

            <el-menu-item index="/studenthomeworkexam">
                <i class="el-icon-edit-outline"></i>
                <span slot="title">作业考试</span>
            </el-menu-item>

            <el-menu-item index="/askandanswer">
                <i class="el-icon-chat-dot-round"></i>
                <span slot="title">问答社区</span>
            </el-menu-item>
        </el-menu>
    </div>
</template>

<script>
export default {
    name: "StudentAside",
    computed: {
        activeMenu() {
            return this.$route.path;
        }
    }
}
</script>

<style scoped>
.el-menu-vertical-demo {
    width: 200px;
    min-height: calc(100vh - 60px);
    border-right: 1px solid #eef0f4;
}
.el-menu-item {
    font-size: 14px;
    color: #4a5568;
}
.el-menu-item i {
    font-size: 18px;
    margin-right: 5px;
}
.el-menu-item.is-active {
    color: #4e6ef2 !important;
    background-color: #f0f5ff !important;
}
.el-menu-item:hover {
    background-color: #f5f7fa !important;
}
</style>