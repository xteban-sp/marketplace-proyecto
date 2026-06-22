import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext.jsx'
import { useCart } from '../cart/CartContext.jsx'

export default function Navbar() {
  const { user, logout, hasRole } = useAuth()
  const { count } = useCart()
  const navigate = useNavigate()
  const canSell = hasRole('SELLER') || hasRole('ADMIN')

  function handleLogout() {
    logout()
    navigate('/login')
  }

  const initial = (user?.username || '?').charAt(0).toUpperCase()

  return (
    <header className="nav">
      <Link to="/" className="nav__brand">
        <span className="nav__logo">Feria</span>
        <span className="nav__tag">marketplace universitario</span>
      </Link>

      <nav className="nav__links">
        <Link to="/">Catálogo</Link>
        {canSell && <Link to="/mis-productos">Mis productos</Link>}
      </nav>

      <div className="nav__right">
        {canSell ? (
          <Link to="/publicar" className="btn btn--primary nav__sell">+ Publicar</Link>
        ) : (
          <Link to="/vender" className="btn btn--ghost nav__sell">Vender</Link>
        )}

        <Link to="/carrito" className="nav__cart" aria-label="Carrito">
          🛒
          {count > 0 && <span className="nav__cart-badge" key={count}>{count}</span>}
        </Link>

        <span className="nav__user">
          <span className="nav__avatar">{initial}</span>
          <span className="nav__username">{user?.username}</span>
        </span>
        <button className="btn btn--ghost" onClick={handleLogout}>Salir</button>
      </div>
    </header>
  )
}
