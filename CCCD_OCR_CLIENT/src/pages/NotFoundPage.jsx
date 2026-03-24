import { Link } from 'react-router-dom'

function NotFoundPage() {
  return (
    <main className="container">
      <section className="card">
        <h1>404</h1>
        <p>Page not found.</p>

        <div className="actions">
          <Link className="button-link" to="/">
            Go to home
          </Link>
        </div>
      </section>
    </main>
  )
}

export default NotFoundPage
