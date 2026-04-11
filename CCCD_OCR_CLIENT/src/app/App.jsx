import { Navigate, Route, Routes } from 'react-router-dom'
import DatasetPage from '@/pages/DatasetPage'
import HomePage from '@/pages/HomePage'
import LabelingPage from '@/pages/LabelingPage'
import NotFoundPage from '@/pages/NotFoundPage'
import OcrPage from '@/pages/OcrPage'
import TrainingMonitorPage from '@/pages/TrainingMonitorPage'
import TrainingPage from '@/pages/TrainingPage'
import TrainingResultPage from '@/pages/TrainingResultPage'
import StatisticsPage from '@/pages/StatisticsPage'

function App() {
  return (
    <Routes>
      <Route path="/" element={<HomePage />} />
      <Route path="/ocr" element={<OcrPage />} />
      <Route path="/labeling" element={<LabelingPage />} />
      <Route path="/dataset" element={<DatasetPage />} />
      <Route path="/training" element={<TrainingPage />} />
      <Route path="/training/monitor" element={<TrainingMonitorPage />} />
      <Route path="/training/results" element={<TrainingResultPage />} />
      <Route path="/statistics" element={<StatisticsPage />} />
      <Route path="/home" element={<Navigate to="/" replace />} />
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  )
}

export default App
