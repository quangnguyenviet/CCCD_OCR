import { useEffect, useState } from 'react'
import { getModelStatistics } from '@/features/statistics/services/statisticsApi'

export function useModelStatistics() {
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  async function loadData() {
    try {
      setLoading(true)
      const res = await getModelStatistics()
      setData(res)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadData()
  }, [])

  return {
    data,
    loading,
    error,
    reload: loadData,
  }
}
