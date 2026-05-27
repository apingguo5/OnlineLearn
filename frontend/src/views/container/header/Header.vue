<template>
    <div>
        <el-menu :default-active="'1'" mode="horizontal" style="line-height: 60px; border-bottom: none;">
            <div class="cn">
                <div class="title">
                    <img src="@/assets/我的学习.png" alt="" style="width: 40px; height: 40px;position: relative;top: 5px;margin-left: 10px">
                    <div style="display: inline">
                        《智能终端应用开发》线上课程系统
                    </div>
                </div>
            </div>
        </el-menu>
    </div>
</template>

<script>
export default {
    name: "Header.vue"
}
</script>

<style scoped>
.cn {
    display: flex;
    justify-content: space-between;
    background-color: #9aa9e1;
}

.title {
    line-height: 100%;
    padding: 0 15px 15px 0;
    font-weight: bold;
    color: #1a1a2e;
    position: absolute;
    font-size: 24px;
}
</style>
