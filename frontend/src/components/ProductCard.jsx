import { Link } from 'react-router-dom'
import { money } from '../utils/format.js'

export default function ProductCard({ product }) {
  return (
    <Link to={`/producto/${product.id}`} className="card">
      <div className="card__media">
        {product.imageUrl ? (
          <img src={product.imageUrl} alt={product.name} />
        ) : (
          <span className="card__placeholder">{(product.name || '?').charAt(0)}</span>
        )}
        {product.categoryName && <span className="card__chip">{product.categoryName}</span>}
      </div>
      <div className="card__body">
        <h3 className="card__title">{product.name}</h3>
        <p className="card__seller">{product.sellerName || 'Vendedor del campus'}</p>
        <div className="card__foot">
          <span className="card__price">{money(product.price)}</span>
          <span className={`card__stock ${product.stock > 0 ? '' : 'card__stock--out'}`}>
            {product.stock > 0 ? `${product.stock} disp.` : 'Agotado'}
          </span>
        </div>
      </div>
    </Link>
  )
}
