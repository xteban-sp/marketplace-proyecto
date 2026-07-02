import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext.jsx'
import { errorMessage } from '../api/client.js'

const EMPTY = {
    fullName: '',
    dni: '',
    email: '',
    universityCode: '',
    phone: '',
    username: '',
    password: '',
}

export default function Register() {
    const { register } = useAuth()
    const navigate = useNavigate()
    const [form, setForm] = useState(EMPTY)
    const [error, setError] = useState('')
    const [loading, setLoading] = useState(false)

    function update(field) {
        return (e) => setForm({ ...form, [field]: e.target.value })
    }

    function validate() {
        if (form.fullName.trim().length < 3)        return 'Ingresa tu nombre completo'
        if (!/^\d{8}$/.test(form.dni))              return 'El DNI debe tener 8 dígitos'
        if (!/^\S+@\S+\.\S+$/.test(form.email))     return 'Email inválido'
        if (form.universityCode.trim().length < 6)   return 'Código universitario inválido'
        if (!/^\d{9}$/.test(form.phone))            return 'El celular debe tener 9 dígitos'
        if (form.username.trim().length < 4)         return 'El usuario debe tener al menos 4 caracteres'
        if (form.password.length < 6)                return 'La contraseña debe tener al menos 6 caracteres'
        return ''
    }

    async function handleSubmit(e) {
        e.preventDefault()
        const v = validate()
        if (v) { setError(v); return }
        setError('')
        setLoading(true)
        try {
            const data = await register({ ...form, dni: form.dni.trim(), username: form.username.trim() })
            if (data.token) {
                navigate('/')
            } else {
                navigate('/login', { state: { info: data.message || 'Revisa tu correo para activar tu cuenta.' } })
            }
        } catch (err) {
            setError(errorMessage(err, 'No se pudo crear la cuenta'))
        } finally {
            setLoading(false)
        }
    }

    return (
        <div className="auth">
            <section className="auth__art auth__art--alt" aria-hidden="true">
                <div className="auth__art-bg" />
                <div className="auth__art-content">
                    <div className="auth__art-logo">
                        <svg viewBox="0 0 38 38" fill="none" width="38" height="38">
                            <rect width="38" height="38" rx="10" fill="var(--color-accent)" />
                            <path d="M11 11h16M11 19h10M11 27h13" stroke="#fff" strokeWidth="2.5" strokeLinecap="round" />
                            <circle cx="27" cy="23" r="5" stroke="#fff" strokeWidth="2" />
                            <path d="M27 21v2.5l1.5 1.5" stroke="#fff" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" />
                        </svg>
                        <span className="auth__art-logo-name">Feria</span>
                    </div>
                    <p className="auth__art-kicker">Únete a la feria</p>
                    <h1 className="auth__art-title">
                        Crea tu cuenta<br />y empieza a comprar.
                    </h1>
                    <p className="auth__art-text">
                        Solo necesitas tus datos universitarios para registrarte.
                    </p>
                </div>
            </section>

            <div className="auth__panel">
                <div className="auth__box">
                    <p className="auth__eyebrow">Nuevo en Feria</p>
                    <h2 className="auth__heading">Crear cuenta</h2>
                    <p className="auth__sub">Completa tus datos universitarios</p>

                    {error && <div className="alert">{error}</div>}

                    <form onSubmit={handleSubmit} className="form">
                        <label className="field">
                            <span>Nombre completo</span>
                            <input value={form.fullName} onChange={update('fullName')} placeholder="Juan Pérez" required />
                        </label>
                        <div className="form__row">
                            <label className="field">
                                <span>DNI</span>
                                <input value={form.dni} onChange={update('dni')} placeholder="12345678" maxLength={8} required />
                            </label>
                            <label className="field">
                                <span>Celular</span>
                                <input value={form.phone} onChange={update('phone')} placeholder="987654321" maxLength={9} required />
                            </label>
                        </div>
                        <label className="field">
                            <span>Email</span>
                            <input type="email" value={form.email} onChange={update('email')} placeholder="juan@upeu.edu.pe" required />
                        </label>
                        <label className="field">
                            <span>Código universitario</span>
                            <input value={form.universityCode} onChange={update('universityCode')} placeholder="202012345" maxLength={9} required />
                        </label>
                        <div className="form__row">
                            <label className="field">
                                <span>Usuario</span>
                                <input value={form.username} onChange={update('username')} placeholder="juanp" required />
                            </label>
                            <label className="field">
                                <span>Contraseña</span>
                                <input type="password" value={form.password} onChange={update('password')} placeholder="••••••••" required />
                            </label>
                        </div>
                        <button className="btn btn--primary btn--block btn--lg" disabled={loading}>
                            {loading ? 'Creando…' : 'Crear cuenta'}
                        </button>
                    </form>

                    <p className="auth__switch">
                        ¿Ya tienes cuenta? <Link to="/login">Inicia sesión</Link>
                    </p>
                </div>
            </div>
        </div>
    )
}