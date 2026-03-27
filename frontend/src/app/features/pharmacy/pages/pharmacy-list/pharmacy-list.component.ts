import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CalendarModule } from 'primeng/calendar';
import { DropdownModule } from 'primeng/dropdown';
import { InputNumberModule } from 'primeng/inputnumber';
import { InputTextModule } from 'primeng/inputtext';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { TableModule } from 'primeng/table';
import { ApiResponse } from '../../../../core/models/common.models';
import { Medicine, MedicineCategory } from '../../../../core/models/pharmacy.models';
import { AuthService } from '../../../../core/services/auth.service';
import { ExcelExportService } from '../../../../core/services/excel-export.service';
import { PharmacyService } from '../../../../core/services/pharmacy.service';
import { CODE_PATTERN, trimRequired } from '../../../../core/validators/app-validators';
import { HeaderComponent } from '../../../../shared/components/layout/header/header.component';
import { SidebarComponent } from '../../../../shared/components/layout/sidebar/sidebar.component';

@Component({
  selector: 'app-pharmacy-list',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    SidebarComponent,
    HeaderComponent,
    InputTextModule,
    InputTextareaModule,
    InputNumberModule,
    DropdownModule,
    CalendarModule,
    TableModule,
  ],
  templateUrl: './pharmacy-list.component.html',
  styleUrl: './pharmacy-list.component.scss',
})
export class PharmacyListComponent implements OnInit {
  medicines: Medicine[] = [];
  filteredMedicines: Medicine[] = [];
  isLoading = true;
  userRole: string | null = null;
  showAddForm = false;
  showEditForm = false;
  showRestockForm = false;
  editingMedicine: Medicine | null = null;
  restockingMedicine: Medicine | null = null;
  medicineForm!: FormGroup;
  restockForm!: FormGroup;
  isSubmitting = false;
  errorMessage = '';
  successMessage = '';
  showLowStockOnly = false;

  categories = Object.values(MedicineCategory);

  futureDateValidator(control: any) {
    if (!control.value) return null;
    const date = new Date(control.value);
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    return date > today ? null : { notFuture: true };
  }

  constructor(
    private pharmacyService: PharmacyService,
    private authService: AuthService,
    private fb: FormBuilder,
    private excelExportService: ExcelExportService,
  ) {}

  ngOnInit(): void {
    this.userRole = this.authService.getUserRole();
    this.initForm();
    this.loadMedicines();
  }

  initForm(): void {
    this.medicineForm = this.fb.group({
      name: ['', [...trimRequired(2, 200)]],
      medicineCode: ['', [Validators.required, Validators.maxLength(50), Validators.pattern(CODE_PATTERN)]],
      category: ['', Validators.required],
      manufacturer: ['', [...trimRequired(2, 100)]],
      description: ['', Validators.maxLength(500)],
      unitPrice: [null, [Validators.required, Validators.min(0.01)]],
      quantityInStock: [0, [Validators.required, Validators.min(0)]],
      reorderLevel: [10, [Validators.required, Validators.min(0)]],
      expiryDate: [null, [this.futureDateValidator]],
    });

    this.restockForm = this.fb.group({
      quantity: [1, [Validators.required, Validators.min(1)]],
    });
  }

  loadMedicines(): void {
    this.isLoading = true;
    this.pharmacyService.getAll().subscribe({
      next: (res: ApiResponse<Medicine[]>) => {
        this.medicines = res.data || [];
        this.applyFilter();
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      },
    });
  }

  applyFilter(): void {
    let list = this.medicines;
    if (this.showLowStockOnly) {
      list = list.filter((m) => m.quantityInStock <= m.reorderLevel);
    }
    this.filteredMedicines = list;
  }

  toggleLowStock(): void {
    this.showLowStockOnly = !this.showLowStockOnly;
    this.applyFilter();
  }

  openAddForm(): void {
    this.showAddForm = true;
    this.showEditForm = false;
    this.showRestockForm = false;
    this.editingMedicine = null;
    this.restockingMedicine = null;
    this.medicineForm.reset({ unitPrice: null, quantityInStock: 0, reorderLevel: 10 });
    this.errorMessage = '';
    this.successMessage = '';
  }

