<script setup>
import { computed } from 'vue'
import { useModelStore } from '../stores/modelStore'

const props = defineProps({
  providers: {
    type: Array,
    default: () => []
  }
})

const store = useModelStore()

const connectedProviders = computed(() => 
  props.providers.filter(p => p.connected)
)

const currentProvider = computed(() =>
  props.providers.find(p => p.id === store.selectedProvider)
)
</script>

<template>
  <div class="model-selector" v-if="connectedProviders.length">
    <h3>Select Model</h3>
    
    <div class="selector-controls">
      <div class="provider-select">
        <label for="provider">Provider:</label>
        <select 
          id="provider" 
          v-model="store.selectedProvider"
          @change="store.selectedModel = currentProvider?.models?.[0]?.name || null"
        >
          <option 
            v-for="provider in connectedProviders" 
            :key="provider.id" 
            :value="provider.id"
          >
            {{ provider.name }} ({{ provider.latency }}ms)
          </option>
        </select>
      </div>

      <div class="model-select" v-if="currentProvider?.models?.length">
        <label for="model">Model:</label>
        <select id="model" v-model="store.selectedModel">
          <option 
            v-for="model in currentProvider.models" 
            :key="model.name" 
            :value="model.name"
          >
            {{ model.name }} {{ model.size ? `(${model.size})` : '' }}
          </option>
        </select>
      </div>
    </div>

    <div class="current-selection">
      <span class="selection-label">Selected:</span>
      <span class="selection-value">
        {{ currentProvider?.name }} / {{ store.selectedModel || 'No model' }}
      </span>
    </div>
  </div>
</template>

<style scoped>
.model-selector {
  background: white;
  border-radius: 0.75rem;
  padding: 1.5rem;
  margin-bottom: 2rem;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.model-selector h3 {
  margin: 0 0 1rem;
  color: #374151;
}

.selector-controls {
  display: flex;
  gap: 1.5rem;
  flex-wrap: wrap;
}

.provider-select,
.model-select {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.provider-select label,
.model-select label {
  font-size: 0.875rem;
  font-weight: 500;
  color: #6b7280;
}

select {
  padding: 0.5rem 1rem;
  border: 1px solid #d1d5db;
  border-radius: 0.5rem;
  font-size: 1rem;
  min-width: 200px;
  background: white;
  cursor: pointer;
}

select:focus {
  outline: none;
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

.current-selection {
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid #e5e7eb;
  font-size: 0.875rem;
}

.selection-label {
  color: #6b7280;
  margin-right: 0.5rem;
}

.selection-value {
  font-weight: 600;
  color: #6366f1;
}
</style>
