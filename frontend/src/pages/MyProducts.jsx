import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import api, { errorMessage } from '../api/client.js'
import { useAuth } from '../auth/AuthContext.jsx'
import { money, optimizeImage } from '../utils/format.js'

export default function MyProducts() {
  const { user } = useAuth()
  const [products, setProducts] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  function load() {
    if (!user?.userId) {
      setLoading(false)
      return
    }
    setLoading(true)
    api
      .get(`/api/productos/seller/${user.userId}`)
      .then((res) => setProducts(res.data || []))
      .catch((err) => setError(errorMessage(err, 'No se pudieron cargar tus productos')))
      .finally(() => setLoading(false))
  }

  useEffect(load, [user])

  async function handleDelete(id) {
    if (!window.confirm('¿Eliminar este producto? Dejará de mostrarse en el catálogo.')) return
    try {
      await api.delete(`/api/productos/${id}`)
      setProducts((prev) => prev.filter((p) => p.id !== id))
    } catch (err) {
      alert(errorMessage(err, 'No se pudo eliminar'))
    }
  }

  return (
    <main className="page">
      <div className="myhead">
        <div>
          <h1 className="hero__title">Mis productos</h1>
          <p className="hero__sub">Gestiona lo que publicas en la feria.</p>
        </div>
        <Link to="/publicar" className="btn btn--primary">+ Publicar</Link>
      </div>

      {error && <div className="alert alert--page">{error}</div>}

      {loading ? (
        <div className="state">Cargando…</div>
      ) : products.length === 0 ? (
        <div className="state">Aún no has publicado productos. ¡Anímate a publicar el primero!</div>
      ) : (
        <div className="mylist">
          {products.map((p) => (
            <div className="myrow" key={p.id}>
              <div className="myrow__media">
                {p.imageUrl ? <img src={optimizeImage(p.imageUrl, 160)} alt={p.name} loading="lazy" /> : <span>{(p.name || '?').charAt(0)}</span>}
              </div>
              <div className="myrow__info">
                <Link to={`/producto/${p.id}`} className="myrow__name">{p.name}</Link>
                <span className="myrow__meta">{p.categoryName} · {p.stock} en stock</span>
              </div>
              <div className="myrow__price">{money(p.price)}</div>
              <div className="myrow__actions">
                <Link to={`/editar/${p.id}`} className="btn btn--ghost">Editar</Link>
                <button className="btn btn--ghost btn--danger" onClick={() => handleDelete(p.id)}>Eliminar</button>
              </div>
            </div>
          ))}
        </div>
      )}
    </main>
  )
}
