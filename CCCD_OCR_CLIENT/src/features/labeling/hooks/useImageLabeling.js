import { useMemo, useState } from 'react'
import {
  saveImageBoundingBoxes,
  uploadLabelingImage,
} from '@/features/labeling/services/labelingApi'

export function useImageLabeling() {
  const [imageFile, setImageFile] = useState(null)
  const [imagePreview, setImagePreview] = useState('')
  const [imageId, setImageId] = useState(null)
  const [currentLabel, setCurrentLabel] = useState('')
  const [boxes, setBoxes] = useState([])

  const [uploading, setUploading] = useState(false)
  const [saving, setSaving] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')
  const [successMessage, setSuccessMessage] = useState('')

  const canUpload = useMemo(() => !!imageFile && !uploading, [imageFile, uploading])
  const canSaveBoxes = useMemo(() => !!imageId && boxes.length > 0 && !saving, [imageId, boxes, saving])

  function selectImage(file) {
    if (!file) return

    if (!file.type.startsWith('image/')) {
      setErrorMessage('Vui lòng chọn đúng định dạng ảnh.')
      return
    }

    const objectUrl = URL.createObjectURL(file)

    if (imagePreview) {
      URL.revokeObjectURL(imagePreview)
    }

    setImageFile(file)
    setImagePreview(objectUrl)
    setImageId(null)
    setBoxes([])
    setSuccessMessage('')
    setErrorMessage('')
  }

  function addBox(box) {
    setBoxes((prev) => [...prev, box])
  }

  function removeBox(index) {
    setBoxes((prev) => prev.filter((_, idx) => idx !== index))
  }

  function updateLabel(value) {
    setCurrentLabel(value)
  }

  async function uploadImage() {
    if (!imageFile) return

    try {
      setUploading(true)
      setErrorMessage('')
      setSuccessMessage('')

      const uploaded = await uploadLabelingImage(imageFile)
      setImageId(uploaded.imageId)
      setSuccessMessage('Tải ảnh thành công. Bạn có thể bắt đầu vẽ bounding box.')
    } catch (error) {
      setErrorMessage(error.message)
    } finally {
      setUploading(false)
    }
  }

  async function saveBoxes() {
    if (!imageId) return

    try {
      setSaving(true)
      setErrorMessage('')
      setSuccessMessage('')

      const normalizedBoxes = boxes
        .map((box) => ({
          labelName: box.labelName,
          xMin: Number(box.xMin),
          yMin: Number(box.yMin),
          xMax: Number(box.xMax),
          yMax: Number(box.yMax),
        }))
        .filter((box) => [box.xMin, box.yMin, box.xMax, box.yMax].every(Number.isFinite))

      if (normalizedBoxes.length !== boxes.length) {
        throw new Error('Có bounding box không hợp lệ (tọa độ rỗng/null). Vui lòng vẽ lại box đó.')
      }
      console.log('Saving boxes:', normalizedBoxes)

      await saveImageBoundingBoxes(imageId, normalizedBoxes)
      setSuccessMessage('Đã lưu bounding box thành công.')
    } catch (error) {
      setErrorMessage(error.message)
    } finally {
      setSaving(false)
    }
  }

  return {
    imageFile,
    imagePreview,
    imageId,
    currentLabel,
    boxes,
    uploading,
    saving,
    errorMessage,
    successMessage,
    canUpload,
    canSaveBoxes,
    selectImage,
    addBox,
    removeBox,
    updateLabel,
    uploadImage,
    saveBoxes,
  }
}
