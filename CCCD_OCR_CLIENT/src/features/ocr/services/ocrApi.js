import axios from 'axios'

const API_BASE_URL = import.meta.env.VITE_OCR_API_BASE_URL ?? 'http://localhost:8080'
const apiClient = axios.create({
  baseURL: API_BASE_URL,
})

function resolveApiErrorMessage(error) {
  if (axios.isAxiosError(error)) {
    return error.response?.data?.message ?? error.message ?? 'Có lỗi xảy ra từ máy chủ.'
  }

  return 'Có lỗi xảy ra từ máy chủ.'
}

export async function getActiveModels() {
  try {
    const response = await apiClient.get('/api/models/active')
    return response.data
  } catch (error) {
    throw new Error(resolveApiErrorMessage(error))
  }
}

export async function extractFromImage({ cardModelUrl, roiModelUrl, ocrModelUrl, imageFile }) {
  const formData = new FormData()
  formData.append('image_file', imageFile)

  try {
    const response = await apiClient.post('/api/v1/extract/image', formData, {
      params: {
        card_model_url: cardModelUrl,
        roi_model_url: roiModelUrl,
        ocr_model_url: ocrModelUrl,
      },
    })

    return response.data
  } catch (error) {
    throw new Error(resolveApiErrorMessage(error))
  }
}
