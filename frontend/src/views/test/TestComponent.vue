<template>
    <div>
        <h1>测试组件</h1>
        <div style="margin: 20px 0; padding: 10px; background-color: #f0f0f0;">{{ message }}</div>
        <button @click="testDirect">直接测试</button>
        <div v-if="debugInfo.length > 0">
            <h3>调试信息：</h3>
            <div v-for="(info, index) in debugInfo" :key="index" style="margin: 5px 0; padding: 5px; background-color: #e0e0e0;">
                {{ info }}
            </div>
        </div>
    </div>
</template>

<script>
export default {
    name: 'TestComponent',
    data() {
        return {
            message: '测试组件加载成功',
            debugInfo: []
        }
    },
    created() {
        this.message = '组件创建于：' + new Date().toLocaleString()
        this.debugInfo.push('组件创建成功')
        this.debugInfo.push('当前时间：' + new Date().toLocaleString())
    },
    methods: {
        testDirect() {
            this.message = '按钮点击于：' + new Date().toLocaleString()
            this.debugInfo.push('按钮被点击')
            this.debugInfo.push('点击时间：' + new Date().toLocaleString())
        }
    }
}
</script>
