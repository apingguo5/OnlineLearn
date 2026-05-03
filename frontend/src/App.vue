<!--
 * @Description: 
 * @Author: Rabbiter
 * @Date: 2024-04-26 23:22:24
-->
<template>
  <div id="app">

    <router-view class="con">

    </router-view>

  </div>
</template>

<script>

export default {
  name: 'app',
}
</script>

<style>
#app {
  margin: 0;
  padding: 0
}
.con {
  height: 100%;
  overflow: hidden;
}

.c {
  height: 100%;
}

.el-submenu__title {
    font-size: 20px;
}
</style>
