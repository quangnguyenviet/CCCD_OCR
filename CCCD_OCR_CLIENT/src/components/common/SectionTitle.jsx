function SectionTitle({ title, subtitle }) {
  return (
    <header className="section-title">
      <h1>{title}</h1>
      {subtitle ? <p className="muted">{subtitle}</p> : null}
    </header>
  )
}

export default SectionTitle
