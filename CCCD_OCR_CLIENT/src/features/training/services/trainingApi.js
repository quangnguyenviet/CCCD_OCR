import axios from 'axios'

const API_BASE_URL = import.meta.env.VITE_MODEL_SERVICE_API_BASE_URL ?? 'http://localhost:8082'

const apiClient = axios.create({
  baseURL: API_BASE_URL,
})

function resolveApiErrorMessage(error) {
  if (axios.isAxiosError(error)) {
    return error.response?.data?.message ?? error.message ?? 'Có lỗi xảy ra từ máy chủ.'
  }

  return 'Có lỗi xảy ra từ máy chủ.'
}

export async function startRoiTraining({ file, datasetId, name, epochs, batchSize }) {
  const formData = new FormData()
  if (file) {
    formData.append('file', file)
  }
  if (datasetId !== null && datasetId !== undefined) {
    formData.append('dataset_id', String(datasetId))
  }
  formData.append('name', name)
  formData.append('epochs', String(epochs))
  formData.append('batch_size', String(batchSize))

  try {
    const response = await apiClient.post('/api/v1/training/roi-detection/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })

    return response.data
  } catch (error) {
    throw new Error(resolveApiErrorMessage(error))
  }
}

export async function getTrainingDatasets() {
  try {
    const response = await apiClient.get('/api/v1/labeling/cccd/datasets')
    return response.data
  } catch (error) {
    throw new Error(resolveApiErrorMessage(error))
  }
}

export async function getRoiTrainingStatus(modelId) {
  try {
    const response = await apiClient.get(`/api/v1/training/roi-detection/status/${modelId}`)
    return response.data
  } catch (error) {
    throw new Error(resolveApiErrorMessage(error))
  }
}

export async function stopRoiTraining(modelId) {
  try {
    const response = await apiClient.post(`/api/v1/training/roi-detection/stop/${modelId}`)
    return response.data
  } catch (error) {
    throw new Error(resolveApiErrorMessage(error))
  }
}