import { Navigate, Route, Routes } from 'react-router-dom'
import HomePage from '@/pages/HomePage'
import LabelingPage from '@/pages/LabelingPage'
import NotFoundPage from '@/pages/NotFoundPage'
import OcrPage from '@/pages/OcrPage'

function App() {
  return (
    <Routes>
      <Route path="/" element={<HomePage />} />
      <Route path="/ocr" element={<OcrPage />} />
      <Route path="/labeling" element={<LabelingPage />} />
      <Route path="/home" element={<Navigate to="/" replace />} />
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  )
}

export default App
