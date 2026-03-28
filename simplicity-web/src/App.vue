<script setup>
import { ref, onMounted } from 'vue'
import { useModelStore } from './stores/modelStore'
import ModelSelector from './components/ModelSelector.vue'
import ChatInterface from './components/ChatInterface.vue'
import StatusBar from './components/StatusBar.vue'

const store = useModelStore()
const isLoading = ref(true)

onMounted(async () => {
  await store.discoverProviders()
  isLoading.value = false
})
</script>

<template>
  <div class="app-container">
    <header class="app-header">
      <h1>Simplicity AI</h1>
      <p class="subtitle">Local AI Model Binder</p>
    </header>

    <main class="app-main">
      <div v-if="isLoading" class="loading">
        <div class="spinner"></div>
        <p>Discovering AI providers...</p>
      </div>

      <template v-else>
        <StatusBar :providers="store.providers" />
        <ModelSelector :providers="store.providers" />
        <ChatInterface />
      </template>
    </main>

    <footer class="app-footer">
      <p>Works with Ollama, LM Studio, OpenAI, Anthropic, Google, and more</p>
    </footer>
  </div>
</template>

<style scoped>
.app-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.app-header {
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  color: white;
  padding: 2rem;
  text-align: center;
}

.app-header h1 {
  margin: 0;
  font-size: 2.5rem;
}

.subtitle {
  margin: 0.5rem 0 0;
  opacity: 0.9;
}

.app-main {
  flex: 1;
  padding: 2rem;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}

.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 300px;
}

.spinner {
  width: 50px;
  height: 50px;
  border: 4px solid #e5e7eb;
  border-top-color: #6366f1;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.app-footer {
  background: #1f2937;
  color: #9ca3af;
  padding: 1rem;
  text-align: center;
  font-size: 0.875rem;
}
</style>
