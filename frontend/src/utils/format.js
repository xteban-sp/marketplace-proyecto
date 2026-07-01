// Helpers de formato reutilizables.

// Formatea un valor numérico como precio en soles (S/).
export function money(value) {
  const n = Number(value ?? 0)
  return `S/ ${n.toFixed(2)}`
}

// Optimiza imágenes servidas por Cloudinary: inserta f_auto (mejor formato,
// WebP/AVIF), q_auto (calidad automática) y un ancho objetivo, para reducir
// muchísimo el peso/ancho de banda. Para URLs que NO son de Cloudinary
// (p. ej. picsum) las devuelve sin tocar.
export function optimizeImage(url, width = 600) {
  if (!url) return url
  if (url.includes('res.cloudinary.com') && url.includes('/upload/')) {
    return url.replace('/upload/', `/upload/f_auto,q_auto,w_${width}/`)
  }
  return url
}
