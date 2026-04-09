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

export function buildApiUrl(path) {
  return new URL(path, API_BASE_URL).toString()
}

export async function getImages() {
  try {
    const response = await apiClient.get('/api/images')
    return response.data
  } catch (error) {
    throw new Error(resolveApiErrorMessage(error))
  }
}

export async function createDataset({ datasetName, imageIds }) {
  try {
    const response = await apiClient.post('/api/datasets', {
      datasetName,
      imageIds,
    })
    return response.data
  } catch (error) {
    throw new Error(resolveApiErrorMessage(error))
  }
}
