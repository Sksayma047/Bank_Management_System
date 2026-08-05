import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthService } from '../../services/auth.service';
import { AccountService } from '../../services/account.service';
import { TransactionService } from '../../services/transaction.service';
import { Account } from '../../models/account.model';
import { BankTransaction } from '../../models/transaction.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatTableModule,
    MatSelectModule,
    MatInputModule,
    MatFormFieldModule,
    MatSnackBarModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  customerName = '';
  accounts: Account[] = [];
  recentTransactions: BankTransaction[] = [];
  
  isLoadingAccounts = false;
  isLoadingTransactions = false;
  isOpeningAccount = false;
  
  openAccountForm!: FormGroup;
  displayedColumns: string[] = ['ref', 'type', 'amount', 'desc', 'date', 'status'];

  constructor(
    private authService: AuthService,
    private accountService: AccountService,
    private transactionService: TransactionService,
    private fb: FormBuilder,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.customerName = this.authService.getCustomerName() || 'Customer';
    this.loadAccounts();

    this.openAccountForm = this.fb.group({
      accountType: ['SAVINGS', Validators.required],
      initialDeposit: [100.00, [Validators.required, Validators.min(0)]]
    });

    // Dynamically adjust minimum deposit validation depending on account type
    this.openAccountForm.get('accountType')?.valueChanges.subscribe(type => {
      const depositControl = this.openAccountForm.get('initialDeposit');
      if (type === 'SAVINGS') {
        depositControl?.setValidators([Validators.required, Validators.min(100.00)]);
      } else {
        depositControl?.setValidators([Validators.required, Validators.min(0)]);
      }
      depositControl?.updateValueAndValidity();
    });
  }

  loadAccounts(): void {
    this.isLoadingAccounts = true;
    this.accountService.getAccounts().subscribe({
      next: (accs) => {
        this.accounts = accs;
        this.isLoadingAccounts = false;
        
        // If there are accounts, load recent transactions for the first active account
        if (accs.length > 0) {
          this.loadRecentTransactions(accs[0].accountNumber);
        }
      },
      error: (err) => {
        this.isLoadingAccounts = false;
        this.snackBar.open('Failed to load accounts.', 'Close', {
          duration: 3000,
          panelClass: ['error-snackbar']
        });
      }
    });
  }

  loadRecentTransactions(accountNumber: string): void {
    this.isLoadingTransactions = true;
    this.transactionService.getTransactionHistory(accountNumber).subscribe({
      next: (txs) => {
        // Show only the 5 most recent transactions
        this.recentTransactions = txs.slice(0, 5);
        this.isLoadingTransactions = false;
      },
      error: () => {
        this.isLoadingTransactions = false;
      }
    });
  }

  handleOpenAccount(): void {
    if (this.openAccountForm.invalid) {
      return;
    }

    this.isOpeningAccount = true;
    const { accountType, initialDeposit } = this.openAccountForm.value;

    this.accountService.openAccount(accountType, initialDeposit).subscribe({
      next: (newAcc) => {
        this.isOpeningAccount = false;
        this.snackBar.open(`Account ${newAcc.accountNumber} opened successfully!`, 'Close', {
          duration: 4000,
          panelClass: ['success-snackbar']
        });
        this.openAccountForm.patchValue({ initialDeposit: accountType === 'SAVINGS' ? 100 : 0 });
        this.loadAccounts();
      },
      error: (err) => {
        this.isOpeningAccount = false;
        const msg = err.error?.message || 'Failed to open account. Check minimum deposit rules.';
        this.snackBar.open(msg, 'Close', {
          duration: 4000,
          panelClass: ['error-snackbar']
        });
      }
    });
  }
}
