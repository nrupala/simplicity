<script setup>
defineProps({
  providers: {
    type: Array,
    default: () => []
  }
})
</script>

<template>
  <div class="status-bar">
    <h3>Provider Status</h3>
    <div class="providers-grid">
      <div 
        v-for="provider in providers" 
        :key="provider.id"
        :class="['provider-card', { connected: provider.connected }]"
      >
        <div class="provider-header">
          <span class="status-dot" :class="{ connected: provider.connected }"></span>
          <span class="provider-name">{{ provider.name }}</span>
        </div>
        <div class="provider-details">
          <div v-if="provider.connected">
            <span class="status-text">Connected</span>
            <span class="latency">{{ provider.latency }}ms</span>
          </div>
          <div v-else class="not-connected">
            <span class="status-text">Not Connected</span>
            <span class="hint" v-if="provider.id === 'ollama'">
              Start Ollama: <code>ollama serve</code>
            </span>
            <span class="hint" v-else-if="provider.id === 'lmstudio'">
              Open LM Studio and start server
            </span>
          </div>
        </div>
        <div v-if="provider.models?.length" class="model-count">
          {{ provider.models.length }} models available
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.status-bar {
  margin-bottom: 2rem;
}

.status-bar h3 {
  margin-bottom: 1rem;
  color: #374151;
}

.providers-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1rem;
}

.provider-card {
  background: white;
  border: 2px solid #e5e7eb;
  border-radius: 0.75rem;
  padding: 1.25rem;
  transition: all 0.2s;
}

.provider-card.connected {
  border-color: #10b981;
  background: #f0fdf4;
}

.provider-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
}

.status-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #ef4444;
}

.status-dot.connected {
  background: #10b981;
}

.provider-name {
  font-weight: 600;
  color: #111827;
}

.provider-details {
  font-size: 0.875rem;
  color: #6b7280;
}

.latency {
  margin-left: 0.5rem;
  color: #10b981;
  font-weight: 500;
}

.not-connected {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.hint {
  font-size: 0.75rem;
  color: #9ca3af;
}

.hint code {
  background: #f3f4f6;
  padding: 0.125rem 0.375rem;
  border-radius: 0.25rem;
  font-size: 0.75rem;
}

.model-count {
  margin-top: 0.75rem;
  padding-top: 0.75rem;
  border-top: 1px solid #e5e7eb;
  font-size: 0.75rem;
  color: #6b7280;
}
</style>
