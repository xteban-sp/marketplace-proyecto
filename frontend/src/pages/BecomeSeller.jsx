import { useState } from 'react'
import { useNavigate, Navigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext.jsx'
import { errorMessage } from '../api/client.js'

export default function BecomeSeller() {
  const { becomeSeller, hasRole } = useAuth()
  const navigate = useNavigate()
  const [accepted, setAccepted] = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  // Si ya es vendedor, no tiene sentido estar aquí.
  if (hasRole('SELLER') || hasRole('ADMIN')) {
    return <Navigate to="/publicar" replace />
  }

  async function activar() {
    setError('')
    setLoading(true)
    try {
      await becomeSeller()
      navigate('/publicar')
    } catch (err) {
      setError(errorMessage(err, 'No se pudo activar tu cuenta de vendedor'))
      setLoading(false)
    }
  }

  return (
    <main className="page page--narrow">
      <h1 className="hero__title">Conviértete en vendedor</h1>
      <p className="hero__sub">Publica tus productos y véndele a otros estudiantes.</p>

      <div className="onboard">
        <ul className="onboard__list">
          <li><strong>Publica fácil:</strong> sube foto, precio y stock en segundos.</li>
          <li><strong>Tú gestionas:</strong> edita o retira tus productos cuando quieras.</li>
          <li><strong>Cobra seguro:</strong> los pagos se procesan con Mercado Pago.</li>
        </ul>

        <div className="onboard__terms">
          <h3>Compromiso del vendedor</h3>
          <p>
            Al activar tu cuenta de vendedor te comprometes a publicar productos reales,
            con información veraz, y a respetar a los compradores del campus.
          </p>
        </div>

        {error && <div className="alert">{error}</div>}

        <label className="check">
          <input type="checkbox" checked={accepted} onChange={(e) => setAccepted(e.target.checked)} />
          <span>Acepto el compromiso del vendedor.</span>
        </label>

        <button className="btn btn--primary btn--block btn--lg" disabled={!accepted || loading} onClick={activar}>
          {loading ? 'Activando…' : 'Activar cuenta de vendedor'}
        </button>
        <button className="btn btn--ghost btn--block" onClick={() => navigate('/')} disabled={loading}>
          Ahora no
        </button>
      </div>
    </main>
  )
}
