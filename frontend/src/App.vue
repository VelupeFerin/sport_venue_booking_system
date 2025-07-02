<template>
  <div id="app">
    <RouterView />
    <LoadingOverlay 
      :visible="isLoading" 
      :message="loadingMessage" 
    />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { RouterView } from 'vue-router'
import { useUserStore } from './store/user'
import LoadingOverlay from './components/LoadingOverlay.vue'

const userStore = useUserStore()

// 计算加载状态
const isLoading = computed(() => userStore.isLoading)
const loadingMessage = ref('正在验证权限...')
</script>

<style>
/* 全局样式已在main.css中定义 */
</style>