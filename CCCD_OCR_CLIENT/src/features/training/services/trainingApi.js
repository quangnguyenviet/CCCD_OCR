import axios from 'axios'

const API_BASE_URL = import.meta.env.VITE_MODEL_SERVICE_API_BASE_URL ?? 'http://localhost:8080'

const apiClient = axios.create({
  baseURL: API_BASE_URL,
})

function resolveApiErrorMessage(error) {
  if (axios.isAxiosError(error)) {
    return error.response?.data?.message ?? error.message ?? 'Có lỗi xảy ra từ máy chủ.'
  }

  return 'Có lỗi xảy ra từ máy chủ.'
}

export async function getDatasets() {
  try {
    const response = await apiClient.get('/api/datasets')
    return response.data
  } catch (error) {
    throw new Error(resolveApiErrorMessage(error))
  }
}

export async function startTraining(payload) {
  try {
    const response = await apiClient.post('/api/training/start', payload)
    return response.data
  } catch (error) {
    throw new Error(resolveApiErrorMessage(error))
  }
}

export async function getTrainingLogs(logFilePath, tailLines = 200) {
  try {
    const response = await apiClient.get('/api/training/logs', {
      params: {
        logFilePath,
        tailLines,
      },
    })

    return response.data
  } catch (error) {
    throw new Error(resolveApiErrorMessage(error))
  }
}

export async function registerTrainedModel(payload) {
  try {
    const response = await apiClient.post('/api/models/register-trained', payload)
    return response.data
  } catch (error) {
    throw new Error(resolveApiErrorMessage(error))
  }
}

export async function getModelMetrics(modelId) {
  try {
    const response = await apiClient.get(`/api/models/${modelId}/metrics`)
    return response.data
  } catch (error) {
    throw new Error(resolveApiErrorMessage(error))
  }
}

