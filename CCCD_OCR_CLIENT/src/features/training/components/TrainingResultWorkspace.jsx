import { useEffect, useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { getTrainingResultImages, resolveResultImageUrl } from '@/features/training/services/trainingApi'
import './TrainingWorkspace.css'

function TrainingResultWorkspace() {
  const location = useLocation()
  const navigate = useNavigate()
  const trainingResult = location.state?.trainingResult

  const [loading, setLoading] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')
  const [resultDir, setResultDir] = useState('')
  const [images, setImages] = useState([])

  useEffect(() => {
    if (!trainingResult?.modelOutputDir || !trainingResult?.name) return

    let mounted = true

    async function load() {
      try {
        setLoading(true)
        setErrorMessage('')
        const response = await getTrainingResultImages(trainingResult.modelOutputDir, trainingResult.name)
        if (!mounted) return
        setResultDir(response?.runDir ?? '')
        setImages(response?.images ?? [])
      } catch (error) {
        if (!mounted) return
        setErrorMessage(error.message)
      } finally {
        if (mounted) setLoading(false)
      }
    }

    load()
    return () => {
      mounted = false
    }
  }, [trainingResult?.modelOutputDir, trainingResult?.name])

  if (!trainingResult) {
    return (
      <section className="training-panel">
        <h2>Kết quả chi tiết huấn luyện</h2>
        <p>Không có thông tin phiên huấn luyện.</p>
        <button type="button" className="primary-btn" onClick={() => navigate('/training')}>
          Quay lại huấn luyện
        </button>
      </section>
    )
  }

  return (
    <div className="training-layout" style={{ gridTemplateColumns: '1fr' }}>
      <section className="training-panel">
        <h2>Kết quả chi tiết huấn luyện</h2>
        <div className="result-box">
          <div><strong>Tên run:</strong> {trainingResult.name}</div>
          <div><strong>Thư mục output:</strong> {trainingResult.modelOutputDir}</div>
          <div><strong>Thư mục kết quả:</strong> {resultDir || '-'}</div>
          <div><strong>Số ảnh:</strong> {images.length}</div>
        </div>

        <div className="button-row" style={{ marginTop: '1rem' }}>
          <Link className="button-link" to="/training/monitor" state={{ trainingResult, trainingInput: location.state?.trainingInput }}>
            Quay lại theo dõi
          </Link>
          <Link className="button-link" to="/training" style={{ marginLeft: '0.5rem' }}>
            Phiên huấn luyện mới
          </Link>
        </div>

        {loading ? <p>Đang tải ảnh kết quả...</p> : null}
        {errorMessage ? <p className="message error">{errorMessage}</p> : null}

        {!loading && !errorMessage && images.length === 0 ? (
          <p>Chưa có ảnh kết quả trong thư mục run (có thể training chưa ghi xong).</p>
        ) : null}

        <div className="result-image-grid" style={{ marginTop: '1rem' }}>
          {images.map((item) => (
            <article key={item.path} className="result-image-card">
              <div className="result-image-title">{item.name}</div>
              <a href={resolveResultImageUrl(item.url)} target="_blank" rel="noreferrer">
                <img className="result-image" src={resolveResultImageUrl(item.url)} alt={item.name} loading="lazy" />
              </a>
            </article>
          ))}
        </div>
      </section>
    </div>
  )
}

export default TrainingResultWorkspace
