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
            <section className="auth__art" aria-hidden="true">
                <div className="auth__art-bg" />
                <div className="auth__art-content">
                    <div className="auth__art-logo">
                        <img src="/frontend/src/image/LogoCompraPe.png" alt="CompraPe"/>
                        <span className="auth__art-logo-name">CompraPe</span>
                    </div>
                    <p className="auth__art-kicker">Tu mercado segurisisimo</p>
                    <h1 className="auth__art-title">
                        Cómprale a tu causa,<br />vende a tu equipo osiosiosi .
                    </h1>
                    <p className="auth__art-text">
                        Intercambia fácil en tu universidad.
                    </p>
                </div>
            </section>

            <div className="auth__panel">
                <div className="auth__box">
                    <p className="auth__eyebrow">Bienvenido de vuelta</p>
                    <h2 className="auth__heading">Iniciar sesión</h2>
                    <p className="auth__sub">Ingresa tus credenciales para continuar</p>

                    {info  && <div className="notice">{info}</div>}
                    {error && <div className="alert">{error}</div>}

                    <form onSubmit={handleSubmit} className="form">
                        <label className="field">
                            <span>Usuario</span>
                            <input
                                value={username}
                                onChange={(e) => setUsername(e.target.value)}
                                placeholder="Escribe aqui"
                                autoComplete="username"
                                required
                                autoFocus
                            />
                        </label>
                        <label className="field">
                            <span>Contraseña</span>
                            <input
                                type="password"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                placeholder="Escribe aqui"
                                autoComplete="current-password"
                                required
                            />
                        </label>
                        <button className="btn btn--primary btn--block btn--lg" disabled={loading}>
                            {loading ? 'Verificando…' : 'Entrar'}
                        </button>
                    </form>

                    <p className="auth__switch">
                        ¿No tienes cuenta? <Link to="/register">Regístrate gratis</Link>
                    </p>
                </div>
            </div>
        </div>
    )
}