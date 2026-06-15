// Sube una imagen a Cloudinary usando un "unsigned upload preset".
// Configura VITE_CLOUDINARY_CLOUD_NAME y VITE_CLOUDINARY_UPLOAD_PRESET en .env.
// Devuelve la URL segura (https) de la imagen subida.

const CLOUD_NAME = import.meta.env.VITE_CLOUDINARY_CLOUD_NAME
const UPLOAD_PRESET = import.meta.env.VITE_CLOUDINARY_UPLOAD_PRESET

export function cloudinaryConfigured() {
  return Boolean(CLOUD_NAME && UPLOAD_PRESET)
}

export async function uploadImage(file) {
  if (!cloudinaryConfigured()) {
    throw new Error('Cloudinary no está configurado (revisa el .env)')
  }
  const form = new FormData()
  form.append('file', file)
  form.append('upload_preset', UPLOAD_PRESET)

  const res = await fetch(`https://api.cloudinary.com/v1_1/${CLOUD_NAME}/image/upload`, {
    method: 'POST',
    body: form,
  })
  if (!res.ok) {
    throw new Error('No se pudo subir la imagen')
  }
  const data = await res.json()
  return data.secure_url
}
