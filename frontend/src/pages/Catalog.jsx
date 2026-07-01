import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import api, { errorMessage } from '../api/client.js'
import { useAuth } from '../auth/AuthContext.jsx'
import ProductCard from '../components/ProductCard.jsx'

const SORTS = {
  recientes: 'createdAt,desc',
  'precio-asc': 'price,asc',
  'precio-desc': 'price,desc',
}

export default function Catalog() {
  const { hasRole } = useAuth()
  const navigate = useNavigate()
  const canSell = hasRole('SELLER') || hasRole('ADMIN')

  const [products, setProducts] = useState([])
  const [categories, setCategories] = useState([])
  const [name, setName] = useState('')
  const [debouncedName, setDebouncedName] = useState('')
  const [categoryId, setCategoryId] = useState('')
  const [sort, setSort] = useState('recientes')
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    api.get('/api/categorias').then((res) => setCategories(res.data || [])).catch(() => setCategories([]))
  }, [])

  // Debounce (~300 ms) del buscador.
  useEffect(() => {
    const t = setTimeout(() => {
      setDebouncedName(name.trim())
      setPage(0)
    }, 300)
    return () => clearTimeout(t)
  }, [name])

  useEffect(() => {
    let active = true
    setLoading(true)
    setError('')
    const hasFilter = debouncedName || categoryId
    const url = hasFilter ? '/api/productos/search' : '/api/productos'
    const params = { page, size: 12, sort: SORTS[sort] }
    if (debouncedName) params.name = debouncedName
    if (categoryId) params.categoryId = categoryId

    api
      .get(url, { params })
      .then((res) => {
        if (!active) return
        setProducts(res.data.content || [])
        setTotalPages(res.data.totalPages ?? 0)
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
  }, [debouncedName, categoryId, sort, page])

  function selectCategory(id) {
    setPage(0)
    setCategoryId(id)
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
        <input
          className="filters__search filters__search--hero"
          value={name}
          onChange={(e) => setName(e.target.value)}
          placeholder="Buscar producto…"
        />
      </section>

      <div className="catbar">
        <div className="chips">
          <button
            className={`chip ${!categoryId ? 'chip--active' : ''}`}
            onClick={() => selectCategory('')}
          >
            Todas
          </button>
          {categories.map((c) => (
            <button
              key={c.id}
              className={`chip ${categoryId === String(c.id) ? 'chip--active' : ''}`}
              onClick={() => selectCategory(String(c.id))}
            >
              {c.name}
            </button>
          ))}
        </div>
        <select
          className="filters__select"
          value={sort}
          onChange={(e) => {
            setPage(0)
            setSort(e.target.value)
          }}
        >
          <option value="recientes">Más recientes</option>
          <option value="precio-asc">Precio: menor a mayor</option>
          <option value="precio-desc">Precio: mayor a menor</option>
        </select>
      </div>

      {error && <div className="alert alert--page">{error}</div>}

      {loading ? (
        <div className="grid">
          {Array.from({ length: 8 }).map((_, i) => (
            <div className="card card--skeleton" key={i}>
              <div className="card__media skeleton" />
              <div className="card__body">
                <div className="skeleton sk-line sk-line--title" />
                <div className="skeleton sk-line sk-line--sm" style={{ marginTop: '0.5rem' }} />
                <div className="sk-foot">
                  <div className="skeleton sk-line" />
                  <div className="skeleton sk-line" />
                </div>
              </div>
            </div>
          ))}
        </div>
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
