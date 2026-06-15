import { Routes, Route, Navigate } from 'react-router-dom'
import { useAuth } from './auth/AuthContext.jsx'
import Navbar from './components/Navbar.jsx'
import Footer from './components/Footer.jsx'
import Login from './pages/Login.jsx'
import Register from './pages/Register.jsx'
import Catalog from './pages/Catalog.jsx'
import ProductDetail from './pages/ProductDetail.jsx'
import NewProduct from './pages/NewProduct.jsx'
import EditProduct from './pages/EditProduct.jsx'
import MyProducts from './pages/MyProducts.jsx'
import BecomeSeller from './pages/BecomeSeller.jsx'
import Cart from './pages/Cart.jsx'
import NotFound from './pages/NotFound.jsx'

function ProtectedRoute({ children }) {
  const { token } = useAuth()
  if (!token) return <Navigate to="/login" replace />
  return children
}

function SellerRoute({ children }) {
  const { token, hasRole } = useAuth()
  if (!token) return <Navigate to="/login" replace />
  if (!hasRole('SELLER') && !hasRole('ADMIN')) return <Navigate to="/" replace />
  return children
}

export default function App() {
  const { token } = useAuth()
  return (
    <div className="app">
      {token && <Navbar />}
      <div className="app__content">
        <Routes>
          <Route path="/login" element={token ? <Navigate to="/" replace /> : <Login />} />
          <Route path="/register" element={token ? <Navigate to="/" replace /> : <Register />} />
          <Route path="/" element={<ProtectedRoute><Catalog /></ProtectedRoute>} />
          <Route path="/producto/:id" element={<ProtectedRoute><ProductDetail /></ProtectedRoute>} />
          <Route path="/carrito" element={<ProtectedRoute><Cart /></ProtectedRoute>} />
          <Route path="/vender" element={<ProtectedRoute><BecomeSeller /></ProtectedRoute>} />
          <Route path="/publicar" element={<SellerRoute><NewProduct /></SellerRoute>} />
          <Route path="/mis-productos" element={<SellerRoute><MyProducts /></SellerRoute>} />
          <Route path="/editar/:id" element={<SellerRoute><EditProduct /></SellerRoute>} />
          <Route path="*" element={<NotFound />} />
        </Routes>
      </div>
      {token && <Footer />}
    </div>
  )
}
