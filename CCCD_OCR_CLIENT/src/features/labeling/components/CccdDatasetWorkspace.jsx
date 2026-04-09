import { useEffect, useMemo, useState } from 'react'
import {
  createLabelingDataset,
  exportDatasetAsZip,
  getAnnotatedLabelingImages,
  getCreatedDatasets,
  getLabeledImagePreviewUrl,
} from '@/features/labeling/services/labelingApi'
import './CccdDatasetWorkspace.css'

function CccdDatasetWorkspace() {
  const [annotatedImages, setAnnotatedImages] = useState([])
  const [selectedImageIds, setSelectedImageIds] = useState([])
  const [datasets, setDatasets] = useState([])
  const [datasetName, setDatasetName] = useState('cccd_dataset')

  const [message, setMessage] = useState('')
  const [errorMessage, setErrorMessage] = useState('')
  const [isLoadingSamples, setIsLoadingSamples] = useState(false)
  const [isLoadingDatasets, setIsLoadingDatasets] = useState(false)
  const [isCreatingDataset, setIsCreatingDataset] = useState(false)
  const [isExportingDatasetId, setIsExportingDatasetId] = useState(null)

  useEffect(() => {
    loadAnnotatedImages()
    loadDatasets()
  }, [])

  const canCreateDataset = useMemo(() => {
    return Boolean(datasetName.trim()) && selectedImageIds.length > 0 && !isCreatingDataset
  }, [datasetName, selectedImageIds.length, isCreatingDataset])

  async function loadAnnotatedImages() {
    try {
      setIsLoadingSamples(true)
      setErrorMessage('')
      const response = await getAnnotatedLabelingImages()
      setAnnotatedImages(response?.data ?? [])
    } catch (error) {
      setErrorMessage(error.message)
    } finally {
      setIsLoadingSamples(false)
    }
  }

  async function loadDatasets() {
    try {
      setIsLoadingDatasets(true)
      setErrorMessage('')
      const response = await getCreatedDatasets()
      setDatasets(response?.data ?? [])
    } catch (error) {
      setErrorMessage(error.message)
    } finally {
      setIsLoadingDatasets(false)
    }
  }

  function toggleSample(imageId) {
    setSelectedImageIds((prev) =>
      prev.includes(imageId) ? prev.filter((id) => id !== imageId) : [...prev, imageId],
    )
  }

  function toggleSelectAll() {
    if (selectedImageIds.length === annotatedImages.length) {
      setSelectedImageIds([])
      return
    }

    setSelectedImageIds(annotatedImages.map((item) => item.image_id))
  }

  async function handleCreateDataset() {
    try {
      setIsCreatingDataset(true)
      setErrorMessage('')
      setMessage('')

      const response = await createLabelingDataset(datasetName.trim(), selectedImageIds)
      const created = response?.data

      setMessage(`Tạo dataset thành công. dataset_id=${created?.dataset_id}`)
      await loadDatasets()
    } catch (error) {
      setErrorMessage(error.message)
    } finally {
      setIsCreatingDataset(false)
    }
  }

  async function handleExportDataset(datasetId, fileName) {
    try {
      setIsExportingDatasetId(datasetId)
      setErrorMessage('')
      setMessage('')

      const zipBlob = await exportDatasetAsZip(datasetId)
      const downloadUrl = URL.createObjectURL(zipBlob)
      const anchor = document.createElement('a')
      anchor.href = downloadUrl
      anchor.download = fileName ?? `dataset_${datasetId}.zip`
      document.body.appendChild(anchor)
      anchor.click()
      anchor.remove()
      URL.revokeObjectURL(downloadUrl)

      setMessage('Xuất dataset thành công.')
    } catch (error) {
      setErrorMessage(error.message)
    } finally {
      setIsExportingDatasetId(null)
    }
  }

  return (
    <div className="dataset-layout">
      <section className="dataset-panel">
        <header className="panel-header">
          <h2>Tạo dataset từ mẫu đã gán nhãn</h2>
          <p className="panel-subtitle">Chọn từng mẫu ảnh đã có nhãn, nhập tên dataset, lưu dataset vào database và xuất khi cần.</p>
        </header>

        <div className="dataset-controls">
          <input
            type="text"
            value={datasetName}
            onChange={(event) => setDatasetName(event.target.value)}
            placeholder="Nhập tên dataset"
          />

          <button type="button" className="secondary-btn" onClick={loadAnnotatedImages} disabled={isLoadingSamples}>
            {isLoadingSamples ? 'Đang tải mẫu...' : 'Tải lại mẫu'}
          </button>

          <button type="button" className="secondary-btn" onClick={toggleSelectAll} disabled={annotatedImages.length === 0}>
            {selectedImageIds.length === annotatedImages.length ? 'Bỏ chọn tất cả' : 'Chọn tất cả'}
          </button>

          <button type="button" className="primary-btn" disabled={!canCreateDataset} onClick={handleCreateDataset}>
            {isCreatingDataset ? 'Đang tạo dataset...' : 'Tạo dataset'}
          </button>
        </div>

        {message ? <p className="success-text">{message}</p> : null}
        {errorMessage ? <p className="error-text">{errorMessage}</p> : null}

        <p className="selection-summary">
          Đã chọn <strong>{selectedImageIds.length}</strong> / {annotatedImages.length} mẫu
        </p>

        <div className="sample-grid">
          {annotatedImages.length === 0 ? (
            <p>Chưa có mẫu đã gán nhãn.</p>
          ) : (
            annotatedImages.map((item) => (
              <label key={item.image_id} className="sample-card">
                <input
                  type="checkbox"
                  checked={selectedImageIds.includes(item.image_id)}
                  onChange={() => toggleSample(item.image_id)}
                />
                <img
                  src={getLabeledImagePreviewUrl(item.image_id)}
                  alt={`sample-${item.image_id}`}
                  loading="lazy"
                />
                <div className="sample-meta">
                  <strong>#{item.image_id} {item.original_filename}</strong>
                  <p>{item.annotation_count} nhãn</p>
                  <p>{item.image_width}x{item.image_height}</p>
                </div>
              </label>
            ))
          )}
        </div>
      </section>

      <section className="dataset-panel">
        <h3>Dataset đã tạo</h3>
        {isLoadingDatasets ? <p>Đang tải danh sách dataset...</p> : null}
        <ul className="dataset-list">
          {datasets.map((dataset) => (
            <li key={dataset.dataset_id}>
              <div>
                <strong>{dataset.dataset_name}</strong>
                <p>id={dataset.dataset_id}</p>
                <p>{dataset.total_images} ảnh</p>
              </div>
              <button
                type="button"
                className="secondary-btn"
                disabled={isExportingDatasetId === dataset.dataset_id}
                onClick={() => handleExportDataset(dataset.dataset_id, dataset.zip_file_name)}
              >
                {isExportingDatasetId === dataset.dataset_id ? 'Đang xuất...' : 'Xuất'}
              </button>
            </li>
          ))}
        </ul>
      </section>
    </div>
  )
}

export default CccdDatasetWorkspace
