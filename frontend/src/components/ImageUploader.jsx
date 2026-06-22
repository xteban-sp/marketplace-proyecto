import { useRef, useState } from 'react'
import { uploadImage, cloudinaryConfigured } from '../api/cloudinary.js'

// Sube una foto (cámara o archivo) a Cloudinary y devuelve la URL vía onUploaded.
export default function ImageUploader({ value, onUploaded }) {
  const inputRef = useRef(null)
  const [uploading, setUploading] = useState(false)
  const [error, setError] = useState('')

  async function handleFile(e) {
    const file = e.target.files?.[0]
    if (!file) return
    setError('')
    setUploading(true)
    try {
      const url = await uploadImage(file)
      onUploaded(url)
    } catch (err) {
      setError(err.message || 'Error al subir la imagen')
    } finally {
      setUploading(false)
    }
  }

  if (!cloudinaryConfigured()) {
    return (
      <div className="uploader uploader--off">
        Subida de fotos deshabilitada: falta configurar Cloudinary en el <code>.env</code>.
        Por ahora puedes pegar una URL de imagen.
      </div>
    )
  }

  return (
    <div className="uploader">
      <button
        type="button"
        className="uploader__preview"
        onClick={() => inputRef.current?.click()}
        aria-label={value ? 'Cambiar foto' : 'Subir o tomar foto'}
      >
        {value ? <img src={value} alt="Vista previa" /> : <span>+ Foto</span>}
      </button>
      <div className="uploader__actions">
        <button type="button" className="btn btn--ghost" onClick={() => inputRef.current?.click()} disabled={uploading}>
          {uploading ? 'Subiendo…' : value ? 'Cambiar foto' : 'Subir / tomar foto'}
        </button>
        {value && (
          <button type="button" className="btn btn--ghost" onClick={() => onUploaded('')} disabled={uploading}>
            Quitar
          </button>
        )}
      </div>
      {/* capture abre la cámara en móviles */}
      <input
        ref={inputRef}
        type="file"
        accept="image/*"
        capture="environment"
        hidden
        onChange={handleFile}
      />
      {error && <div className="alert">{error}</div>}
    </div>
  )
}
