import ModelStatisticsWorkspace from '@/features/statistics/components/ModelStatisticsWorkspace'
import SectionTitle from '@/components/common/SectionTitle'

export default function StatisticsPage() {
  return (
    <main className="container" style={{ padding: '2rem 0' }}>
      <SectionTitle title="Thống kê Mô hình" subtitle="Dashboard tổng quan về các mô hình AI" />
      <ModelStatisticsWorkspace />
    </main>
  )
}
