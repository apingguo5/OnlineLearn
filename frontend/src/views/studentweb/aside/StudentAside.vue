<template>
    <div>
        <el-menu
            :default-active="activeMenu"
            class="el-menu-vertical-demo"
            background-color="#324157"
            text-color="#fff"
            active-text-color="#ffd04b"
            router
        >
            <el-menu-item index="/studenthome">
                <i class="el-icon-s-home"></i>
                <span slot="title">首页</span>
            </el-menu-item>

            <el-menu-item index="/courses">
                <i class="el-icon-reading"></i>
                <span slot="title">课程</span>
            </el-menu-item>

            <el-menu-item index="/studentpractice">
                <i class="el-icon-edit-outline"></i>
                <span slot="title">作业习题</span>
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
    border-right: none;
}
.el-menu-item {
    font-size: 14px;
}
.el-menu-item i {
    font-size: 18px;
    margin-right: 5px;
}
</style>