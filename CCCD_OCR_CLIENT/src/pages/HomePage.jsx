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
          <Link className="button-link" to="/training" style={{ marginLeft: '0.75rem' }}>
            Open training page
          </Link>
          <Link className="button-link" to="/labeling" style={{ marginLeft: '0.75rem' }}>
            Open labeling page
          </Link>
          <Link className="button-link" to="/dataset" style={{ marginLeft: '0.75rem' }}>
            Open dataset page
          </Link>
        </div>
      </section>
    </main>
  )
}

export default HomePage
