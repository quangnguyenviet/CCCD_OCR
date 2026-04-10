import { useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  getDatasets,
  startTraining,
} from '@/features/training/services/trainingApi'

const defaultForm = {
  datasetId: '',
  modelType: 'OCR',
  epochs: 10,
  batchSize: 16,
  trainRatio: 0.7,
  valRatio: 0.2,
  testRatio: 0.1,
  imageSize: 640,
  name: 'cccd_yolo',
}

export function useTraining() {
  const navigate = useNavigate()
  const [datasets, setDatasets] = useState([])
  const [loadingDatasets, setLoadingDatasets] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')
  const [successMessage, setSuccessMessage] = useState('')
  const [starting, setStarting] = useState(false)
  const [form, setForm] = useState(defaultForm)

  useEffect(() => {
    let mounted = true

    async function loadDatasets() {
      try {
        setLoadingDatasets(true)
        const response = await getDatasets()
        if (!mounted) return
        setDatasets(response ?? [])
      } catch (error) {
        if (!mounted) return
        setErrorMessage(error.message)
      } finally {
        if (mounted) setLoadingDatasets(false)
      }
    }

    loadDatasets()

    return () => {
      mounted = false
    }
  }, [])

  const canStart = useMemo(() => {
    const datasetSelected = String(form.datasetId).trim().length > 0
    const ratioSum = Number(form.trainRatio) + Number(form.valRatio) + Number(form.testRatio)

    return datasetSelected && !starting && Math.abs(ratioSum - 1) <= 0.0001
  }, [form, starting])

  function updateField(field, value) {
    setForm((prev) => ({
      ...prev,
      [field]: value,
    }))
  }

  async function handleStartTraining() {
    try {
      setStarting(true)
      setErrorMessage('')
      setSuccessMessage('')

      const payload = {
        datasetId: Number(form.datasetId),
        epochs: Number(form.epochs),
        batchSize: Number(form.batchSize),
        trainRatio: Number(form.trainRatio),
        valRatio: Number(form.valRatio),
        testRatio: Number(form.testRatio),
        imageSize: Number(form.imageSize),
        name: form.name,
      }

      const response = await startTraining(payload)
      setSuccessMessage('Đã khởi chạy huấn luyện.')

      navigate('/training/monitor', {
        state: {
          trainingResult: response,
          trainingInput: {
            ...payload,
            modelType: form.modelType,
          },
        },
      })
    } catch (error) {
      setErrorMessage(error.message)
    } finally {
      setStarting(false)
    }
  }

  return {
    datasets,
    loadingDatasets,
    errorMessage,
    successMessage,
    modelType: form.modelType,
    starting,
    canStart,
    form,
    updateField,
    handleStartTraining,
  }
}
