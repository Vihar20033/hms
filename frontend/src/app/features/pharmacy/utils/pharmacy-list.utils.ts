import { Medicine } from '../models/pharmacy.models';

export function filterMedicinesByLowStock(medicines: Medicine[], showLowStockOnly: boolean): Medicine[] {
  if (!showLowStockOnly) {
    return medicines;
  }

  return medicines.filter((medicine) => medicine.quantityInStock <= medicine.reorderLevel);
}

export function isMedicineLowStock(medicine: Medicine): boolean {
  const threshold = medicine.reorderLevel ?? 10;
  return medicine.quantityInStock <= threshold;
}

export function buildCategoryOptions(categories: string[]): Array<{ label: string; value: string }> {
  return categories.map((category) => ({ label: category, value: category }));
}

export function formatMedicineDate(value: Date | string | null): string | null {
  if (!value) {
    return null;
  }

  if (typeof value === 'string') {
    return value;
  }

  const year = value.getFullYear();
  const month = String(value.getMonth() + 1).padStart(2, '0');
  const day = String(value.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}
