import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { CalendarModule } from 'primeng/calendar';
import { DropdownModule } from 'primeng/dropdown';
import { InputNumberModule } from 'primeng/inputnumber';
import { InputTextModule } from 'primeng/inputtext';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { TableModule } from 'primeng/table';
import { Subject, debounceTime, distinctUntilChanged } from 'rxjs';
import { HeaderComponent } from '../../../../layout/header/header.component';
import { SidebarComponent } from '../../../../layout/sidebar/sidebar.component';
import { StatusModalService } from '../../../../shared/services/status-modal.service';
import { AuthService } from '../../../auth/services/auth.service';
import { Medicine, MedicineCategory } from '../../models/pharmacy.models';
import { PharmacyService } from '../../services/pharmacy.service';
import { createMedicineForm, createRestockForm } from '../../utils/pharmacy-list-form';
import {
  buildCategoryOptions,
  filterMedicinesByLowStock,
  formatMedicineDate,
  isMedicineLowStock,
} from '../../utils/pharmacy-list.utils';

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

  // Search & Pagination
  searchQuery = '';
  searchSubject = new Subject<string>();
  currentPage = 0;
  pageSize = 20;
  isFirstPage = true;
  hasNextPage = false;
  openMenuId: number | null = null;

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
  minExpiryDate: Date = new Date();

  categories = Object.values(MedicineCategory);

  constructor(
    private pharmacyService: PharmacyService,
    private authService: AuthService,
    private fb: FormBuilder,
    private statusModalService: StatusModalService,
  ) {
    this.searchSubject.pipe(debounceTime(400), distinctUntilChanged()).subscribe((query) => {
      this.searchQuery = query;
      this.loadMedicines(0);
    });
  }

  ngOnInit(): void {
    this.userRole = this.authService.getUserRole();
    this.initForm();
    this.calculateMinDate();
    this.loadMedicines(0);
  }

  calculateMinDate(): void {
    const today = new Date();
    this.minExpiryDate = new Date(today.getFullYear(), today.getMonth(), today.getDate() + 1);
  }

  initForm(): void {
    this.medicineForm = createMedicineForm(this.fb);
    this.restockForm = createRestockForm(this.fb);
  }

  onSearch(event: Event): void {
    const query = (event.target as HTMLInputElement).value;
    this.searchSubject.next(query);
  }

  loadMedicines(page = 0): void {
    if (this.showLowStockOnly) {
      this.loadLowStockMedicines();
      return;
    }

    this.isLoading = true;
    this.currentPage = Math.max(page, 0);

    this.pharmacyService.getSlice(this.currentPage, this.pageSize, this.searchQuery).subscribe({
      next: (res) => {
        if (res.data) {
          this.medicines = res.data.content;
          this.isFirstPage = res.data.first;
          this.hasNextPage = res.data.hasNext;
          this.applyFilter();
        }
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      },
    });
  }

  private loadLowStockMedicines(): void {
    this.isLoading = true;
    this.currentPage = 0;
    this.isFirstPage = true;
    this.hasNextPage = false;

    this.pharmacyService.getAll().subscribe({
      next: (res) => {
        const allMedicines = res.data ?? [];
        const lowStockMedicines = allMedicines.filter((medicine) => isMedicineLowStock(medicine));
        const query = this.searchQuery.trim().toLowerCase();

        this.medicines = query
          ? lowStockMedicines.filter((medicine) =>
              [medicine.name, medicine.medicineCode, medicine.manufacturer, medicine.category]
                .filter((value): value is string => !!value)
                .some((value) => value.toLowerCase().includes(query)),
            )
          : lowStockMedicines;

        this.applyFilter();
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      },
    });
  }

  previousPage(): void {
    if (this.isLoading || this.isFirstPage) return;
    this.loadMedicines(this.currentPage - 1);
  }

  nextPage(): void {
    if (this.isLoading || !this.hasNextPage) return;
    this.loadMedicines(this.currentPage + 1);
  }

  applyFilter(): void {
    this.filteredMedicines = filterMedicinesByLowStock(this.medicines, this.showLowStockOnly);
  }

  toggleLowStock(): void {
    this.showLowStockOnly = !this.showLowStockOnly;
    this.loadMedicines(0);
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
    this.openMenuId = null;
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
    this.openMenuId = null;
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
      expiryDate: formatMedicineDate(data.expiryDate),
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
    const { quantity } = this.restockForm.value;

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

  async onDelete(id: number): Promise<void> {
    this.openMenuId = null;
    const confirmed = await this.statusModalService.confirm('Delete Medicine', 'Delete this medicine?', 'Delete');
    if (!confirmed) return;

    this.pharmacyService.delete(id).subscribe({
      next: () => {
        this.statusModalService.showSuccess('Medicine Deleted', 'The medicine was removed from inventory.');
        this.loadMedicines();
      },
      error: (err: HttpErrorResponse) =>
        this.statusModalService.showError('Delete Failed', err.error?.message || 'Could not delete this medicine.'),
    });
  }

  toggleMenu(id: number): void {
    this.openMenuId = this.openMenuId === id ? null : id;
  }

  isLowStock(med: Medicine): boolean {
    return isMedicineLowStock(med);
  }

  categoryOptions(): Array<{ label: string; value: string }> {
    return buildCategoryOptions(this.categories);
  }
}
