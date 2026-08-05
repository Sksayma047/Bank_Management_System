import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AccountService } from '../../services/account.service';
import { TransactionService } from '../../services/transaction.service';
import { Account } from '../../models/account.model';

@Component({
  selector: 'app-withdraw',
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
    MatIconModule,
    MatSnackBarModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './withdraw.component.html',
  styleUrls: ['./withdraw.component.css']
})
export class WithdrawComponent implements OnInit {
  withdrawForm!: FormGroup;
  accounts: Account[] = [];
  selectedAccount: Account | null = null;
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

    this.withdrawForm = this.fb.group({
      accountNumber: ['', Validators.required],
      amount: ['', [Validators.required, Validators.min(0.01)]],
      description: ['', Validators.maxLength(100)]
    });

    // Watch selected account to show limits
    this.withdrawForm.get('accountNumber')?.valueChanges.subscribe(num => {
      this.selectedAccount = this.accounts.find(a => a.accountNumber === num) || null;
    });
  }

  loadAccounts(): void {
    this.isLoadingAccounts = true;
    this.accountService.getAccounts().subscribe({
      next: (accs) => {
        this.accounts = accs.filter(a => a.status === 'ACTIVE');
        this.isLoadingAccounts = false;
        if (this.accounts.length > 0) {
          this.withdrawForm.patchValue({ accountNumber: this.accounts[0].accountNumber });
        }
      },
      error: () => {
        this.isLoadingAccounts = false;
        this.snackBar.open('Failed to load accounts for withdrawal.', 'Close', {
          duration: 3000,
          panelClass: ['error-snackbar']
        });
      }
    });
  }

  getAvailableLimit(): number {
    if (!this.selectedAccount) return 0;
    if (this.selectedAccount.accountType === 'SAVINGS') {
      return Math.max(0, this.selectedAccount.balance - 100.00); // Savings has minimum balance of $100
    } else {
      return this.selectedAccount.balance + 1000.00; // Current has overdraft of $1000
    }
  }

  onSubmit(): void {
    if (this.withdrawForm.invalid) {
      return;
    }

    const { accountNumber, amount, description } = this.withdrawForm.value;
    const limit = this.getAvailableLimit();

    if (amount > limit) {
      this.snackBar.open('Insufficient funds. Exceeds available limit.', 'Close', {
        duration: 4000,
        panelClass: ['error-snackbar']
      });
      return;
    }

    this.isSubmitting = true;
    this.transactionService.withdraw(accountNumber, amount, description).subscribe({
      next: (res) => {
        this.isSubmitting = false;
        this.snackBar.open(res.message || 'Withdrawal successful!', 'Close', {
          duration: 4000,
          panelClass: ['success-snackbar']
        });
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.isSubmitting = false;
        const msg = err.error?.message || 'Withdrawal failed. Please check inputs.';
        this.snackBar.open(msg, 'Close', {
          duration: 4000,
          panelClass: ['error-snackbar']
        });
      }
    });
  }
}
