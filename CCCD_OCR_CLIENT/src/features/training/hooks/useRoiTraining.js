import { useCallback, useEffect, useMemo, useState } from 'react'
import {
  getRoiTrainingStatus,
  getTrainingDatasets,
  startRoiTraining,
  stopRoiTraining,
} from '@/features/training/services/trainingApi'

const TERMINAL_STATUSES = new Set(['SUCCESS', 'FAILED', 'STOPPED'])

const initialFormState = {
  name: 'roi-detection-cccd',
  epochs: 50,
  batchSize: 8,
}

const DATASET_SOURCES = {
  ZIP: 'zip',
  DB: 'db',
}

function normalizeDataset(dataset) {
  if (!dataset) return null

  return {
    id: dataset.id ?? dataset.dataset_id ?? null,
    name: dataset.name ?? dataset.datasetName ?? dataset.dataset_name ?? '',
    totalImages: dataset.totalImages ?? dataset.total_images ?? 0,
    zipFileName: dataset.zipFileName ?? dataset.zip_file_name ?? '',
  }
}

export function useRoiTraining() {
  const [form, setForm] = useState(initialFormState)
  const [datasetSource, setDatasetSource] = useState(DATASET_SOURCES.ZIP)
  const [datasetFile, setDatasetFile] = useState(null)
  const [availableDatasets, setAvailableDatasets] = useState([])
  const [selectedDatasetId, setSelectedDatasetId] = useState('')
  const [isLoadingDatasets, setIsLoadingDatasets] = useState(false)
  const [datasetError, setDatasetError] = useState('')

  const [jobId, setJobId] = useState(null)
  const [jobStatus, setJobStatus] = useState(null)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [isStopping, setIsStopping] = useState(false)
  const [statusError, setStatusError] = useState('')
  const [logs, setLogs] = useState([])

  const canSubmit = useMemo(() => {
    const hasDatasetSource =
      datasetSource === DATASET_SOURCES.ZIP
        ? datasetFile !== null
        : Number(selectedDatasetId) > 0

    return (
      form.name.trim().length > 0 &&
      Number(form.epochs) > 0 &&
      Number(form.batchSize) > 0 &&
      hasDatasetSource &&
      !isSubmitting
    )
  }, [form.name, form.epochs, form.batchSize, datasetSource, datasetFile, selectedDatasetId, isSubmitting])

  useEffect(() => {
    let active = true

    async function loadDatasets() {
      try {
        setIsLoadingDatasets(true)
        setDatasetError('')
        const response = await getTrainingDatasets()
        if (!active) return

        const datasets = Array.isArray(response?.data)
          ? response.data.map(normalizeDataset).filter((dataset) => dataset?.id)
          : []
        setAvailableDatasets(datasets)
      } catch (error) {
        if (!active) return
        setDatasetError(error.message)
      } finally {
        if (active) {
          setIsLoadingDatasets(false)
        }
      }
    }

    loadDatasets()

    return () => {
      active = false
    }
  }, [])

  function updateField(field, value) {
    setForm((prev) => ({
      ...prev,
      [field]: value,
    }))
  }

  function selectDatasetFile(file) {
    if (!file) return

    const isZip = file.type === 'application/zip' || file.name.toLowerCase().endsWith('.zip')
    if (!isZip) {
      setDatasetError('Vui lòng chọn file ZIP chứa dataset huấn luyện.')
      return
    }

    setDatasetError('')
    setDatasetFile(file)
  }

  function changeDatasetSource(source) {
    setDatasetSource(source)
    setDatasetError('')
  }

  function selectDatasetId(value) {
    setSelectedDatasetId(value)
    setDatasetError('')
  }

  const appendLog = useCallback((message) => {
    if (!message) return

    setLogs((prev) => {
      if (prev[prev.length - 1] === message) return prev
      return [...prev, message].slice(-10)
    })
  }, [])

  async function submitTraining() {
    if (!canSubmit) return

    if (datasetSource === DATASET_SOURCES.ZIP && !datasetFile) {
      setDatasetError('Vui lòng chọn file ZIP chứa dataset huấn luyện.')
      return
    }

    if (datasetSource === DATASET_SOURCES.DB && Number(selectedDatasetId) <= 0) {
      setDatasetError('Vui lòng chọn dataset từ cơ sở dữ liệu.')
      return
    }

    try {
      setIsSubmitting(true)
      setStatusError('')
      setDatasetError('')
      setLogs([])

      const response = await startRoiTraining({
        file: datasetSource === DATASET_SOURCES.ZIP ? datasetFile : null,
        datasetId: datasetSource === DATASET_SOURCES.DB ? Number(selectedDatasetId) : null,
        name: form.name.trim(),
        epochs: Number(form.epochs),
        batchSize: Number(form.batchSize),
      })

      const modelId = response?.data?.model_id ?? response?.data?.modelId ?? null
      setJobId(modelId)
      setJobStatus({
        status: 'PENDING',
        progressPercent: 0,
        latestLog: response?.message ?? 'Đã tạo job huấn luyện.',
      })
      appendLog(response?.message ?? 'Đã tạo job huấn luyện.')
      if (modelId) appendLog(`Job ID: ${modelId}`)
    } catch (error) {
      setStatusError(error.message)
    } finally {
      setIsSubmitting(false)
    }
  }

  useEffect(() => {
    if (!jobId || !jobStatus?.status || TERMINAL_STATUSES.has(jobStatus.status)) return

    let active = true
    let timerId = null

    const poll = async () => {
      try {
        const status = await getRoiTrainingStatus(jobId)
        if (!active) return

        setJobStatus(status)
        appendLog(status?.latestLog)

        if (status?.status && TERMINAL_STATUSES.has(status.status)) {
          return
        }

        timerId = window.setTimeout(poll, 2000)
      } catch (error) {
        if (!active) return

        setStatusError(error.message)
        timerId = window.setTimeout(poll, 3000)
      }
    }

    poll()

    return () => {
      active = false
      if (timerId) window.clearTimeout(timerId)
    }
  }, [jobId, jobStatus?.status, appendLog])

  const canStop = useMemo(() => {
    if (!jobId || !jobStatus?.status) return false
    return !TERMINAL_STATUSES.has(jobStatus.status) && !isStopping
  }, [jobId, jobStatus?.status, isStopping])

  const selectedDataset = useMemo(() => {
    const selectedId = Number(selectedDatasetId)
    if (!selectedId) return null
    return availableDatasets.find((dataset) => Number(dataset.id) === selectedId) ?? null
  }, [availableDatasets, selectedDatasetId])

  async function stopTraining() {
    if (!jobId || !canStop) return

    try {
      setIsStopping(true)
      setStatusError('')

      const status = await stopRoiTraining(jobId)
      setJobStatus(status)
      appendLog(status?.latestLog ?? '[STOPPED] Đã dừng huấn luyện.')
    } catch (error) {
      setStatusError(error.message)
    } finally {
      setIsStopping(false)
    }
  }

  function resetForm() {
    setForm(initialFormState)
    setDatasetSource(DATASET_SOURCES.ZIP)
    setDatasetFile(null)
    setSelectedDatasetId('')
    setDatasetError('')
    setJobId(null)
    setJobStatus(null)
    setIsSubmitting(false)
    setIsStopping(false)
    setStatusError('')
    setLogs([])
  }

  return {
    form,
    datasetSource,
    datasetFile,
    availableDatasets,
    selectedDatasetId,
    selectedDataset,
    isLoadingDatasets,
    datasetError,
    jobId,
    jobStatus,
    isSubmitting,
    isStopping,
    statusError,
    logs,
    canSubmit,
    canStop,
    updateField,
    changeDatasetSource,
    selectDatasetFile,
    selectDatasetId,
    submitTraining,
    stopTraining,
    resetForm,
    DATASET_SOURCES,
  }
}