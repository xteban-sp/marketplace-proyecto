import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080',
})

// Inyecta el JWT guardado en cada petición.
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('mp_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Si el token caduca o es inválido, limpia y manda al login.
api.interceptors.response.use(
  (res) => res,
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('mp_token')
      localStorage.removeItem('mp_user')
      if (!window.location.pathname.startsWith('/login')) {
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  },
)

// Extrae un mensaje legible del formato de error uniforme del backend.
export function errorMessage(error, fallback = 'Ocurrió un error') {
  const data = error?.response?.data
  if (!data) return fallback
  if (typeof data === 'string') return data
  if (data.message) return data.message
  if (data.error) return data.error
  if (data.fieldErrors) return Object.values(data.fieldErrors).join(' · ')
  return fallback
}

export default api
