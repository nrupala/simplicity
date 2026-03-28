<script setup>
import { ref } from 'vue'
import { useModelStore } from '../stores/modelStore'

const store = useModelStore()
const prompt = ref('')
const response = ref('')
const isLoading = ref(false)
const error = ref(null)

async function sendMessage() {
  if (!prompt.value.trim() || !store.isConnected) return
  
  isLoading.value = true
  error.value = null
  response.value = ''
  
  try {
    response.value = await store.generate(prompt.value)
  } catch (e) {
    error.value = e.message
    response.value = ''
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <div class="chat-interface">
    <h3>Chat</h3>
    
    <div class="chat-area">
      <textarea 
        v-model="prompt"
        placeholder="Ask a question..."
        :disabled="isLoading || !store.isConnected"
        rows="4"
      ></textarea>
      
      <button 
        @click="sendMessage"
        :disabled="isLoading || !store.isConnected || !prompt.trim()"
        class="send-button"
      >
        {{ isLoading ? 'Generating...' : 'Send' }}
      </button>
    </div>

    <div v-if="error" class="error-message">
      {{ error }}
    </div>

    <div v-if="response" class="response-area">
      <h4>Response</h4>
      <div class="response-content">
        {{ response }}
      </div>
    </div>

    <div v-if="!store.isConnected" class="not-connected-warning">
      <p>Connect to a local AI provider to start chatting.</p>
      <p class="hint">
        <strong>Ollama:</strong> <code>ollama serve</code> then <code>ollama pull llama3</code><br>
        <strong>LM Studio:</strong> Open app, load model, click "Start Server"
      </p>
    </div>
  </div>
</template>

<style scoped>
.chat-interface {
  background: white;
  border-radius: 0.75rem;
  padding: 1.5rem;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.chat-interface h3 {
  margin: 0 0 1rem;
  color: #374151;
}

.chat-area {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

textarea {
  width: 100%;
  padding: 1rem;
  border: 1px solid #d1d5db;
  border-radius: 0.5rem;
  font-size: 1rem;
  font-family: inherit;
  resize: vertical;
}

textarea:focus {
  outline: none;
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

textarea:disabled {
  background: #f9fafb;
  cursor: not-allowed;
}

.send-button {
  padding: 0.75rem 1.5rem;
  background: #6366f1;
  color: white;
  border: none;
  border-radius: 0.5rem;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.2s;
}

.send-button:hover:not(:disabled) {
  background: #4f46e5;
}

.send-button:disabled {
  background: #9ca3af;
  cursor: not-allowed;
}

.error-message {
  margin-top: 1rem;
  padding: 1rem;
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 0.5rem;
  color: #dc2626;
}

.response-area {
  margin-top: 1.5rem;
}

.response-area h4 {
  margin: 0 0 0.5rem;
  color: #374151;
}

.response-content {
  padding: 1rem;
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 0.5rem;
  white-space: pre-wrap;
  line-height: 1.6;
}

.not-connected-warning {
  margin-top: 1.5rem;
  padding: 1.5rem;
  background: #fef3c7;
  border: 1px solid #fcd34d;
  border-radius: 0.5rem;
}

.not-connected-warning p {
  margin: 0 0 0.5rem;
  color: #92400e;
}

.hint {
  font-size: 0.875rem;
}

.hint code {
  background: rgba(0,0,0,0.1);
  padding: 0.125rem 0.375rem;
  border-radius: 0.25rem;
}
</style>
