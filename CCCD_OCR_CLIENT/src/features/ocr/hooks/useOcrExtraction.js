import { useEffect, useMemo, useState } from 'react'
import { extractFromImage, getActiveModels } from '@/features/ocr/services/ocrApi'

const initialSelectedModels = {
  cardModelId: '',
  roiModelId: '',
  ocrModelId: '',
}

export function useOcrExtraction() {
  const [models, setModels] = useState({
    card_detection_models: [],
    roi_detection_models: [],
    ocr_models: [],
  })
  const [modelsLoading, setModelsLoading] = useState(false)
  const [modelsError, setModelsError] = useState('')

  const [selectedModels, setSelectedModels] = useState(initialSelectedModels)
  const [imageFile, setImageFile] = useState(null)
  const [imagePreview, setImagePreview] = useState('')

  const [status, setStatus] = useState('idle')
  const [result, setResult] = useState(null)
  const [errorMessage, setErrorMessage] = useState('')

  useEffect(() => {
    let mounted = true

    async function loadModels() {
      try {
        setModelsLoading(true)
        setModelsError('')

        const response = await getActiveModels();
        console.log('Active models response:', response)

        if (!mounted) return

        setModels(response ?? {})
      } catch (error) {
        if (!mounted) return
        setModelsError(error.message)
      } finally {
        if (mounted) setModelsLoading(false)
      }
    }

    loadModels()

    return () => {
      mounted = false
    }
  }, [])

  useEffect(() => {
    if (!imageFile) {
      setImagePreview('')
      return
    }

    const objectUrl = URL.createObjectURL(imageFile)
    setImagePreview(objectUrl)

    return () => {
      URL.revokeObjectURL(objectUrl)
    }
  }, [imageFile])

  const canSubmit = useMemo(() => {
    return (
      selectedModels.cardModelId &&
      selectedModels.roiModelId &&
      selectedModels.ocrModelId &&
      imageFile &&
      status !== 'loading'
    )
  }, [selectedModels, imageFile, status])

  function updateModelSelection(field, value) {
    setSelectedModels((prev) => ({
      ...prev,
      [field]: value,
    }))
  }

  function selectImage(file) {
    if (!file) return

    if (!file.type.startsWith('image/')) {
      setErrorMessage('Vui lòng chọn đúng định dạng ảnh.')
      return
    }

    setErrorMessage('')
    setImageFile(file)
  }

  async function startExtraction() {
    if (!canSubmit) return

    try {
      setStatus('loading')
      setErrorMessage('')

      const response = await extractFromImage({
        ...selectedModels,
        imageFile,
      })

      setResult(response)
      setStatus('success')
    } catch (error) {
      setStatus('error')
      setErrorMessage(error.message)
    }
  }

  return {
    models,
    modelsLoading,
    modelsError,
    selectedModels,
    imageFile,
    imagePreview,
    status,
    result,
    errorMessage,
    canSubmit,
    updateModelSelection,
    selectImage,
    startExtraction,
  }
}
