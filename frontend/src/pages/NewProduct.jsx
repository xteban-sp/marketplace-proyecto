import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import api, { errorMessage } from '../api/client.js'
import ImageUploader from '../components/ImageUploader.jsx'

export default function NewProduct() {
  const navigate = useNavigate()
  const [categories, setCategories] = useState([])
  const [form, setForm] = useState({
    name: '',
    description: '',
    price: '',
    stock: '',
    categoryId: '',
    imageUrl: '',
  })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    api.get('/api/categorias').then((res) => setCategories(res.data || [])).catch(() => {})
  }, [])

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
      const { data } = await api.post('/api/productos', payload)
      navigate(`/producto/${data.id}`)
    } catch (err) {
      setError(errorMessage(err, 'No se pudo publicar el producto'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <main className="page page--narrow">
      <h1 className="hero__title">Publicar producto</h1>
      <p className="hero__sub">Sube una foto y completa los datos.</p>

      {error && <div className="alert alert--page">{error}</div>}

      <form onSubmit={handleSubmit} className="form form--card">
        <ImageUploader value={form.imageUrl} onUploaded={(url) => setForm({ ...form, imageUrl: url })} />

        <label className="field">
          <span>URL de imagen (opcional, si no subes foto)</span>
          <input value={form.imageUrl} onChange={update('imageUrl')} placeholder="https://…" />
        </label>

        <label className="field">
          <span>Nombre</span>
          <input value={form.name} onChange={update('name')} placeholder="Ej. Audífonos Bluetooth" required />
        </label>

        <label className="field">
          <span>Descripción</span>
          <textarea value={form.description} onChange={update('description')} rows={3} placeholder="Detalles del producto…" />
        </label>

        <div className="form__row">
          <label className="field">
            <span>Precio (S/)</span>
            <input type="number" step="0.01" min="0" value={form.price} onChange={update('price')} placeholder="0.00" required />
          </label>
          <label className="field">
            <span>Stock</span>
            <input type="number" min="0" value={form.stock} onChange={update('stock')} placeholder="0" required />
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
          {loading ? 'Publicando…' : 'Publicar producto'}
        </button>
      </form>
    </main>
  )
}
