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

export async function uploadLabelingImage(imageFile) {
  const formData = new FormData()
  formData.append('image_file', imageFile)

  try {
    const response = await apiClient.post('/api/labeling/images/upload', formData)
    return response.data
  } catch (error) {
    throw new Error(resolveApiErrorMessage(error))
  }
}

export async function saveImageBoundingBoxes(imageId, boxes) {
  try {
    const response = await apiClient.post(`/api/labeling/images/${imageId}/boxes`, {
      boxes,
    })
    return response.data
  } catch (error) {
    throw new Error(resolveApiErrorMessage(error))
  }
}
