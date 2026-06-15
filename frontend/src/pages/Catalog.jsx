import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import api, { errorMessage } from '../api/client.js'
import { useAuth } from '../auth/AuthContext.jsx'
import ProductCard from '../components/ProductCard.jsx'

export default function Catalog() {
  const { hasRole } = useAuth()
  const navigate = useNavigate()
  const canSell = hasRole('SELLER') || hasRole('ADMIN')

  const [products, setProducts] = useState([])
  const [categories, setCategories] = useState([])
  const [name, setName] = useState('')
  const [categoryId, setCategoryId] = useState('')
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  // Carga las categorías una vez.
  useEffect(() => {
    api
      .get('/api/categorias')
      .then((res) => setCategories(res.data || []))
      .catch(() => setCategories([]))
  }, [])

  // Carga productos cuando cambian filtros o página.
  useEffect(() => {
    let active = true
    setLoading(true)
    setError('')
    const hasFilter = name.trim() || categoryId
    const url = hasFilter ? '/api/productos/search' : '/api/productos'
    const params = { page, size: 12 }
    if (name.trim()) params.name = name.trim()
    if (categoryId) params.categoryId = categoryId

    api
      .get(url, { params })
      .then((res) => {
        if (!active) return
        const data = res.data
        setProducts(data.content || [])
        setTotalPages(data.totalPages ?? 0)
      })
      .catch((err) => {
        if (!active) return
        setError(errorMessage(err, 'No se pudieron cargar los productos'))
        setProducts([])
      })
      .finally(() => active && setLoading(false))

    return () => {
      active = false
    }
  }, [name, categoryId, page])

  function onSearchChange(e) {
    setPage(0)
    setName(e.target.value)
  }
  function onCategoryChange(e) {
    setPage(0)
    setCategoryId(e.target.value)
  }

  return (
    <main className="page">
      {!canSell && (
        <div className="sellbar">
          <div>
            <strong>¿Tienes algo para vender?</strong>
            <span> Conviértete en vendedor y publica tus productos.</span>
          </div>
          <button className="btn btn--primary" onClick={() => navigate('/vender')}>
            Quiero vender
          </button>
        </div>
      )}

      <section className="hero">
        <h1 className="hero__title">Explora la feria</h1>
        <p className="hero__sub">Encuentra lo que buscas entre tus compañeros.</p>
        <div className="filters">
          <input
            className="filters__search"
            value={name}
            onChange={onSearchChange}
            placeholder="Buscar producto…"
          />
          <select className="filters__select" value={categoryId} onChange={onCategoryChange}>
            <option value="">Todas las categorías</option>
            {categories.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
          </select>
        </div>
      </section>

      {error && <div className="alert alert--page">{error}</div>}

      {loading ? (
        <div className="state">Cargando productos…</div>
      ) : products.length === 0 ? (
        <div className="state">No hay productos que coincidan.</div>
      ) : (
        <div className="grid">
          {products.map((p) => (
            <ProductCard key={p.id} product={p} />
          ))}
        </div>
      )}

      {totalPages > 1 && (
        <div className="pager">
          <button className="btn btn--ghost" disabled={page === 0} onClick={() => setPage(page - 1)}>
            ← Anterior
          </button>
          <span className="pager__info">
            Página {page + 1} de {totalPages}
          </span>
          <button
            className="btn btn--ghost"
            disabled={page + 1 >= totalPages}
            onClick={() => setPage(page + 1)}
          >
            Siguiente →
          </button>
        </div>
      )}
    </main>
  )
}
