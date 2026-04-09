import { useRef } from 'react'
import { useRoiTraining } from '@/features/training/hooks/useRoiTraining'
import './TrainingWorkspace.css'

const STATUS_LABELS = {
  PENDING: 'Chờ xử lý',
  EXTRACTING: 'Giải nén dữ liệu',
  TRAINING: 'Đang huấn luyện',
  SUCCESS: 'Thành công',
  FAILED: 'Thất bại',
  STOPPED: 'Đã dừng',
}

function formatProgress(progress) {
  if (progress === null || progress === undefined) return '-'
  return `${progress}%`
}

function TrainingWorkspace() {
  const inputRef = useRef(null)
  const {
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
  } = useRoiTraining()

  const currentStatus = jobStatus?.status ?? 'PENDING'
  const currentProgress = jobStatus?.progressPercent ?? 0

  function handleFileChange(event) {
    const file = event.target.files?.[0]
    if (!file) return
    selectDatasetFile(file)
  }

  function handleDrop(event) {
    event.preventDefault()
    const file = event.dataTransfer.files?.[0]
    if (!file) return
    selectDatasetFile(file)
  }

  function handleDragOver(event) {
    event.preventDefault()
  }

  return (
    <div className="training-layout">
      <section className="training-panel">
        <header className="panel-header">
          <p className="eyebrow">Huấn luyện ROI detection</p>
          <h2>Huấn luyện mô hình nhận dạng vùng thông tin CCCD</h2>
          <p className="panel-subtitle">
            Bạn có thể upload file ZIP hoặc chọn dataset đã lưu từ hệ thống labeling.
          </p>
        </header>

        <div className="form-grid">
          <div className="form-group">
            <label htmlFor="training-name">Tên mô hình</label>
            <input
              id="training-name"
              type="text"
              value={form.name}
              onChange={(event) => updateField('name', event.target.value)}
              placeholder="roi-detection-cccd-v1"
            />
          </div>

          <div className="split-row">
            <div className="form-group">
              <label htmlFor="training-epochs">Epochs</label>
              <input
                id="training-epochs"
                type="number"
                min="1"
                value={form.epochs}
                onChange={(event) => updateField('epochs', event.target.value)}
              />
            </div>

            <div className="form-group">
              <label htmlFor="training-batch">Batch size</label>
              <input
                id="training-batch"
                type="number"
                min="1"
                value={form.batchSize}
                onChange={(event) => updateField('batchSize', event.target.value)}
              />
            </div>
          </div>

          <div className="form-group">
            <p className="upload-label">Nguồn dataset</p>
            <div className="source-row" role="radiogroup" aria-label="Nguồn dataset">
              <label className="source-option">
                <input
                  type="radio"
                  name="dataset-source"
                  value={DATASET_SOURCES.ZIP}
                  checked={datasetSource === DATASET_SOURCES.ZIP}
                  onChange={(event) => changeDatasetSource(event.target.value)}
                />
                <span>Upload file ZIP</span>
              </label>
              <label className="source-option">
                <input
                  type="radio"
                  name="dataset-source"
                  value={DATASET_SOURCES.DB}
                  checked={datasetSource === DATASET_SOURCES.DB}
                  onChange={(event) => changeDatasetSource(event.target.value)}
                />
                <span>Chọn từ CSDL</span>
              </label>
            </div>
          </div>

          {datasetSource === DATASET_SOURCES.ZIP ? (
            <div className="form-group">
              <p className="upload-label">Dataset ZIP</p>
              <div
                className="dropzone"
                role="button"
                tabIndex={0}
                onClick={() => inputRef.current?.click()}
                onDrop={handleDrop}
                onDragOver={handleDragOver}
              >
                <input
                  ref={inputRef}
                  type="file"
                  accept=".zip,application/zip"
                  onChange={handleFileChange}
                  className="hidden-input"
                />
                <p>Kéo thả file ZIP hoặc click để chọn dataset</p>
                <small>Trong ZIP phải có data.yaml và ảnh/nhãn theo đúng cấu trúc train/valid.</small>
                {datasetFile ? <strong>{datasetFile.name}</strong> : null}
              </div>
            </div>
          ) : (
            <div className="form-group">
              <label htmlFor="training-dataset-id">Dataset trong CSDL</label>
              <select
                id="training-dataset-id"
                value={selectedDatasetId}
                onChange={(event) => selectDatasetId(event.target.value)}
                disabled={isLoadingDatasets}
              >
                <option value="">{isLoadingDatasets ? 'Đang tải danh sách dataset...' : '-- Chọn dataset --'}</option>
                {availableDatasets.map((dataset) => (
                  <option key={dataset.id} value={dataset.id}>
                    {dataset.name} (#{dataset.id}) - {dataset.totalImages} ảnh
                  </option>
                ))}
              </select>
            </div>
          )}

          {datasetError ? <p className="error-text">{datasetError}</p> : null}
          {statusError ? <p className="error-text">{statusError}</p> : null}

          <div className="action-row">
            <button type="button" className="primary-btn" disabled={!canSubmit} onClick={submitTraining}>
              {isSubmitting ? 'Đang tạo job...' : 'Bắt đầu huấn luyện'}
            </button>
            <button type="button" className="secondary-btn" onClick={resetForm}>
              Reset form
            </button>
            <button
              type="button"
              className="danger-btn"
              disabled={!canStop}
              onClick={stopTraining}
            >
              {isStopping ? 'Đang dừng...' : 'Dừng huấn luyện'}
            </button>
          </div>
        </div>
      </section>

      <section className="training-panel status-panel">
        <header className="panel-header">
          <p className="eyebrow">Trạng thái huấn luyện</p>
          <h2>Theo dõi tiến trình model</h2>
        </header>

        {!jobId ? (
          <div className="state-box empty">
            <p>Chưa có job nào được khởi tạo. Hãy upload ZIP và bấm bắt đầu huấn luyện.</p>
          </div>
        ) : (
          <div className="status-card">
            <div className="status-row">
              <span className={`status-pill status-${currentStatus.toLowerCase()}`}>
                {STATUS_LABELS[currentStatus] ?? currentStatus}
              </span>
              <span className="job-id">Job ID: {jobId}</span>
            </div>

            <div className="progress-block">
              <div className="progress-header">
                <span>Tiến trình</span>
                <strong>{formatProgress(currentProgress)}</strong>
              </div>
              <div className="progress-bar" aria-hidden="true">
                <div className="progress-fill" style={{ width: `${currentProgress ?? 0}%` }} />
              </div>
            </div>

            <div className="detail-grid">
              <div>
                <label>Mô hình</label>
                <p>{form.name}</p>
              </div>
              <div>
                <label>Epochs</label>
                <p>{form.epochs}</p>
              </div>
              <div>
                <label>Batch size</label>
                <p>{form.batchSize}</p>
              </div>
              <div>
                <label>Nguồn dataset</label>
                <p>
                  {datasetSource === DATASET_SOURCES.ZIP
                    ? 'Upload ZIP'
                    : selectedDataset
                      ? `${selectedDataset.name} (#${selectedDataset.id})`
                      : `Dataset ID: ${selectedDatasetId || '-'}`}
                </p>
              </div>
              <div>
                <label>Log mới nhất</label>
                <p>{jobStatus?.latestLog ?? '-'}</p>
              </div>
            </div>

            <div className="log-box">
              <h3>Lịch sử log</h3>
              {logs.length === 0 ? <p>Chưa có log.</p> : null}
              <ul>
                {logs.map((log, index) => (
                  <li key={`${index}-${log}`}>{log}</li>
                ))}
              </ul>
            </div>
          </div>
        )}
      </section>
    </div>
  )
}

export default TrainingWorkspace