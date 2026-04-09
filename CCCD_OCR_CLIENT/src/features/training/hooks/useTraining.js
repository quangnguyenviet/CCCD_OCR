import { useEffect, useMemo, useState } from 'react'
import {
  getDatasets,
  getTrainingLogs,
  registerTrainedModel,
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
  baseWeights: 'yolov8n.pt',
  projectName: 'cccd_project',
  runName: 'cccd_yolo',
}

export function useTraining() {
  const [datasets, setDatasets] = useState([])
  const [loadingDatasets, setLoadingDatasets] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')
  const [successMessage, setSuccessMessage] = useState('')
  const [starting, setStarting] = useState(false)
  const [result, setResult] = useState(null)
  const [trainingLogs, setTrainingLogs] = useState('')
  const [logsLoading, setLogsLoading] = useState(false)
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

  useEffect(() => {
    if (!result?.logFilePath) {
      setTrainingLogs('')
      return undefined
    }

    let mounted = true

    async function loadLogs() {
      try {
        setLogsLoading(true)
        const response = await getTrainingLogs(result.logFilePath, 300)
        if (!mounted) return
        setTrainingLogs(response?.content ?? '')
      } catch (error) {
        if (!mounted) return
        setTrainingLogs(`Không đọc được log: ${error.message}`)
      } finally {
        if (mounted) setLogsLoading(false)
      }
    }

    loadLogs()
    const timer = setInterval(loadLogs, 2000)

    return () => {
      mounted = false
      clearInterval(timer)
    }
  }, [result?.logFilePath])

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
      setResult(null)

      const payload = {
        datasetId: Number(form.datasetId),
        epochs: Number(form.epochs),
        batchSize: Number(form.batchSize),
        trainRatio: Number(form.trainRatio),
        valRatio: Number(form.valRatio),
        testRatio: Number(form.testRatio),
        imageSize: Number(form.imageSize),
        baseWeights: form.baseWeights,
        projectName: form.projectName,
        runName: form.runName,
      }

      const response = await startTraining(payload)
      setResult(response)
      setSuccessMessage('Đã khởi chạy huấn luyện.')
    } catch (error) {
      setErrorMessage(error.message)
    } finally {
      setStarting(false)
    }
  }

  async function handleRegisterTrainedModel() {
    if (!result?.bestModelPath) {
      setErrorMessage('Chưa có đường dẫn model huấn luyện hợp lệ.')
      return
    }

    try {
      setStarting(true)
      setErrorMessage('')
      setSuccessMessage('')

      const response = await registerTrainedModel({
        name: form.runName || result.runName,
        type: form.modelType,
        datasetId: Number(form.datasetId),
        modelFilePath: result.bestModelPath,
        url: result.bestModelPath,
        epochs: Number(form.epochs),
        batchSize: Number(form.batchSize),
        trainingLogPath: result.logFilePath,
      })

      setSuccessMessage(`Đã lưu model huấn luyện: ${response.name}`)
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
    result,
    trainingLogs,
    logsLoading,
    modelType: form.modelType,
    starting,
    canStart,
    form,
    updateField,
    handleStartTraining,
    handleRegisterTrainedModel,
  }
}
