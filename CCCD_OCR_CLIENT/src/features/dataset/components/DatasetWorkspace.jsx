import { buildApiUrl } from '@/features/dataset/services/datasetApi'
import { useDatasetBuilder } from '@/features/dataset/hooks/useDatasetBuilder'
import './DatasetWorkspace.css'

function DatasetWorkspace() {
  const {
    images,
    loadingImages,
    errorMessage,
    successMessage,
    datasetName,
    selectedImageIds,
    selectedCount,
    creating,
    createdDataset,
    canCreate,
    setDatasetName,
    toggleImage,
    selectAll,
    clearSelection,
    handleCreateDataset,
  } = useDatasetBuilder()

  return (
    <div className="dataset-layout">
      <section className="dataset-panel">
        <h2>Tạo dataset huấn luyện</h2>
        <p>Chọn các ảnh đã gán nhãn trong database để gom thành một dataset.</p>

        <div className="dataset-controls">
          <input
            type="text"
            value={datasetName}
            onChange={(event) => setDatasetName(event.target.value)}
            placeholder="Tên dataset"
          />

          <div className="button-row">
            <button type="button" className="secondary-btn" onClick={selectAll}>
              Chọn tất cả
            </button>
            <button type="button" className="secondary-btn" onClick={clearSelection}>
              Bỏ chọn
            </button>
          </div>

          <div className="button-row">
            <button type="button" className="primary-btn" disabled={!canCreate} onClick={handleCreateDataset}>
              {creating ? 'Đang tạo...' : 'Lưu tạo dataset'}
            </button>
          </div>

          <small>Đã chọn: {selectedCount} ảnh</small>
          {createdDataset ? (
            <div className="summary-box">
              <strong>Dataset đã tạo</strong>
              <div>ID: {createdDataset.id}</div>
              <div>Tên: {createdDataset.datasetName}</div>
              <div>Tổng ảnh: {createdDataset.totalImages}</div>
            </div>
          ) : null}
        </div>

        {errorMessage ? <p className="message error">{errorMessage}</p> : null}
        {successMessage ? <p className="message success">{successMessage}</p> : null}
      </section>

      <section className="dataset-panel">
        <h3>Danh sách ảnh trong DB</h3>
        {loadingImages ? <p>Đang tải ảnh...</p> : null}
        {!loadingImages && images.length === 0 ? <p>Chưa có ảnh nào trong database.</p> : null}

        <div className="image-grid">
          {images.map((image) => {
            const selected = selectedImageIds.includes(image.id)
            const hasBoxes = Number(image.boundingBoxCount) > 0

            return (
              <label key={image.id} className={`image-card ${selected ? 'selected' : ''}`}>
                <input
                  type="checkbox"
                  checked={selected}
                  onChange={() => toggleImage(image.id)}
                  style={{ display: 'none' }}
                />
                <img className="image-thumb" src={buildApiUrl(image.previewUrl)} alt={image.originalFilename} />
                <div className="image-card-body">
                  <div className="image-title">{image.originalFilename}</div>
                  <div className="image-meta">
                    {image.imageWidth} × {image.imageHeight}
                  </div>
                  <div className="badge-row">
                    <span className="badge">Box: {image.boundingBoxCount}</span>
                    {!hasBoxes ? <span className="badge warn">Chưa gán nhãn</span> : null}
                  </div>
                </div>
              </label>
            )
          })}
        </div>
      </section>
    </div>
  )
}

export default DatasetWorkspace
