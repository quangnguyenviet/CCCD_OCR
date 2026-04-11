import React, { useMemo } from 'react'
import { PieChart, Pie, Cell, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts'
import { useModelStatistics } from '@/features/statistics/hooks/useModelStatistics'
import './ModelStatistics.css'

const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042', '#AF19FF']

export default function ModelStatisticsWorkspace() {
  const { data, loading, error, reload } = useModelStatistics()

  const typeData = useMemo(() => {
    if (!data?.modelsByType) return []
    return Object.entries(data.modelsByType).map(([k, v]) => ({ name: k, value: v }))
  }, [data])

  const statusData = useMemo(() => {
    if (!data?.modelsByStatus) return []
    return Object.entries(data.modelsByStatus).map(([k, v]) => ({ name: k, value: v }))
  }, [data])

  if (loading) return <div className="card">Đang tải dữ liệu thống kê...</div>
  if (error) return (
    <div className="card error-container">
      <p style={{ color: 'red' }}>Lỗi: {error}</p>
      <button onClick={reload} className="button" style={{ marginTop: '1rem' }}>Thử lại</button>
    </div>
  )

  return (
    <div className="statistics-workspace">
      <div className="stats-summary">
        <div className="stat-card total-card">
          <h3>Tổng số mô hình</h3>
          <p className="stat-number">{data?.totalModels ?? 0}</p>
        </div>
      </div>

      <div className="charts-container">
        <div className="chart-card">
          <h3>Phân bổ theo Loại Mô hình</h3>
          <div className="chart-wrapper">
             <ResponsiveContainer width="100%" height={300}>
               <BarChart data={typeData} margin={{ top: 20, right: 30, left: 0, bottom: 5 }}>
                 <CartesianGrid strokeDasharray="3 3" />
                 <XAxis dataKey="name" />
                 <YAxis />
                 <Tooltip />
                 <Legend />
                 <Bar dataKey="value" name="Số lượng" fill="#3b82f6" />
               </BarChart>
             </ResponsiveContainer>
          </div>
        </div>

        <div className="chart-card">
          <h3>Tỷ lệ Trạng thái (Status)</h3>
          <div className="chart-wrapper">
             <ResponsiveContainer width="100%" height={300}>
               <PieChart>
                 <Pie
                   data={statusData}
                   cx="50%"
                   cy="50%"
                   labelLine={true}
                   label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
                   outerRadius={100}
                   fill="#8884d8"
                   dataKey="value"
                 >
                   {statusData.map((entry, index) => (
                     <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                   ))}
                 </Pie>
                 <Tooltip />
               </PieChart>
             </ResponsiveContainer>
          </div>
        </div>
      </div>
    </div>
  )
}
