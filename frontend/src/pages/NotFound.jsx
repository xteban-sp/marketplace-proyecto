import { Link } from 'react-router-dom'

export default function NotFound() {
  return (
    <main className="page page--narrow">
      <div className="notfound">
        <span className="notfound__code">404</span>
        <h1 className="hero__title">Esta página no existe</h1>
        <p className="hero__sub">Puede que el enlace esté roto o que la página se haya movido.</p>
        <Link to="/" className="btn btn--primary btn--lg">Volver a la feria</Link>
      </div>
    </main>
  )
}
