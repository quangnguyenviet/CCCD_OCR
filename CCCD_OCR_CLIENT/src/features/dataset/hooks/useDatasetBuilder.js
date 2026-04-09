import { useEffect, useMemo, useState } from 'react'
import { createDataset, getImages } from '@/features/dataset/services/datasetApi'

export function useDatasetBuilder() {
  const [images, setImages] = useState([])
  const [loadingImages, setLoadingImages] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')
  const [successMessage, setSuccessMessage] = useState('')
  const [datasetName, setDatasetName] = useState('')
  const [selectedImageIds, setSelectedImageIds] = useState([])
  const [creating, setCreating] = useState(false)
  const [createdDataset, setCreatedDataset] = useState(null)

  useEffect(() => {
    let mounted = true

    async function loadImages() {
      try {
        setLoadingImages(true)
        setErrorMessage('')
        const response = await getImages()
        if (!mounted) return
        setImages(response ?? [])
      } catch (error) {
        if (!mounted) return
        setErrorMessage(error.message)
      } finally {
        if (mounted) setLoadingImages(false)
      }
    }

    loadImages()

    return () => {
      mounted = false
    }
  }, [])

  const selectedCount = selectedImageIds.length

  const canCreate = useMemo(() => {
    return datasetName.trim().length > 0 && selectedImageIds.length > 0 && !creating
  }, [datasetName, selectedImageIds, creating])

  function toggleImage(imageId) {
    setSelectedImageIds((prev) => {
      if (prev.includes(imageId)) {
        return prev.filter((id) => id !== imageId)
      }
      return [...prev, imageId]
    })
  }

  function selectAll() {
    setSelectedImageIds(images.map((image) => image.id))
  }

  function clearSelection() {
    setSelectedImageIds([])
  }

  async function handleCreateDataset() {
    try {
      setCreating(true)
      setErrorMessage('')
      setSuccessMessage('')

      const response = await createDataset({
        datasetName,
        imageIds: selectedImageIds,
      })

      setCreatedDataset(response)
      setSuccessMessage(`Đã tạo dataset ${response.datasetName} thành công.`)
    } catch (error) {
      setErrorMessage(error.message)
    } finally {
      setCreating(false)
    }
  }

  return {
    images,
    loadingImages,
    errorMessage,
    successMessage,
    datasetName,
    selectedImageIds,
    selectedCount,
    creating,
    createdDataset,
    canCreate,
    setDatasetName,
    toggleImage,
    selectAll,
    clearSelection,
    handleCreateDataset,
  }
}
