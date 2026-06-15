export default function Footer() {
  return (
    <footer className="footer">
      <span className="footer__brand">Feria</span>
      <span className="footer__text">Marketplace universitario · Proyecto DAD</span>
      <span className="footer__year">© {new Date().getFullYear()}</span>
    </footer>
  )
}
