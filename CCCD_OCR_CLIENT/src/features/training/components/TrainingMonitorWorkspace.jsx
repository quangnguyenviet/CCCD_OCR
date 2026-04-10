import { useEffect, useMemo, useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import {
  getTrainingStatus,
  getModelMetrics,
  getTrainingLogs,
  registerTrainedModel,
  stopTraining,
} from '@/features/training/services/trainingApi'
import './TrainingWorkspace.css'

function TrainingMonitorWorkspace() {
  const location = useLocation()
  const navigate = useNavigate()

  const trainingResult = location.state?.trainingResult
  const trainingInput = location.state?.trainingInput

  const [trainingLogs, setTrainingLogs] = useState('')
  const [logsLoading, setLogsLoading] = useState(false)
  const [metrics, setMetrics] = useState(null)
  const [metricsLoading, setMetricsLoading] = useState(false)
  const [savingModel, setSavingModel] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')
  const [successMessage, setSuccessMessage] = useState('')
  const [stoppingTraining, setStoppingTraining] = useState(false)
  const [isTrainingRunning, setIsTrainingRunning] = useState(Boolean(trainingResult))

  const canSaveModel = useMemo(() => {
    return Boolean(trainingResult?.bestModelPath) && !savingModel && !isTrainingRunning
  }, [trainingResult?.bestModelPath, savingModel, isTrainingRunning])

  const trainingInfoText = useMemo(() => {
    if (!trainingResult) return ''
    return [
      `Status: ${trainingResult.status ?? '-'}`,
      `Dataset: ${trainingResult.datasetName ?? '-'}`,
      `Tiến trình: ${isTrainingRunning ? 'Đang huấn luyện' : 'Đã hoàn thành'}`,
      `Log file: ${trainingResult.logFilePath ?? '-'}`,
      `Best model: ${trainingResult.bestModelPath ?? '-'}`,
      `Tên: ${trainingResult.name ?? '-'}`,
      `Epoch: ${trainingResult.epochs ?? '-'}`,
      `Batch size: ${trainingResult.batchSize ?? '-'}`,
    ].join('\n')
  }, [trainingResult, isTrainingRunning])

  const metricsText = useMemo(() => {
    if (!metrics) return ''

    let parsedMetrics = metrics.finalMetrics
    if (typeof metrics.finalMetrics === 'string') {
      try {
        parsedMetrics = JSON.stringify(JSON.parse(metrics.finalMetrics), null, 2)
      } catch {
        parsedMetrics = metrics.finalMetrics
      }
    }

    return [
      `Model: ${metrics.name ?? '-'}`,
      `Status: ${metrics.status ?? '-'}`,
      `Start time: ${metrics.trainingStartTime ? new Date(metrics.trainingStartTime).toLocaleString('vi-VN') : '-'}`,
      `End time: ${metrics.trainingEndTime ? new Date(metrics.trainingEndTime).toLocaleString('vi-VN') : '-'}`,
      `Duration: ${metrics.trainingDurationSeconds !== null && metrics.trainingDurationSeconds !== undefined ? Math.round(metrics.trainingDurationSeconds / 60) + ' minutes' : '-'}`,
      `Final Loss: ${metrics.finalLoss !== null && metrics.finalLoss !== undefined ? metrics.finalLoss.toFixed(4) : '-'}`,
      '',
      'Metrics:',
      `${parsedMetrics ?? '-'}`,
    ].join('\n')
  }, [metrics])

  useEffect(() => {
    if (!trainingResult?.logFilePath) {
      setTrainingLogs('')
      return undefined
    }

    let mounted = true

    async function loadLogs() {
      try {
        setLogsLoading(true)
        const [response, status] = await Promise.all([
          getTrainingLogs(trainingResult.logFilePath, 300),
          getTrainingStatus(),
        ])
        if (!mounted) return
        setTrainingLogs(response?.content ?? '')
        setIsTrainingRunning(Boolean(status?.running))
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
  }, [trainingResult?.logFilePath])

  async function handleRegisterTrainedModel() {
    if (!trainingResult?.bestModelPath) {
      setErrorMessage('Chưa có đường dẫn model huấn luyện hợp lệ.')
      return
    }

    if (isTrainingRunning) {
      setErrorMessage('Mô hình đang huấn luyện. Chỉ được lưu sau khi huấn luyện hoàn tất.')
      return
    }

    try {
      setSavingModel(true)
      setErrorMessage('')
      setSuccessMessage('')

      const response = await registerTrainedModel({
        name: trainingInput?.name || trainingResult?.name,
        type: trainingInput?.modelType || 'OCR',
        datasetId: Number(trainingInput?.datasetId || trainingResult?.datasetId),
        modelFilePath: trainingResult.bestModelPath,
        url: trainingResult.bestModelPath,
        epochs: Number(trainingInput?.epochs || trainingResult?.epochs),
        batchSize: Number(trainingInput?.batchSize || trainingResult?.batchSize),
        trainingLogPath: trainingResult.logFilePath,
      })

      setSuccessMessage(`Đã lưu model huấn luyện: ${response.name}`)

      if (response.id) {
        try {
          setMetricsLoading(true)
          const metricsData = await getModelMetrics(response.id)
          setMetrics(metricsData)
        } catch (error) {
          console.warn('Không thể lấy metrics:', error.message)
        } finally {
          setMetricsLoading(false)
        }
      }
    } catch (error) {
      setErrorMessage(error.message)
    } finally {
      setSavingModel(false)
    }
  }

  async function handleStopTraining() {
    try {
      setStoppingTraining(true)
      setErrorMessage('')
      setSuccessMessage('')

      await stopTraining()

      setSuccessMessage('Đã dừng huấn luyện.')
      setIsTrainingRunning(false)
    } catch (error) {
      setErrorMessage('Không thể dừng huấn luyện: ' + (error.message || 'lỗi không xác định'))
    } finally {
      setStoppingTraining(false)
    }
  }

  if (!trainingResult) {
    return (
      <div className="training-layout">
        <section className="training-panel">
          <h2>Theo dõi huấn luyện</h2>
          <p>Không có phiên huấn luyện để theo dõi.</p>
          <div className="button-row">
            <button type="button" className="primary-btn" onClick={() => navigate('/training')}>
              Quay lại trang huấn luyện
            </button>
          </div>
        </section>
      </div>
    )
  }

  return (
    <div className="monitor-layout">
      <section className="training-panel monitor-side-panel">
        <h2>Theo dõi huấn luyện</h2>
        <p>Thông tin phiên huấn luyện.</p>

        <textarea className="monitor-textarea" readOnly value={trainingInfoText} />

        <div className="button-row" style={{ marginTop: '1rem' }}>
          <button
            type="button"
            className="secondary-btn"
            disabled={!canSaveModel}
            onClick={handleRegisterTrainedModel}
          >
            {savingModel ? 'Đang lưu...' : 'Lưu mô hình'}
          </button>
          <button
            type="button"
            className="danger-btn"
            disabled={stoppingTraining || !isTrainingRunning}
            onClick={handleStopTraining}
          >
            {stoppingTraining ? 'Đang dừng...' : 'Dừng huấn luyện'}
          </button>
          <Link
            className="button-link"
            to="/training/results"
            state={{ trainingResult, trainingInput }}
            style={{ marginLeft: '0.5rem' }}
          >
            Xem kết quả chi tiết
          </Link>
          <Link className="button-link" to="/training" style={{ marginLeft: '0.5rem' }}>
            Tạo phiên khác
          </Link>
        </div>

        {errorMessage ? <p className="message error">{errorMessage}</p> : null}
        {successMessage ? <p className="message success">{successMessage}</p> : null}
      </section>

      <section className="training-panel monitor-log-panel">
        <div className="result-box">
          <h4>Live training log {logsLoading ? '(đang cập nhật...)' : ''}</h4>
          <pre className="code-box monitor-log-box">{trainingLogs || 'Chưa có log.'}</pre>
        </div>
      </section>

      <section className="training-panel monitor-side-panel">
        <h4>Training Metrics {metricsLoading ? '(đang cập nhật...)' : ''}</h4>
        {metrics ? <textarea className="monitor-textarea" readOnly value={metricsText} /> : <p>Chưa có metrics.</p>}
      </section>
    </div>
  )
}

export default TrainingMonitorWorkspace
