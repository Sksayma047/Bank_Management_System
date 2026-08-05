import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AccountService } from '../../services/account.service';
import { TransactionService } from '../../services/transaction.service';
import { Account } from '../../models/account.model';

@Component({
  selector: 'app-deposit',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatSnackBarModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './deposit.component.html',
  styleUrls: ['./deposit.component.css']
})
export class DepositComponent implements OnInit {
  depositForm!: FormGroup;
  accounts: Account[] = [];
  isLoadingAccounts = false;
  isSubmitting = false;

  constructor(
    private fb: FormBuilder,
    private accountService: AccountService,
    private transactionService: TransactionService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadAccounts();

    this.depositForm = this.fb.group({
      accountNumber: ['', Validators.required],
      amount: ['', [Validators.required, Validators.min(0.01)]],
      description: ['', Validators.maxLength(100)]
    });
  }

  loadAccounts(): void {
    this.isLoadingAccounts = true;
    this.accountService.getAccounts().subscribe({
      next: (accs) => {
        // Only active accounts can receive deposits
        this.accounts = accs.filter(a => a.status === 'ACTIVE');
        this.isLoadingAccounts = false;
        if (this.accounts.length > 0) {
          this.depositForm.patchValue({ accountNumber: this.accounts[0].accountNumber });
        }
      },
      error: () => {
        this.isLoadingAccounts = false;
        this.snackBar.open('Failed to load accounts for deposit.', 'Close', {
          duration: 3000,
          panelClass: ['error-snackbar']
        });
      }
    });
  }

  onSubmit(): void {
    if (this.depositForm.invalid) {
      return;
    }

    this.isSubmitting = true;
    const { accountNumber, amount, description } = this.depositForm.value;

    this.transactionService.deposit(accountNumber, amount, description).subscribe({
      next: (res) => {
        this.isSubmitting = false;
        this.snackBar.open(res.message || 'Deposit successful!', 'Close', {
          duration: 4000,
          panelClass: ['success-snackbar']
        });
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.isSubmitting = false;
        const msg = err.error?.message || 'Deposit failed. Please try again.';
        this.snackBar.open(msg, 'Close', {
          duration: 4000,
          panelClass: ['error-snackbar']
        });
      }
    });
  }
}
