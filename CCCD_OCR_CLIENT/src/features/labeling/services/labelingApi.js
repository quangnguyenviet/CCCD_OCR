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

export async function uploadLabelingImage(imageFile) {
  const formData = new FormData()
  formData.append('image_file', imageFile)

  try {
    const response = await apiClient.post('/api/v1/labeling/cccd/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })

    return response.data
  } catch (error) {
    throw new Error(resolveApiErrorMessage(error))
  }
}

export async function saveLabelingAnnotations(imageId, annotations) {
  try {
    const response = await apiClient.post(`/api/v1/labeling/cccd/${imageId}/annotations`, {
      annotations,
    })

    return response.data
  } catch (error) {
    throw new Error(resolveApiErrorMessage(error))
  }
}

export async function getAnnotatedLabelingImages() {
  try {
    const response = await apiClient.get('/api/v1/labeling/cccd/images/annotated')
    return response.data
  } catch (error) {
    throw new Error(resolveApiErrorMessage(error))
  }
}

export async function createLabelingDataset(datasetName, imageIds) {
  try {
    const response = await apiClient.post('/api/v1/labeling/cccd/datasets', {
      dataset_name: datasetName,
      image_ids: imageIds,
    })
    return response.data
  } catch (error) {
    throw new Error(resolveApiErrorMessage(error))
  }
}

export async function getCreatedDatasets() {
  try {
    const response = await apiClient.get('/api/v1/labeling/cccd/datasets')
    return response.data
  } catch (error) {
    throw new Error(resolveApiErrorMessage(error))
  }
}

export async function exportDatasetAsZip(datasetId) {
  try {
    const response = await apiClient.get(`/api/v1/labeling/cccd/datasets/${datasetId}/export`, {
      responseType: 'blob',
    })

    return response.data
  } catch (error) {
    throw new Error(resolveApiErrorMessage(error))
  }
}

export function getLabeledImagePreviewUrl(imageId) {
  return `${API_BASE_URL}/api/v1/labeling/cccd/images/${imageId}/raw`
}
