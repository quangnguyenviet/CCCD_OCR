import { useMemo, useRef, useState } from 'react'
import { useImageLabeling } from '@/features/labeling/hooks/useImageLabeling'
import './LabelingWorkspace.css'

const LABEL_OPTIONS = [
  { value: 'cccd_number', text: 'Số CCCD' },
  { value: 'full_name', text: 'Họ và tên' },
  { value: 'date_of_birth', text: 'Ngày sinh' },
  { value: 'gender', text: 'Giới tính' },
  { value: 'nationality', text: 'Quốc tịch' },
  { value: 'place_of_origin', text: 'Quê quán' },
  { value: 'place_of_residence', text: 'Nơi thường trú' },
  { value: '__custom__', text: 'Nhãn tùy chỉnh' },
]

function clamp(value, min, max) {
  return Math.max(min, Math.min(value, max))
}

function LabelingWorkspace() {
  const imageRef = useRef(null)
  const [draftRect, setDraftRect] = useState(null)
  const [isDrawing, setIsDrawing] = useState(false)
  const [selectedLabelOption, setSelectedLabelOption] = useState('cccd_number')

  const {
    imageFile,
    imagePreview,
    imageId,
    currentLabel,
    boxes,
    uploading,
    saving,
    errorMessage,
    successMessage,
    canUpload,
    canSaveBoxes,
    selectImage,
    addBox,
    removeBox,
    updateLabel,
    uploadImage,
    saveBoxes,
  } = useImageLabeling()

  const activeLabel = selectedLabelOption === '__custom__' ? currentLabel.trim() : selectedLabelOption

  function hasValidImageMetrics() {
    const imageEl = imageRef.current
    if (!imageEl) return false

    const rect = imageEl.getBoundingClientRect()
    return (
      imageEl.naturalWidth > 0 &&
      imageEl.naturalHeight > 0 &&
      rect.width > 0 &&
      rect.height > 0
    )
  }

  function getCanvasPoint(event) {
    const imageEl = imageRef.current
    if (!imageEl) return null

    const rect = imageEl.getBoundingClientRect()
    if (!rect.width || !rect.height) return null

    const x = clamp(event.clientX - rect.left, 0, rect.width)
    const y = clamp(event.clientY - rect.top, 0, rect.height)

    return { x, y, width: rect.width, height: rect.height }
  }

  function convertToImageCoords(rect) {
    const imageEl = imageRef.current
    if (!imageEl || !rect) return null

    const displayRect = imageEl.getBoundingClientRect()
    if (!displayRect.width || !displayRect.height || !imageEl.naturalWidth || !imageEl.naturalHeight) {
      return null
    }

    const scaleX = imageEl.naturalWidth / displayRect.width
    const scaleY = imageEl.naturalHeight / displayRect.height

    const xMin = Number((Math.min(rect.x1, rect.x2) * scaleX).toFixed(2))
    const yMin = Number((Math.min(rect.y1, rect.y2) * scaleY).toFixed(2))
    const xMax = Number((Math.max(rect.x1, rect.x2) * scaleX).toFixed(2))
    const yMax = Number((Math.max(rect.y1, rect.y2) * scaleY).toFixed(2))

    if (![xMin, yMin, xMax, yMax].every(Number.isFinite)) {
      return null
    }

    return {
      labelName: activeLabel,
      xMin,
      yMin,
      xMax,
      yMax,
    }
  }

  function toCanvasRect(box) {
    const imageEl = imageRef.current
    if (!imageEl) return null
    if (!imageEl.naturalWidth || !imageEl.naturalHeight) return null

    const displayWidth = imageEl.getBoundingClientRect().width
    const displayHeight = imageEl.getBoundingClientRect().height

    if (!displayWidth || !displayHeight) return null

    const scaleX = displayWidth / imageEl.naturalWidth
    const scaleY = displayHeight / imageEl.naturalHeight

    const left = box.xMin * scaleX
    const top = box.yMin * scaleY
    const width = (box.xMax - box.xMin) * scaleX
    const height = (box.yMax - box.yMin) * scaleY

    return { left, top, width, height }
  }

  function handleFileChange(event) {
    const file = event.target.files?.[0]
    if (!file) return
    selectImage(file)
  }

  function handleLabelOptionChange(event) {
    const selected = event.target.value
    setSelectedLabelOption(selected)

    if (selected !== '__custom__') {
      updateLabel(selected)
    }
  }

  function handleMouseDown(event) {
    if (!imagePreview) return
    if (!activeLabel) return
    if (!hasValidImageMetrics()) return

    const point = getCanvasPoint(event)
    if (!point) return

    setIsDrawing(true)
    setDraftRect({
      x1: point.x,
      y1: point.y,
      x2: point.x,
      y2: point.y,
    })
  }

  function handleMouseMove(event) {
    if (!isDrawing || !draftRect) return

    const point = getCanvasPoint(event)
    if (!point) return

    setDraftRect((prev) => {
      if (!prev) return prev
      return {
        ...prev,
        x2: point.x,
        y2: point.y,
      }
    })
  }

  function handleMouseUp() {
    if (!isDrawing || !draftRect) return

    setIsDrawing(false)

    const width = Math.abs(draftRect.x2 - draftRect.x1)
    const height = Math.abs(draftRect.y2 - draftRect.y1)

    if (width < 8 || height < 8) {
      setDraftRect(null)
      return
    }

    const box = convertToImageCoords(draftRect)
    if (box) addBox(box)

    setDraftRect(null)
  }

  const draftStyle = useMemo(() => {
    if (!draftRect) return null

    return {
      left: Math.min(draftRect.x1, draftRect.x2),
      top: Math.min(draftRect.y1, draftRect.y2),
      width: Math.abs(draftRect.x2 - draftRect.x1),
      height: Math.abs(draftRect.y2 - draftRect.y1),
    }
  }, [draftRect])

  return (
    <div className="labeling-layout">
      <section className="labeling-panel">
        <h2>Upload ảnh CCCD & vẽ bounding box</h2>

        <div className="labeling-controls">
          <input type="file" accept="image/*" onChange={handleFileChange} />
          <select value={selectedLabelOption} onChange={handleLabelOptionChange}>
            {LABEL_OPTIONS.map((option) => (
              <option key={option.value} value={option.value}>
                {option.text}
              </option>
            ))}
          </select>

          {selectedLabelOption === '__custom__' ? (
            <input
              type="text"
              placeholder="Nhập nhãn tùy chỉnh"
              value={currentLabel}
              onChange={(event) => updateLabel(event.target.value)}
            />
          ) : null}

          <div className="button-row">
            <button type="button" className="primary-btn" disabled={!canUpload} onClick={uploadImage}>
              {uploading ? 'Đang upload...' : 'Upload ảnh'}
            </button>
            <button type="button" className="secondary-btn" disabled={!canSaveBoxes} onClick={saveBoxes}>
              {saving ? 'Đang lưu...' : 'Lưu bounding box'}
            </button>
          </div>

          {imageId ? <small>Image ID: {imageId}</small> : <small>Bạn có thể vẽ trước, upload trước khi lưu box.</small>}
          {imageFile ? <small>File: {imageFile.name}</small> : null}
          <small>Nhãn đang vẽ: {activeLabel || '(chưa chọn)'}</small>
        </div>

        {errorMessage ? <p className="message error">{errorMessage}</p> : null}
        {successMessage ? <p className="message success">{successMessage}</p> : null}

        <div className="canvas-wrapper">
          {!imagePreview ? (
            <p>Chọn ảnh trước để bắt đầu.</p>
          ) : (
            <div className="image-stage">
              <img ref={imageRef} src={imagePreview} alt="CCCD preview" draggable={false} />
              <div
                className="overlay"
                onMouseDown={handleMouseDown}
                onMouseMove={handleMouseMove}
                onMouseUp={handleMouseUp}
                onMouseLeave={handleMouseUp}
              >
                {boxes.map((box, index) => {
                  const rect = toCanvasRect(box)
                  if (!rect) return null

                  return (
                    <div
                      key={`${box.labelName}-${index}`}
                      className="bbox"
                      style={{
                        left: `${rect.left}px`,
                        top: `${rect.top}px`,
                        width: `${rect.width}px`,
                        height: `${rect.height}px`,
                      }}
                    >
                      <span>{box.labelName}</span>
                    </div>
                  )
                })}

                {draftStyle ? (
                  <div
                    className="bbox"
                    style={{
                      left: `${draftStyle.left}px`,
                      top: `${draftStyle.top}px`,
                      width: `${draftStyle.width}px`,
                      height: `${draftStyle.height}px`,
                    }}
                  />
                ) : null}
              </div>
            </div>
          )}
        </div>
      </section>

      <section className="labeling-panel">
        <h3>Danh sách box ({boxes.length})</h3>
        {boxes.length === 0 ? <p>Chưa có box nào.</p> : null}

        <div className="box-list">
          {boxes.map((box, index) => (
            <div key={`${box.labelName}-${index}`} className="box-item">
              <div>
                <strong>{box.labelName}</strong>
                <div>
                  ({box.xMin}, {box.yMin}) → ({box.xMax}, {box.yMax})
                </div>
              </div>
              <button type="button" className="danger-btn" onClick={() => removeBox(index)}>
                Xóa
              </button>
            </div>
          ))}
        </div>
      </section>
    </div>
  )
}

export default LabelingWorkspace
