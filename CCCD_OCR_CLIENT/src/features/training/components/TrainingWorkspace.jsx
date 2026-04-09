import { useTraining } from '@/features/training/hooks/useTraining'
import './TrainingWorkspace.css'

function TrainingWorkspace() {
  const {
    datasets,
    loadingDatasets,
    errorMessage,
    successMessage,
    result,
    trainingLogs,
    logsLoading,
    modelType,
    starting,
    canStart,
    form,
    updateField,
    handleStartTraining,
    handleRegisterTrainedModel,
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

          <div className="input-grid">
            <label>
              Base weights
              <input
                type="text"
                value={form.baseWeights}
                onChange={(event) => updateField('baseWeights', event.target.value)}
              />
            </label>
            <label>
              Project name
              <input
                type="text"
                value={form.projectName}
                onChange={(event) => updateField('projectName', event.target.value)}
              />
            </label>
          </div>

          <label>
            Run name
            <input
              type="text"
              value={form.runName}
              onChange={(event) => updateField('runName', event.target.value)}
            />
          </label>

          <div className="button-row">
            <button type="button" className="primary-btn" disabled={!canStart} onClick={handleStartTraining}>
              {starting ? 'Đang huấn luyện...' : 'Bắt đầu huấn luyện'}
            </button>
            <button
              type="button"
              className="secondary-btn"
              disabled={!result?.bestModelPath || starting}
              onClick={handleRegisterTrainedModel}
            >
              Dùng mô hình huấn luyện
            </button>
          </div>

          <small>{loadingDatasets ? 'Đang tải dataset...' : `Tổng dataset: ${datasets.length}`}</small>
        </div>

        {errorMessage ? <p className="message error">{errorMessage}</p> : null}
        {successMessage ? <p className="message success">{successMessage}</p> : null}
      </section>

      <section className="training-panel">
        <h3>Kết quả khởi chạy</h3>
        {!result ? <p>Chưa có phiên huấn luyện nào.</p> : null}

        {result ? (
          <div className="result-box">
            <div><strong>Status:</strong> {result.status}</div>
            <div><strong>Dataset:</strong> {result.datasetName}</div>
            <div><strong>Export dir:</strong> {result.exportDir}</div>
            <div><strong>Log file:</strong> {result.logFilePath}</div>
            <div><strong>Data yaml:</strong> {result.dataYamlPath}</div>
            <div><strong>Output dir:</strong> {result.modelOutputDir}</div>
            <div><strong>Best model:</strong> {result.bestModelPath}</div>
            <div><strong>Run name:</strong> {result.runName}</div>
            <div><strong>Epoch:</strong> {result.epochs}</div>
            <div><strong>Batch size:</strong> {result.batchSize}</div>
            <div><strong>Split:</strong> train {result.trainRatio} / val {result.valRatio} / test {result.testRatio}</div>
          </div>
        ) : null}

        <div className="result-box" style={{ marginTop: '1rem' }}>
          <h4>Live training log {logsLoading ? '(đang cập nhật...)' : ''}</h4>
          <pre className="code-box">{trainingLogs || 'Chưa có log.'}</pre>
        </div>

        <div className="dataset-list" style={{ marginTop: '1rem' }}>
          {datasets.map((dataset) => (
            <div key={dataset.id} className="dataset-item">
              <strong>{dataset.datasetName}</strong>
              <div>ID: {dataset.id}</div>
              <div>Total images: {dataset.totalImages}</div>
              <div>Created at: {dataset.createdAt ?? '-'}</div>
            </div>
          ))}
        </div>
      </section>
    </div>
  )
}

export default TrainingWorkspace
