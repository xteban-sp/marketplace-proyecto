import { createContext, useContext, useEffect, useState, useCallback } from 'react'
import api from '../api/client.js'
import { useAuth } from '../auth/AuthContext.jsx'

const NotificationsContext = createContext(null)

export function NotificationsProvider({ children }) {
  const { user, token } = useAuth()
  const [items, setItems] = useState([])

  const refresh = useCallback(async () => {
    if (!token || !user?.userId) {
      setItems([])
      return
    }
    try {
      const { data } = await api.get('/api/notificaciones', { params: { usuarioId: user.userId } })
      const list = (data || []).slice().sort(
        (a, b) => new Date(b.createdAt) - new Date(a.createdAt),
      )
      setItems(list)
    } catch {
      /* silencioso: si el servicio no responde, no rompemos la UI */
    }
  }, [token, user])

  // Carga inicial + sondeo cada 20 s mientras haya sesión.
  useEffect(() => {
    refresh()
    if (!token) return
    const id = setInterval(refresh, 20000)
    return () => clearInterval(id)
  }, [refresh, token])

  async function markRead(id) {
    setItems((prev) => prev.map((n) => (n.id === id ? { ...n, leida: true } : n)))
    try {
      await api.patch(`/api/notificaciones/${id}/read`)
    } catch {
      /* si falla, el refresh siguiente corrige el estado */
    }
  }

  async function markAllRead() {
    const unread = items.filter((n) => !n.leida)
    setItems((prev) => prev.map((n) => ({ ...n, leida: true })))
    await Promise.all(
      unread.map((n) => api.patch(`/api/notificaciones/${n.id}/read`).catch(() => {})),
    )
  }

  const unread = items.filter((n) => !n.leida).length

  return (
    <NotificationsContext.Provider value={{ items, unread, refresh, markRead, markAllRead }}>
      {children}
    </NotificationsContext.Provider>
  )
}

export function useNotifications() {
  const ctx = useContext(NotificationsContext)
  if (!ctx) throw new Error('useNotifications debe usarse dentro de <NotificationsProvider>')
  return ctx
}