  openEditForm(med: Medicine): void {
    this.editingMedicine = med;
    this.showEditForm = true;
    this.showAddForm = false;
    this.showRestockForm = false;
    this.restockingMedicine = null;
    this.medicineForm.patchValue({
      name: med.name,
      medicineCode: med.medicineCode,
      category: med.category,
      manufacturer: med.manufacturer,
      description: med.description,
      unitPrice: med.unitPrice,
      quantityInStock: med.quantityInStock,
      reorderLevel: med.reorderLevel,
      expiryDate: med.expiryDate ? new Date(med.expiryDate) : null,
    });
    this.errorMessage = '';
    this.successMessage = '';
  }

  openRestockForm(med: Medicine): void {
    this.restockingMedicine = med;
    this.showRestockForm = true;
    this.showAddForm = false;
    this.showEditForm = false;
    this.editingMedicine = null;
    this.restockForm.reset({ quantity: 10 });
    this.errorMessage = '';
    this.successMessage = '';
  }

  closeForm(): void {
    this.showAddForm = false;
    this.showEditForm = false;
    this.showRestockForm = false;
    this.editingMedicine = null;
    this.restockingMedicine = null;
  }

  onSubmit(): void {
    if (this.medicineForm.invalid) {
      this.medicineForm.markAllAsTouched();
      return;
    }
    this.isSubmitting = true;
    const data = this.medicineForm.value;
    const payload = {
      ...data,
      expiryDate: this.formatDate(data.expiryDate),
    };

    if (this.showEditForm && this.editingMedicine) {
      this.pharmacyService.update(this.editingMedicine.id, payload).subscribe({
        next: () => {
          this.successMessage = 'Medicine updated successfully!';
          this.isSubmitting = false;
          this.closeForm();
          this.loadMedicines();
        },
        error: (err: HttpErrorResponse) => {
          this.errorMessage = err.error?.message || 'Update failed.';
          this.isSubmitting = false;
        },
      });
    } else {
      this.pharmacyService.create(payload).subscribe({
        next: () => {
          this.successMessage = 'Medicine added successfully!';
          this.isSubmitting = false;
          this.closeForm();
          this.loadMedicines();
        },
        error: (err: HttpErrorResponse) => {
          this.errorMessage = err.error?.message || 'Create failed.';
          this.isSubmitting = false;
        },
      });
    }
  }

  onRestockSubmit(): void {
    if (this.restockForm.invalid || !this.restockingMedicine) {
      this.restockForm.markAllAsTouched();
      return;
    }
    this.isSubmitting = true;
    const quantity = this.restockForm.value.quantity;

    this.pharmacyService.restock(this.restockingMedicine.id, quantity).subscribe({
      next: () => {
        this.successMessage = `Restocked ${this.restockingMedicine!.name} with ${quantity} units!`;
        this.isSubmitting = false;
        this.closeForm();
        this.loadMedicines();
      },
      error: (err: HttpErrorResponse) => {
        this.errorMessage = err.error?.message || 'Restock failed.';
        this.isSubmitting = false;
      },
    });
  }

  onDelete(id: number): void {
    if (!confirm('Delete this medicine?')) return;
    this.pharmacyService.delete(id).subscribe({
      next: () => {
        this.loadMedicines();
      },
      error: () => {},
    });
  }

  exportToExcel(): void {
    const dataToExport = this.filteredMedicines.map((med) => ({
      'Medicine Name': med.name,
      Code: med.medicineCode,
      Category: med.category,
      Manufacturer: med.manufacturer,
      'Stock Qty': med.quantityInStock,
      'Unit Price': med.unitPrice,
      'Reorder Level': med.reorderLevel,
      'Expiry Date': med.expiryDate ? new Date(med.expiryDate).toLocaleDateString() : 'N/A',
      Status:
        med.quantityInStock > med.reorderLevel ? 'In Stock' : med.quantityInStock > 0 ? 'Low Stock' : 'Out of Stock',
    }));

    this.excelExportService.exportAsExcelFile(dataToExport, 'Medicines_Inventory_Export');
  }

  isLowStock(med: Medicine): boolean {
    return med.quantityInStock <= med.reorderLevel;
  }

  categoryOptions(): Array<{ label: string; value: string }> {
    return this.categories.map((category) => ({ label: category, value: category }));
  }

  private formatDate(value: Date | string | null): string | null {
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
}
