import { useRef, useState } from 'react'
import { useOcrExtraction } from '@/features/ocr/hooks/useOcrExtraction'
import './OcrWorkspace.css'

const FIELD_LABELS = {
  cccd_number: 'Số CCCD',
  full_name: 'Họ và tên',
  date_of_birth: 'Ngày sinh',
  gender: 'Giới tính',
  nationality: 'Quốc tịch',
  place_of_origin: 'Quê quán',
  place_of_residence: 'Nơi thường trú',
}

function OcrWorkspace() {
  const inputRef = useRef(null)
  const [copied, setCopied] = useState(false)
  const {
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
  } = useOcrExtraction()

  const extractedData = result?.data ?? {}

  function handleFileChange(event) {
    const file = event.target.files?.[0]
    if (!file) return
    selectImage(file)
  }

  function handleDrop(event) {
    event.preventDefault()
    const file = event.dataTransfer.files?.[0]
    if (!file) return
    selectImage(file)
  }

  function handleDragOver(event) {
    event.preventDefault()
  }

  async function handleCopyResult() {
    if (!result) return
    await navigator.clipboard.writeText(JSON.stringify(result, null, 2))
    setCopied(true)
    setTimeout(() => setCopied(false), 1500)
  }

  function handleDownloadJson() {
    if (!result) return

    const blob = new Blob([JSON.stringify(result, null, 2)], {
      type: 'application/json;charset=utf-8',
    })

    const url = URL.createObjectURL(blob)
    const anchor = document.createElement('a')
    anchor.href = url
    anchor.download = 'cccd_ocr_result.json'
    anchor.click()
    URL.revokeObjectURL(url)
  }

  return (
    <div className="ocr-layout">
      <section className="ocr-panel">
        <header className="panel-header">
          <h2>Cấu hình & Đầu vào</h2>
          <p className="panel-subtitle">Chọn mô hình và tải ảnh CCCD để bắt đầu trích xuất.</p>
        </header>

        <div className="form-group">
          <label htmlFor="card-model">Mô hình nhận dạng vùng CCCD</label>
          <select
            id="card-model"
            value={selectedModels.cardModelId}
            onChange={(event) => updateModelSelection('cardModelId', event.target.value)}
            disabled={modelsLoading}
          >
            <option value="">-- Chọn mô hình --</option>
            {models.card_detection_models?.map((model) => (
              <option key={model.id} value={model.id}>
                {model.name}
              </option>
            ))}
          </select>
        </div>

        <div className="form-group">
          <label htmlFor="roi-model">Mô hình nhận dạng vùng thông tin</label>
          <select
            id="roi-model"
            value={selectedModels.roiModelId}
            onChange={(event) => updateModelSelection('roiModelId', event.target.value)}
            disabled={modelsLoading}
          >
            <option value="">-- Chọn mô hình --</option>
            {models.roi_detection_models?.map((model) => (
              <option key={model.id} value={model.id}>
                {model.name}
              </option>
            ))}
          </select>
        </div>

        <div className="form-group">
          <label htmlFor="ocr-model">Mô hình OCR</label>
          <select
            id="ocr-model"
            value={selectedModels.ocrModelId}
            onChange={(event) => updateModelSelection('ocrModelId', event.target.value)}
            disabled={modelsLoading}
          >
            <option value="">-- Chọn mô hình --</option>
            {models.ocr_models?.map((model) => (
              <option key={model.id} value={model.id}>
                {model.name}
              </option>
            ))}
          </select>
        </div>

        <div className="form-group">
          <p className="upload-label">Tải ảnh CCCD</p>
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
              accept="image/*"
              onChange={handleFileChange}
              className="hidden-input"
            />
            <p>Kéo & thả ảnh vào đây hoặc click để chọn ảnh</p>
            {imageFile ? <small>{imageFile.name}</small> : null}
            {imagePreview ? <img className="preview-image" src={imagePreview} alt="Preview CCCD" /> : null}
          </div>
        </div>

        {modelsLoading ? <p className="info-text">Đang tải danh sách mô hình...</p> : null}
        {modelsError ? <p className="error-text">{modelsError}</p> : null}
        {errorMessage ? <p className="error-text">{errorMessage}</p> : null}

        <button type="button" className="primary-btn" disabled={!canSubmit} onClick={startExtraction}>
          Bắt đầu trích xuất
        </button>
      </section>

      <section className="ocr-panel">
        <header className="panel-header">
          <h2>Kết quả đầu ra</h2>
        </header>

        {status === 'idle' ? (
          <div className="state-box empty">
            <p>Vui lòng cấu hình mô hình và tải ảnh lên để xem kết quả.</p>
          </div>
        ) : null}

        {status === 'loading' ? (
          <div className="state-box loading">
            <div className="spinner" aria-hidden="true"></div>
            <p>Hệ thống đang phân tích ảnh qua 3 lớp mô hình...</p>
          </div>
        ) : null}

        {status === 'error' ? (
          <div className="state-box error">
            <p>Không thể trích xuất dữ liệu. Vui lòng kiểm tra lại cấu hình và thử lại.</p>
          </div>
        ) : null}

        {status === 'success' ? (
          <div className="result-box">
            <div className="result-grid">
              {Object.entries(FIELD_LABELS).map(([key, label]) => (
                <div key={key} className="result-item">
                  <label>{label}</label>
                  <input value={extractedData[key] ?? ''} readOnly />
                </div>
              ))}
            </div>

            <div className="meta-row">
              <small>Thời gian xử lý: {result.processing_time_ms ?? '-'} ms</small>
            </div>

            <div className="action-row">
              <button type="button" className="secondary-btn" onClick={handleCopyResult}>
                {copied ? 'Đã sao chép' : 'Sao chép kết quả'}
              </button>
              <button type="button" className="secondary-btn" onClick={handleDownloadJson}>
                Tải JSON
              </button>
            </div>
          </div>
        ) : null}
      </section>
    </div>
  )
}

export default OcrWorkspace
