import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import api, { errorMessage } from '../api/client.js'
import ImageUploader from '../components/ImageUploader.jsx'

export default function EditProduct() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [categories, setCategories] = useState([])
  const [form, setForm] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    api.get('/api/categorias').then((res) => setCategories(res.data || [])).catch(() => {})
    api
      .get(`/api/productos/${id}`)
      .then((res) => {
        const p = res.data
        setForm({
          name: p.name || '',
          description: p.description || '',
          price: p.price ?? '',
          stock: p.stock ?? '',
          categoryId: p.categoryId ?? '',
          imageUrl: p.imageUrl || '',
        })
      })
      .catch((err) => setError(errorMessage(err, 'No se encontró el producto')))
  }, [id])

  function update(field) {
    return (e) => setForm({ ...form, [field]: e.target.value })
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    if (!form.name.trim()) return setError('Ponle un nombre al producto')
    if (!form.categoryId) return setError('Elige una categoría')
    if (Number(form.price) <= 0) return setError('El precio debe ser mayor a 0')
    if (Number(form.stock) < 0) return setError('El stock no puede ser negativo')

    setLoading(true)
    try {
      const payload = {
        name: form.name.trim(),
        description: form.description.trim(),
        price: Number(form.price),
        stock: Number(form.stock),
        categoryId: Number(form.categoryId),
        // El backend DEBE derivar el vendedor del JWT, no confiar en un sellerId
        // enviado por el cliente (riesgo de suplantación de otro vendedor).
        // Si el backend todavía EXIGE sellerId en el body, descomenta la línea:
        // sellerId: user.userId,
        imageUrl: form.imageUrl || null,
      }
      await api.put(`/api/productos/${id}`, payload)
      navigate('/mis-productos')
    } catch (err) {
      setError(errorMessage(err, 'No se pudo guardar'))
    } finally {
      setLoading(false)
    }
  }

  if (error && !form) return <main className="page"><div className="alert alert--page">{error}</div></main>
  if (!form) return <main className="page"><div className="state">Cargando…</div></main>

  return (
    <main className="page page--narrow">
      <h1 className="hero__title">Editar producto</h1>

      {error && <div className="alert alert--page">{error}</div>}

      <form onSubmit={handleSubmit} className="form form--card">
        <ImageUploader value={form.imageUrl} onUploaded={(url) => setForm({ ...form, imageUrl: url })} />

        <label className="field">
          <span>URL de imagen (opcional)</span>
          <input value={form.imageUrl} onChange={update('imageUrl')} placeholder="https://…" />
        </label>

        <label className="field">
          <span>Nombre</span>
          <input value={form.name} onChange={update('name')} required />
        </label>

        <label className="field">
          <span>Descripción</span>
          <textarea value={form.description} onChange={update('description')} rows={3} />
        </label>

        <div className="form__row">
          <label className="field">
            <span>Precio (S/)</span>
            <input type="number" step="0.01" min="0" value={form.price} onChange={update('price')} required />
          </label>
          <label className="field">
            <span>Stock</span>
            <input type="number" min="0" value={form.stock} onChange={update('stock')} required />
          </label>
        </div>

        <label className="field">
          <span>Categoría</span>
          <select className="filters__select" value={form.categoryId} onChange={update('categoryId')} required>
            <option value="">Elige una categoría</option>
            {categories.map((c) => (
              <option key={c.id} value={c.id}>{c.name}</option>
            ))}
          </select>
        </label>

        <button className="btn btn--primary btn--block btn--lg" disabled={loading}>
          {loading ? 'Guardando…' : 'Guardar cambios'}
        </button>
        <button type="button" className="btn btn--ghost btn--block" onClick={() => navigate('/mis-productos')}>
          Cancelar
        </button>
      </form>
    </main>
  )
}
