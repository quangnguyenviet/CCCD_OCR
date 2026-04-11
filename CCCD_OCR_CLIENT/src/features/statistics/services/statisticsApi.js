import axios from 'axios'

const API_BASE_URL = import.meta.env.VITE_MODEL_SERVICE_API_BASE_URL ?? 'http://localhost:8080'

const apiClient = axios.create({
  baseURL: API_BASE_URL,
})

export async function getModelStatistics() {
  try {
    const response = await apiClient.get('/api/statistics/models')
    return response.data
  } catch (error) {
    if (axios.isAxiosError(error)) {
        throw new Error(error.response?.data?.message ?? error.message ?? 'Có lỗi xảy ra từ máy chủ.')
    }
    throw new Error('Có lỗi xảy ra từ máy chủ.')
  }
}
