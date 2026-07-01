import { useState } from 'react'
import { Link, useNavigate, useLocation } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext.jsx'
import { errorMessage } from '../api/client.js'

function initialInfo(location) {
  if (location.state?.info) return location.state.info
  const params = new URLSearchParams(location.search)
  if (params.get('verified') === '1') return '¡Cuenta activada! Ya puedes iniciar sesión.'
  if (params.get('verified') === '0') return 'El enlace de verificación no es válido o ya fue usado.'
  return ''
}

export default function Login() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [info] = useState(() => initialInfo(location))
  const [loading, setLoading] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await login(username.trim(), password)
      navigate('/')
    } catch (err) {
      setError(errorMessage(err, 'No se pudo iniciar sesión'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth">
      <div className="auth__art">
        <span className="auth__art-kicker">Compra y vende en tu campus</span>
        <h1 className="auth__art-title">
          Lo que necesitas,<br />de quien está al lado.
        </h1>
        <p className="auth__art-text">
          Libros, electrónica y más, entre estudiantes de la universidad.
        </p>
      </div>

      <div className="auth__panel">
        <div className="auth__box">
          <p className="auth__brand">Feria</p>
          <h2 className="auth__heading">Inicia sesión</h2>

          {info && <div className="notice">{info}</div>}
          {error && <div className="alert">{error}</div>}

          <form onSubmit={handleSubmit} className="form">
            <label className="field">
              <span>Usuario o correo</span>
              <input
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="tu_usuario o tu@correo.com"
                autoComplete="username"
                required
              />
            </label>
            <label className="field">
              <span>Contraseña</span>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••"
                autoComplete="current-password"
                required
              />
            </label>
            <button className="btn btn--primary btn--block" disabled={loading}>
              {loading ? 'Entrando…' : 'Entrar'}
            </button>
          </form>

          <p className="auth__switch">
            ¿No tienes cuenta? <Link to="/register">Regístrate</Link>
          </p>
        </div>
      </div>
    </div>
  )
}
