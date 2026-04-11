import SectionTitle from '@/components/common/SectionTitle'
import { Link } from 'react-router-dom'
import { APP_NAME } from '@/shared/constants/app.constants'

function HomePage() {
  return (
    <main className="container">
      <section className="card">
        <SectionTitle title={APP_NAME} subtitle="Client side for CCCD OCR workflow" />

        <p>
          Project structure has been organized following scalable React best practices.
        </p>

        <div className="actions">
          <Link className="button-link" to="/ocr">
            Open OCR page
          </Link>
          <Link className="button-link" to="/labeling" style={{ marginLeft: '0.5rem' }}>
            Open Labeling page
          </Link>
          <Link className="button-link" to="/dataset" style={{ marginLeft: '0.5rem' }}>
            Open Dataset page
          </Link>
          <Link className="button-link" to="/training" style={{ marginLeft: '0.5rem' }}>
            Open Training page
          </Link>
          <Link className="button-link" to="/statistics" style={{ marginLeft: '0.5rem', background: '#3b82f6', color: 'white' }}>
            Thống kê Mô hình
          </Link>
        </div>
      </section>
    </main>
  )
}

export default HomePage
