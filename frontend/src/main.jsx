import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import App from './App.jsx'
import { AuthProvider } from './auth/AuthContext.jsx'
import { CartProvider } from './cart/CartContext.jsx'
import { NotificationsProvider } from './notifications/NotificationsContext.jsx'
import { ThemeProvider } from './theme/ThemeContext.jsx'
import { ToastProvider } from './toast/ToastContext.jsx'
import './index.css'

// Aplica el tema guardado ANTES del primer render (evita parpadeo claro/oscuro).
document.documentElement.dataset.theme = localStorage.getItem('mp_theme') || 'light'

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <BrowserRouter>
      <ThemeProvider>
        <ToastProvider>
          <AuthProvider>
            <NotificationsProvider>
              <CartProvider>
                <App />
              </CartProvider>
            </NotificationsProvider>
          </AuthProvider>
        </ToastProvider>
      </ThemeProvider>
    </BrowserRouter>
  </React.StrictMode>,
)
