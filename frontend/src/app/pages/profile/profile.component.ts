import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { CustomerService } from '../../services/customer.service';
import { Customer } from '../../models/customer.model';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {
  profileForm!: FormGroup;
  customer!: Customer;
  
  isLoading = false;
  isSaving = false;
  isEditMode = false;

  constructor(
    private fb: FormBuilder,
    private customerService: CustomerService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadProfile();

    this.profileForm = this.fb.group({
      fullName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      phone: ['', [Validators.required, Validators.pattern('^[0-9+\\-\\s]{10,15}$')]],
      address: ['', Validators.required]
    });

    this.profileForm.disable(); // Initially disabled
  }

  loadProfile(): void {
    this.isLoading = true;
    this.customerService.getProfile().subscribe({
      next: (data) => {
        this.customer = data;
        this.profileForm.patchValue({
          fullName: data.fullName,
          email: data.email,
          phone: data.phone,
          address: data.address
        });
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
        this.snackBar.open('Failed to load profile details.', 'Close', {
          duration: 3000,
          panelClass: ['error-snackbar']
        });
      }
    });
  }

  toggleEditMode(): void {
    this.isEditMode = !this.isEditMode;
    if (this.isEditMode) {
      this.profileForm.enable();
    } else {
      this.profileForm.disable();
      // Revert values
      this.profileForm.patchValue({
        fullName: this.customer.fullName,
        email: this.customer.email,
        phone: this.customer.phone,
        address: this.customer.address
      });
    }
  }

  onSubmit(): void {
    if (this.profileForm.invalid) {
      return;
    }

    this.isSaving = true;
    const updatedCustomer: Customer = {
      ...this.customer,
      ...this.profileForm.value
    };

    this.customerService.updateProfile(updatedCustomer).subscribe({
      next: (saved) => {
        this.customer = saved;
        this.isSaving = false;
        this.isEditMode = false;
        this.profileForm.disable();
        
        // Update customer details in localStorage cache as well
        localStorage.setItem('customer_name', saved.fullName);
        localStorage.setItem('customer_email', saved.email);

        this.snackBar.open('Profile updated successfully!', 'Close', {
          duration: 4000,
          panelClass: ['success-snackbar']
        });
      },
      error: (err) => {
        this.isSaving = false;
        const msg = err.error?.message || 'Failed to update profile. Email or Phone might be taken.';
        this.snackBar.open(msg, 'Close', {
          duration: 4000,
          panelClass: ['error-snackbar']
        });
      }
    });
  }
}
