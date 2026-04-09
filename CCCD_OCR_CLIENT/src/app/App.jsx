import { Navigate, Route, Routes } from 'react-router-dom'
import HomePage from '@/pages/HomePage'
import NotFoundPage from '@/pages/NotFoundPage'
import OcrPage from '@/pages/OcrPage'
import TrainingPage from '@/pages/TrainingPage'
import LabelingPage from '@/pages/LabelingPage'
import DatasetPage from '@/pages/DatasetPage'

function App() {
  return (
    <Routes>
      <Route path="/" element={<HomePage />} />
      <Route path="/ocr" element={<OcrPage />} />
      <Route path="/training" element={<TrainingPage />} />
      <Route path="/labeling" element={<LabelingPage />} />
      <Route path="/dataset" element={<DatasetPage />} />
      <Route path="/home" element={<Navigate to="/" replace />} />
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  )
}

export default App
