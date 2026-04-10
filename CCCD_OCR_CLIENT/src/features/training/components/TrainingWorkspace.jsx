import { useTraining } from '@/features/training/hooks/useTraining'
import './TrainingWorkspace.css'

function TrainingWorkspace() {
  const {
    datasets,
    loadingDatasets,
    errorMessage,
    successMessage,
    modelType,
    starting,
    canStart,
    form,
    updateField,
    handleStartTraining,
  } = useTraining()

  return (
    <div className="training-layout">
      <section className="training-panel">
        <h2>Huấn luyện model</h2>
        <p>Chọn dataset đã tạo, nhập tham số huấn luyện và chạy train.py từ backend.</p>

        <div className="training-form">
          <label>
            Dataset
            <select value={form.datasetId} onChange={(event) => updateField('datasetId', event.target.value)}>
              <option value="">-- Chọn dataset --</option>
              {datasets.map((dataset) => (
                <option key={dataset.id} value={dataset.id}>
                  {dataset.datasetName} (ảnh: {dataset.totalImages})
                </option>
              ))}
            </select>
          </label>

          <label>
            Loại model
            <select value={modelType} onChange={(event) => updateField('modelType', event.target.value)}>
              <option value="OCR">OCR</option>
              <option value="CARD_DETECTION">CARD_DETECTION</option>
              <option value="ROI_DETECTION">ROI_DETECTION</option>
            </select>
          </label>

          <div className="input-grid">
            <label>
              Epoch
              <input
                type="number"
                min="1"
                value={form.epochs}
                onChange={(event) => updateField('epochs', event.target.value)}
              />
            </label>
            <label>
              Batch size
              <input
                type="number"
                min="1"
                value={form.batchSize}
                onChange={(event) => updateField('batchSize', event.target.value)}
              />
            </label>
            <label>
              Tỷ lệ train
              <input
                type="number"
                step="0.01"
                min="0"
                max="1"
                value={form.trainRatio}
                onChange={(event) => updateField('trainRatio', event.target.value)}
              />
            </label>
            <label>
              Tỷ lệ val
              <input
                type="number"
                step="0.01"
                min="0"
                max="1"
                value={form.valRatio}
                onChange={(event) => updateField('valRatio', event.target.value)}
              />
            </label>
            <label>
              Tỷ lệ test
              <input
                type="number"
                step="0.01"
                min="0"
                max="1"
                value={form.testRatio}
                onChange={(event) => updateField('testRatio', event.target.value)}
              />
            </label>
            <label>
              Kích thước ảnh
              <input
                type="number"
                min="64"
                value={form.imageSize}
                onChange={(event) => updateField('imageSize', event.target.value)}
              />
            </label>
          </div>

          <label>
            Tên
            <input
              type="text"
              value={form.name}
              onChange={(event) => updateField('name', event.target.value)}
            />
          </label>

          <div className="button-row">
            <button type="button" className="primary-btn" disabled={!canStart} onClick={handleStartTraining}>
              {starting ? 'Đang huấn luyện...' : 'Bắt đầu huấn luyện'}
            </button>
          </div>

          <small>{loadingDatasets ? 'Đang tải dataset...' : `Tổng dataset: ${datasets.length}`}</small>
        </div>

        {errorMessage ? <p className="message error">{errorMessage}</p> : null}
        {successMessage ? <p className="message success">{successMessage}</p> : null}
      </section>
    </div>
  )
}

export default TrainingWorkspace
