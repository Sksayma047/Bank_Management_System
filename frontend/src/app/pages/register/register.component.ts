import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { CustomerService } from '../../services/customer.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSnackBarModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent implements OnInit {
  registerForm!: FormGroup;
  isLoading = false;

  constructor(
    private fb: FormBuilder,
    private customerService: CustomerService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.registerForm = this.fb.group({
      fullName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      phone: ['', [Validators.required, Validators.pattern('^[0-9+\\-\\s]{10,15}$')]],
      address: ['', Validators.required],
      dateOfBirth: ['', Validators.required],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  onSubmit(): void {
    if (this.registerForm.invalid) {
      return;
    }

    this.isLoading = true;
    const { fullName, email, phone, address, dateOfBirth, password } = this.registerForm.value;

    const customer = { fullName, email, phone, address, dateOfBirth };

    this.customerService.register(customer, password).subscribe({
      next: () => {
        this.isLoading = false;
        this.snackBar.open('Registration successful! Please login.', 'Close', {
          duration: 4000,
          panelClass: ['success-snackbar']
        });
        this.router.navigate(['/login']);
      },
      error: (err) => {
        this.isLoading = false;
        const errMsg = err.error?.message || 'Registration failed. Please check your entries.';
        this.snackBar.open(errMsg, 'Close', {
          duration: 4000,
          panelClass: ['error-snackbar']
        });
      }
    });
  }
}
