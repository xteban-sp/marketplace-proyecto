import { useEffect } from 'react'
import { useNotifications } from '../notifications/NotificationsContext.jsx'

const ICON = { ORDER: '📦', PAYMENT: '💳', MESSAGE: '💬', REVIEW: '⭐', SYSTEM: '🔔' }

function timeAgo(iso) {
  if (!iso) return ''
  const diff = (Date.now() - new Date(iso).getTime()) / 1000
  if (diff < 60) return 'hace un momento'
  if (diff < 3600) return `hace ${Math.floor(diff / 60)} min`
  if (diff < 86400) return `hace ${Math.floor(diff / 3600)} h`
  return `hace ${Math.floor(diff / 86400)} d`
}

export default function Notifications() {
  const { items, unread, markRead, markAllRead, refresh } = useNotifications()

  useEffect(() => {
    refresh()
  }, []) // eslint-disable-line react-hooks/exhaustive-deps

  return (
    <main className="page page--narrow">
      <div className="myhead">
        <div>
          <h1 className="hero__title">Notificaciones</h1>
          <p className="hero__sub">
            {unread > 0 ? `Tienes ${unread} sin leer` : 'Estás al día'}
          </p>
        </div>
        {unread > 0 && (
          <button className="btn btn--ghost" onClick={markAllRead}>
            Marcar todas como leídas
          </button>
        )}
      </div>

      {items.length === 0 ? (
        <div className="state">No tienes notificaciones todavía.</div>
      ) : (
        <div className="notiflist">
          {items.map((n) => (
            <button
              key={n.id}
              className={`notif ${n.leida ? '' : 'notif--unread'}`}
              onClick={() => !n.leida && markRead(n.id)}
            >
              <span className="notif__icon">{ICON[n.tipo] || '🔔'}</span>
              <span className="notif__body">
                <span className="notif__title">{n.titulo}</span>
                <span className="notif__msg">{n.mensaje}</span>
                <span className="notif__time">{timeAgo(n.createdAt)}</span>
              </span>
              {!n.leida && <span className="notif__dot" aria-label="sin leer" />}
            </button>
          ))}
        </div>
      )}
    </main>
  )
}
