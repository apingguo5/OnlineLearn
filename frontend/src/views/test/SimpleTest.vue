<template>
    <div>
        <h1>简单测试</h1>
        <p>点击次数: {{ count }}</p>
        <button @click="count++">点击我</button>
    </div>
</template>

<script>
export default {
    name: 'SimpleTest',
    data() {
        return {
            count: 0
        }
    }
}
</script>