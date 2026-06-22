import { createContext, useContext, useMemo, useState } from 'react'
import api from '../api/client.js'

const AuthContext = createContext(null)

// Lee los claims del JWT (sin verificar firma; solo para datos como userId/roles).
function decodeJwt(token) {
  try {
    return JSON.parse(atob(token.split('.')[1]))
  } catch {
    return {}
  }
}

// Comprueba si el token está vencido según su claim `exp` (segundos epoch).
// Si no trae `exp`, se considera válido (no podemos saberlo en cliente).
function isTokenExpired(token) {
  if (!token) return true
  const { exp } = decodeJwt(token)
  if (!exp) return false
  return Date.now() >= exp * 1000
}

// Estado inicial: descarta la sesión si el token guardado ya venció.
function readInitialSession() {
  const token = localStorage.getItem('mp_token')
  if (!token || isTokenExpired(token)) {
    localStorage.removeItem('mp_token')
    localStorage.removeItem('mp_user')
    return { token: null, user: null }
  }
  const raw = localStorage.getItem('mp_user')
  return { token, user: raw ? JSON.parse(raw) : null }
}

export function AuthProvider({ children }) {
  const initial = readInitialSession()
  const [token, setToken] = useState(initial.token)
  const [user, setUser] = useState(initial.user)

  function persist(authResponse) {
    const { token: newToken, username, roles } = authResponse
    const claims = decodeJwt(newToken)
    const newUser = {
      username,
      roles: roles || claims.roles || [],
      userId: claims.userId || null,
    }
    localStorage.setItem('mp_token', newToken)
    localStorage.setItem('mp_user', JSON.stringify(newUser))
    setToken(newToken)
    setUser(newUser)
  }

  async function login(username, password) {
    const { data } = await api.post('/api/auth/login', { username, password })
    persist(data)
    return data
  }

  async function register(form) {
    // El backend ignora roles: siempre crea USER.
    const { data } = await api.post('/api/auth/register', form)
    // Si hay verificación por correo, no llega token: NO iniciamos sesión.
    if (data.token) persist(data)
    return data
  }

  async function becomeSeller() {
    const { data } = await api.post('/api/auth/become-seller')
    persist(data)
    return data
  }

  function logout() {
    localStorage.removeItem('mp_token')
    localStorage.removeItem('mp_user')
    setToken(null)
    setUser(null)
  }

  // Acepta roles con o sin prefijo ROLE_ (el backend usa ambos formatos).
  function hasRole(role) {
    const roles = user?.roles || []
    return roles.some((r) => r === role || r === `ROLE_${role}`)
  }

  const value = useMemo(
    () => ({ token, user, login, register, logout, hasRole, becomeSeller }),
    [token, user],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth debe usarse dentro de <AuthProvider>')
  return ctx
}
