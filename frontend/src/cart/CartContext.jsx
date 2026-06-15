import { createContext, useContext, useEffect, useState } from 'react'

const CartContext = createContext(null)

export function CartProvider({ children }) {
  const [items, setItems] = useState(() => {
    const raw = localStorage.getItem('mp_cart')
    return raw ? JSON.parse(raw) : []
  })

  useEffect(() => {
    localStorage.setItem('mp_cart', JSON.stringify(items))
  }, [items])

  function add(product, qty = 1) {
    setItems((prev) => {
      const existing = prev.find((i) => i.id === product.id)
      if (existing) {
        return prev.map((i) =>
          i.id === product.id ? { ...i, qty: Math.min(i.qty + qty, product.stock || 99) } : i,
        )
      }
      return [
        ...prev,
        {
          id: product.id,
          name: product.name,
          price: Number(product.price),
          imageUrl: product.imageUrl || null,
          stock: product.stock,
          qty,
        },
      ]
    })
  }

  function remove(id) {
    setItems((prev) => prev.filter((i) => i.id !== id))
  }

  function setQty(id, qty) {
    setItems((prev) => prev.map((i) => (i.id === id ? { ...i, qty: Math.max(1, qty) } : i)))
  }

  function clear() {
    setItems([])
  }

  const count = items.reduce((n, i) => n + i.qty, 0)
  const total = items.reduce((s, i) => s + i.price * i.qty, 0)

  return (
    <CartContext.Provider value={{ items, add, remove, setQty, clear, count, total }}>
      {children}
    </CartContext.Provider>
  )
}

export function useCart() {
  const ctx = useContext(CartContext)
  if (!ctx) throw new Error('useCart debe usarse dentro de <CartProvider>')
  return ctx
}
