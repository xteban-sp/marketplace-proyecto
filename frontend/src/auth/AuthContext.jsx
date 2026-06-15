import { createContext, useContext, useState } from 'react'
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

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem('mp_token'))
  const [user, setUser] = useState(() => {
    const raw = localStorage.getItem('mp_user')
    return raw ? JSON.parse(raw) : null
  })

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

  return (
    <AuthContext.Provider value={{ token, user, login, register, logout, hasRole, becomeSeller }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth debe usarse dentro de <AuthProvider>')
  return ctx
}
