// Helpers de formato reutilizables.

// Formatea un valor numérico como precio en soles (S/).
export function money(value) {
  const n = Number(value ?? 0)
  return `S/ ${n.toFixed(2)}`
}
