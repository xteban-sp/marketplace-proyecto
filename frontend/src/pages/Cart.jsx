import { Link } from 'react-router-dom'
import { useCart } from '../cart/CartContext.jsx'
import { money, optimizeImage } from '../utils/format.js'

export default function Cart() {
  const { items, remove, setQty, total, clear } = useCart()

  if (items.length === 0) {
    return (
      <main className="page page--narrow">
        <h1 className="hero__title">Tu carrito</h1>
        <div className="state">
          Está vacío por ahora.{' '}
          <Link to="/" className="link">Explora la feria</Link>
        </div>
      </main>
    )
  }

  return (
    <main className="page page--narrow">
      <h1 className="hero__title">Tu carrito</h1>

      <div className="cart">
        {items.map((i) => (
          <div className="cartline" key={i.id}>
            <div className="cartline__media">
              {i.imageUrl ? <img src={optimizeImage(i.imageUrl, 160)} alt={i.name} loading="lazy" /> : <span>{(i.name || '?').charAt(0)}</span>}
            </div>
            <div className="cartline__info">
              <Link to={`/producto/${i.id}`} className="cartline__name">{i.name}</Link>
              <span className="cartline__price">{money(i.price)}</span>
            </div>
            <div className="cartline__qty">
              <button onClick={() => setQty(i.id, i.qty - 1)} aria-label="menos">−</button>
              <span>{i.qty}</span>
              <button onClick={() => setQty(i.id, i.qty + 1)} aria-label="más">+</button>
            </div>
            <div className="cartline__sub">{money(i.price * i.qty)}</div>
            <button className="cartline__remove" onClick={() => remove(i.id)} aria-label="quitar">✕</button>
          </div>
        ))}
      </div>

      <div className="cart__foot">
        <button className="btn btn--ghost" onClick={clear}>Vaciar carrito</button>
        <div className="cart__total">
          <span>Total</span>
          <strong>{money(total)}</strong>
        </div>
      </div>

      <button className="btn btn--primary btn--block btn--lg" disabled title="Disponible en la siguiente etapa">
        Proceder al pago (próximamente)
      </button>
      <p className="detail__note">El pago con Mercado Pago se conecta en la siguiente etapa.</p>
    </main>
  )
}
