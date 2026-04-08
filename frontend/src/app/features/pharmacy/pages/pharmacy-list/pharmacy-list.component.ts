import { AuthService } from '../../../auth/services/auth.service';
import { CalendarModule } from 'primeng/calendar';
import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { DropdownModule } from 'primeng/dropdown';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { HeaderComponent } from '../../../../layout/header/header.component';
import { HttpErrorResponse } from '@angular/common/http';
import { InputNumberModule } from 'primeng/inputnumber';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { InputTextModule } from 'primeng/inputtext';
import { Medicine, MedicineCategory } from '../../models/pharmacy.models';
import { PharmacyService } from '../../services/pharmacy.service';
import { SidebarComponent } from '../../../../layout/sidebar/sidebar.component';
import { TableModule } from 'primeng/table';
import { Subject, debounceTime, distinctUntilChanged } from 'rxjs';
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
  isLastPage = false;
  isMoreLoading = false;

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
  ) {
    this.searchSubject.pipe(
      debounceTime(400),
      distinctUntilChanged()
    ).subscribe(query => {
      this.searchQuery = query;
      this.loadMedicines();
    });
  }

  ngOnInit(): void {
    this.userRole = this.authService.getUserRole();
    this.initForm();
    this.calculateMinDate();
    this.loadMedicines();
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

  loadMedicines(isLoadMore = false): void {
    if (isLoadMore) {
      this.isMoreLoading = true;
      this.currentPage++;
    } else {
      this.isLoading = true;
      this.currentPage = 0;
      this.medicines = [];
    }

    this.pharmacyService.getSlice(this.currentPage, this.pageSize, this.searchQuery).subscribe({
      next: (res) => {
        if (res.data) {
          const newMeds = res.data.content;
          this.medicines = isLoadMore ? [...this.medicines, ...newMeds] : newMeds;
          this.isLastPage = res.data.last;
          this.applyFilter();
        }
        this.isLoading = false;
        this.isMoreLoading = false;
      },
      error: () => {
        this.isLoading = false;
        this.isMoreLoading = false;
      },
    });
  }

  applyFilter(): void {
    this.filteredMedicines = filterMedicinesByLowStock(this.medicines, this.showLowStockOnly);
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

  isLowStock(med: Medicine): boolean {
    return isMedicineLowStock(med);
  }

  categoryOptions(): Array<{ label: string; value: string }> {
    return buildCategoryOptions(this.categories);
  }
}












