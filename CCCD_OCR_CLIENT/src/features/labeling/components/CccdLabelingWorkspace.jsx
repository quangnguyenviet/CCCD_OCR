import { useEffect, useMemo, useRef, useState } from 'react'
import { saveLabelingAnnotations, uploadLabelingImage } from '@/features/labeling/services/labelingApi'
import './CccdLabelingWorkspace.css'

const LABEL_OPTIONS = [
  'cccd_number',
  'full_name',
  'date_of_birth',
  'gender',
  'nationality',
  'place_of_origin',
  'place_of_residence',
]

function normalizeRect(start, end) {
  const x = Math.min(start.x, end.x)
  const y = Math.min(start.y, end.y)
  const width = Math.abs(end.x - start.x)
  const height = Math.abs(end.y - start.y)
  return { x, y, width, height }
}

function CccdLabelingWorkspace() {
  const [selectedLabel, setSelectedLabel] = useState(LABEL_OPTIONS[0])
  const [imageFile, setImageFile] = useState(null)
  const [imagePreviewUrl, setImagePreviewUrl] = useState('')
  const [imageMeta, setImageMeta] = useState(null)
  const [boxes, setBoxes] = useState([])
  const [draftBox, setDraftBox] = useState(null)
  const [drawStart, setDrawStart] = useState(null)

  const [message, setMessage] = useState('')
  const [errorMessage, setErrorMessage] = useState('')
  const [isUploading, setIsUploading] = useState(false)
  const [isSaving, setIsSaving] = useState(false)

  const imageRef = useRef(null)

  useEffect(() => {
    return () => {
      if (imagePreviewUrl) {
        URL.revokeObjectURL(imagePreviewUrl)
      }
    }
  }, [imagePreviewUrl])

  const canSave = useMemo(() => {
    return Boolean(imageMeta?.image_id) && boxes.length > 0 && !isSaving
  }, [imageMeta?.image_id, boxes.length, isSaving])

  function resetAnnotator() {
    setBoxes([])
    setDraftBox(null)
    setDrawStart(null)
  }

  async function handleUpload(event) {
    const file = event.target.files?.[0]
    if (!file) return

    if (!file.type.startsWith('image/')) {
      setErrorMessage('Vui lòng chọn file ảnh hợp lệ.')
      return
    }

    try {
      setIsUploading(true)
      setErrorMessage('')
      setMessage('')

      const response = await uploadLabelingImage(file)
      const meta = response?.data

      setImageFile(file)
      setImageMeta(meta)
      setImagePreviewUrl((prev) => {
        if (prev) URL.revokeObjectURL(prev)
        return URL.createObjectURL(file)
      })
      resetAnnotator()
      setMessage(`Upload thành công. image_id=${meta?.image_id}`)
    } catch (error) {
      setErrorMessage(error.message)
    } finally {
      setIsUploading(false)
    }
  }

  function toLocalPoint(clientX, clientY) {
    const element = imageRef.current
    if (!element) return null

    const rect = element.getBoundingClientRect()
    const x = Math.max(0, Math.min(clientX - rect.left, rect.width))
    const y = Math.max(0, Math.min(clientY - rect.top, rect.height))
    return { x, y }
  }

  function handleMouseDown(event) {
    if (!imagePreviewUrl) return
    const point = toLocalPoint(event.clientX, event.clientY)
    if (!point) return
    setDrawStart(point)
    setDraftBox({ ...point, width: 0, height: 0, label: selectedLabel })
  }

  function handleMouseMove(event) {
    if (!drawStart) return
    const point = toLocalPoint(event.clientX, event.clientY)
    if (!point) return
    const rect = normalizeRect(drawStart, point)
    setDraftBox({ ...rect, label: selectedLabel })
  }

  function handleMouseUp(event) {
    if (!drawStart || !draftBox) return
    const point = toLocalPoint(event.clientX, event.clientY)
    if (!point) {
      setDrawStart(null)
      setDraftBox(null)
      return
    }

    const rect = normalizeRect(drawStart, point)
    if (rect.width >= 8 && rect.height >= 8) {
      setBoxes((prev) => [...prev, { id: crypto.randomUUID(), ...rect, label: selectedLabel }])
    }

    setDrawStart(null)
    setDraftBox(null)
  }

  function removeBox(boxId) {
    setBoxes((prev) => prev.filter((box) => box.id !== boxId))
  }

  async function handleSave() {
    if (!imageMeta?.image_id || !imageRef.current || boxes.length === 0) return

    try {
      setIsSaving(true)
      setErrorMessage('')
      setMessage('')

      const displayedWidth = imageRef.current.clientWidth
      const displayedHeight = imageRef.current.clientHeight

      const scaleX = (imageMeta.image_width ?? displayedWidth) / displayedWidth
      const scaleY = (imageMeta.image_height ?? displayedHeight) / displayedHeight

      const payload = boxes.map((box) => ({
        label_name: box.label,
        x_min: Math.round(box.x * scaleX),
        y_min: Math.round(box.y * scaleY),
        x_max: Math.round((box.x + box.width) * scaleX),
        y_max: Math.round((box.y + box.height) * scaleY),
      }))

      const response = await saveLabelingAnnotations(imageMeta.image_id, payload)
      setMessage(response?.message ?? 'Lưu nhãn thành công.')
    } catch (error) {
      setErrorMessage(error.message)
    } finally {
      setIsSaving(false)
    }
  }

  return (
    <div className="labeling-layout">
      <section className="labeling-panel">
        <header className="panel-header">
          <h2>Gán nhãn CCCD</h2>
          <p className="panel-subtitle">Upload ảnh, vẽ bounding box theo từng trường thông tin và lưu xuống cơ sở dữ liệu.</p>
        </header>

        <div className="toolbar">
          <label className="upload-button" htmlFor="cccd-upload-input">
            {isUploading ? 'Đang upload...' : 'Upload ảnh CCCD'}
          </label>
          <input id="cccd-upload-input" type="file" accept="image/*" onChange={handleUpload} hidden />

          <label htmlFor="label-select">Nhãn đang vẽ</label>
          <select id="label-select" value={selectedLabel} onChange={(event) => setSelectedLabel(event.target.value)}>
            {LABEL_OPTIONS.map((label) => (
              <option key={label} value={label}>
                {label}
              </option>
            ))}
          </select>

          <button type="button" className="secondary-btn" onClick={resetAnnotator}>
            Xóa tất cả box
          </button>

          <button type="button" className="primary-btn" disabled={!canSave} onClick={handleSave}>
            {isSaving ? 'Đang lưu...' : 'Lưu nhãn'}
          </button>
        </div>

        {message ? <p className="success-text">{message}</p> : null}
        {errorMessage ? <p className="error-text">{errorMessage}</p> : null}

        <div className="annotator-wrapper">
          {imagePreviewUrl ? (
            <div
              className="annotator-canvas"
              onMouseDown={handleMouseDown}
              onMouseMove={handleMouseMove}
              onMouseUp={handleMouseUp}
            >
              <img ref={imageRef} src={imagePreviewUrl} alt={imageFile?.name ?? 'CCCD preview'} />

              {boxes.map((box) => (
                <div
                  key={box.id}
                  className="bbox"
                  style={{ left: box.x, top: box.y, width: box.width, height: box.height }}
                >
                  <span>{box.label}</span>
                </div>
              ))}

              {draftBox ? (
                <div
                  className="bbox draft"
                  style={{ left: draftBox.x, top: draftBox.y, width: draftBox.width, height: draftBox.height }}
                >
                  <span>{draftBox.label}</span>
                </div>
              ) : null}
            </div>
          ) : (
            <div className="empty-state">Chưa có ảnh. Vui lòng upload ảnh CCCD để bắt đầu gán nhãn.</div>
          )}
        </div>
      </section>

      <section className="labeling-panel box-list-panel">
        <h3>Danh sách bounding box ({boxes.length})</h3>
        {boxes.length === 0 ? <p>Chưa có bounding box.</p> : null}
        <ul className="box-list">
          {boxes.map((box, index) => (
            <li key={box.id}>
              <div>
                <strong>#{index + 1}</strong> {box.label}
                <p>
                  x={Math.round(box.x)} y={Math.round(box.y)} w={Math.round(box.width)} h={Math.round(box.height)}
                </p>
              </div>
              <button type="button" className="danger-btn" onClick={() => removeBox(box.id)}>
                Xóa
              </button>
            </li>
          ))}
        </ul>
      </section>
    </div>
  )
}

export default CccdLabelingWorkspace
