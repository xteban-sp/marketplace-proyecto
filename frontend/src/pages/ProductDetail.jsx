import { useEffect, useState } from 'react'
import { useParams, Link, useNavigate } from 'react-router-dom'
import api, { errorMessage } from '../api/client.js'
import { useCart } from '../cart/CartContext.jsx'
import { money } from '../utils/format.js'

export default function ProductDetail() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { add } = useCart()
  const [product, setProduct] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [added, setAdded] = useState(false)

  function addToCart() {
    add(product, 1)
    setAdded(true)
    setTimeout(() => setAdded(false), 1500)
  }

  useEffect(() => {
    let active = true
    setLoading(true)
    api
      .get(`/api/productos/${id}`)
      .then((res) => active && setProduct(res.data))
      .catch((err) => active && setError(errorMessage(err, 'No se encontró el producto')))
      .finally(() => active && setLoading(false))
    return () => {
      active = false
    }
  }, [id])

  if (loading) return <main className="page"><div className="state">Cargando…</div></main>
  if (error) return <main className="page"><div className="alert alert--page">{error}</div></main>
  if (!product) return null

  return (
    <main className="page">
      <Link to="/" className="back">← Volver al catálogo</Link>

      <div className="detail">
        <div className="detail__media">
          {product.imageUrl ? (
            <img src={product.imageUrl} alt={product.name} />
          ) : (
            <span className="detail__placeholder">{(product.name || '?').charAt(0)}</span>
          )}
        </div>

        <div className="detail__info">
          {product.categoryName && <span className="detail__chip">{product.categoryName}</span>}
          <h1 className="detail__title">{product.name}</h1>
          <p className="detail__seller">Vendido por {product.sellerName || 'un estudiante del campus'}</p>
          <p className="detail__price">{money(product.price)}</p>
          <p className={`detail__stock ${product.stock > 0 ? '' : 'detail__stock--out'}`}>
            {product.stock > 0 ? `${product.stock} unidades disponibles` : 'Sin stock'}
          </p>
          <p className="detail__desc">{product.description || 'Sin descripción.'}</p>

          <div className="detail__actions">
            <button className={`btn btn--primary btn--lg ${added ? 'btn--added' : ''}`} disabled={product.stock <= 0} onClick={addToCart}>
              {product.stock > 0 ? (added ? '✓ Agregado' : 'Agregar al carrito') : 'Agotado'}
            </button>
            <button className="btn btn--ghost btn--lg" onClick={() => navigate('/carrito')}>
              Ver carrito
            </button>
          </div>
          <p className="detail__note">El pago con Mercado Pago se conecta en la siguiente etapa.</p>
        </div>
      </div>
    </main>
  )
}
