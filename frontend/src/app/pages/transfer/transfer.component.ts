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
  selector: 'app-transfer',
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
  templateUrl: './transfer.component.html',
  styleUrls: ['./transfer.component.css']
})
export class TransferComponent implements OnInit {
  transferForm!: FormGroup;
  accounts: Account[] = [];
  selectedSenderAccount: Account | null = null;
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

    this.transferForm = this.fb.group({
      senderAccountNumber: ['', Validators.required],
      receiverAccountNumber: ['', [Validators.required, Validators.pattern('^(SAV|CUR)-[0-9]{10}$')]],
      amount: ['', [Validators.required, Validators.min(0.01)]],
      description: ['', Validators.maxLength(100)]
    });

    // Watch sender account to show limits
    this.transferForm.get('senderAccountNumber')?.valueChanges.subscribe(num => {
      this.selectedSenderAccount = this.accounts.find(a => a.accountNumber === num) || null;
    });
  }

  loadAccounts(): void {
    this.isLoadingAccounts = true;
    this.accountService.getAccounts().subscribe({
      next: (accs) => {
        this.accounts = accs.filter(a => a.status === 'ACTIVE');
        this.isLoadingAccounts = false;
        if (this.accounts.length > 0) {
          this.transferForm.patchValue({ senderAccountNumber: this.accounts[0].accountNumber });
        }
      },
      error: () => {
        this.isLoadingAccounts = false;
        this.snackBar.open('Failed to load accounts for transfer.', 'Close', {
          duration: 3000,
          panelClass: ['error-snackbar']
        });
      }
    });
  }

  getAvailableLimit(): number {
    if (!this.selectedSenderAccount) return 0;
    if (this.selectedSenderAccount.accountType === 'SAVINGS') {
      return Math.max(0, this.selectedSenderAccount.balance - 100.00); // Savings has minimum balance of $100
    } else {
      return this.selectedSenderAccount.balance + 1000.00; // Current has overdraft of $1000
    }
  }

  onSubmit(): void {
    if (this.transferForm.invalid) {
      return;
    }

    const { senderAccountNumber, receiverAccountNumber, amount, description } = this.transferForm.value;

    if (senderAccountNumber === receiverAccountNumber) {
      this.snackBar.open('Sender and receiver accounts cannot be the same.', 'Close', {
        duration: 4000,
        panelClass: ['error-snackbar']
      });
      return;
    }

    const limit = this.getAvailableLimit();
    if (amount > limit) {
      this.snackBar.open('Insufficient funds. Exceeds available limit.', 'Close', {
        duration: 4000,
        panelClass: ['error-snackbar']
      });
      return;
    }

    this.isSubmitting = true;
    this.transactionService.transfer(senderAccountNumber, receiverAccountNumber, amount, description).subscribe({
      next: (res) => {
        this.isSubmitting = false;
        this.snackBar.open(res.message || 'Transfer completed successfully!', 'Close', {
          duration: 4000,
          panelClass: ['success-snackbar']
        });
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.isSubmitting = false;
        const msg = err.error?.message || 'Transfer failed. Check recipient account number.';
        this.snackBar.open(msg, 'Close', {
          duration: 4000,
          panelClass: ['error-snackbar']
        });
      }
    });
  }
}
