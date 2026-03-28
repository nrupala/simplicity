import { defineStore } from 'pinia'
import { ref } from 'vue'

const OLLAMA_BASE = 'http://localhost:11434'
const LM_STUDIO_BASE = 'http://localhost:1234'

export const useModelStore = defineStore('models', () => {
  const providers = ref([])
  const selectedProvider = ref('ollama')
  const selectedModel = ref(null)
  const isConnected = ref(false)
  const latency = ref(null)

  async function discoverProviders() {
    const results = []

    // Check Ollama
    const ollama = await checkProvider(OLLAMA_BASE, 'ollama', 'Ollama')
    if (ollama.connected) {
      ollama.models = await discoverOllamaModels()
    }
    results.push(ollama)

    // Check LM Studio
    const lmstudio = await checkProvider(LM_STUDIO_BASE, 'lmstudio', 'LM Studio')
    if (lmstudio.connected) {
      lmstudio.models = await discoverLMStudioModels()
    }
    results.push(lmstudio)

    providers.value = results
    
    // Auto-select first connected provider
    const connected = results.find(p => p.connected)
    if (connected) {
      selectedProvider.value = connected.id
      selectedModel.value = connected.models?.[0]?.name || null
      isConnected.value = true
      latency.value = connected.latency
    }
  }

  async function checkProvider(baseUrl, id, name) {
    const start = Date.now()
    try {
      const response = await fetch(`${baseUrl}/api/tags`, {
        method: 'GET',
        headers: { 'Content-Type': 'application/json' },
        signal: AbortSignal.timeout(5000)
      })
      const latencyMs = Date.now() - start
      return {
        id,
        name,
        connected: response.ok,
        latency: latencyMs,
        url: baseUrl,
        models: []
      }
    } catch (error) {
      return {
        id,
        name,
        connected: false,
        latency: null,
        url: baseUrl,
        models: [],
        error: error.message
      }
    }
  }

  async function discoverOllamaModels() {
    try {
      const response = await fetch(`${OLLAMA_BASE}/api/tags`)
      if (response.ok) {
        const data = await response.json()
        return data.models.map(m => ({
          name: m.name,
          size: formatSize(m.size),
          modified: m.modified_at
        }))
      }
    } catch (e) {
      console.error('Failed to discover Ollama models:', e)
    }
    return []
  }

  async function discoverLMStudioModels() {
    try {
      const response = await fetch(`${LM_STUDIO_BASE}/v1/models`)
      if (response.ok) {
        const data = await response.json()
        return data.data.map(m => ({
          name: m.id,
          contextLength: m.context_window
        }))
      }
    } catch (e) {
      console.error('Failed to discover LM Studio models:', e)
    }
    return []
  }

  function formatSize(bytes) {
    if (!bytes) return 'Unknown'
    const gb = bytes / (1024 * 1024 * 1024)
    return `${gb.toFixed(1)} GB`
  }

  async function generate(prompt, options = {}) {
    const provider = providers.value.find(p => p.id === selectedProvider.value)
    if (!provider?.connected) {
      throw new Error('Provider not connected')
    }

    const model = selectedModel.value || 'llama3'
    const url = provider.id === 'ollama' 
      ? `${provider.url}/api/generate`
      : `${provider.url}/v1/completions`

    const body = provider.id === 'ollama'
      ? { model, prompt, stream: false, options }
      : { model, prompt, max_tokens: options.maxTokens || 500 }

    const response = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body)
    })

    if (!response.ok) {
      throw new Error(`API error: ${response.status}`)
    }

    const data = await response.json()
    return provider.id === 'ollama' ? data.response : data.choices[0].text
  }

  return {
    providers,
    selectedProvider,
    selectedModel,
    isConnected,
    latency,
    discoverProviders,
    generate
  }
})
