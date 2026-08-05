import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AccountService } from '../../services/account.service';
import { TransactionService } from '../../services/transaction.service';
import { Account } from '../../models/account.model';
import { BankTransaction } from '../../models/transaction.model';

@Component({
  selector: 'app-transactions',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './transactions.component.html',
  styleUrls: ['./transactions.component.css']
})
export class TransactionsComponent implements OnInit {
  accounts: Account[] = [];
  selectedAccountNumber = '';
  
  isLoadingAccounts = false;
  isLoadingTransactions = false;

  allTransactions: BankTransaction[] = [];
  dataSource = new MatTableDataSource<BankTransaction>([]);
  displayedColumns: string[] = ['transactionId', 'referenceNumber', 'transactionType', 'amount', 'description', 'transactionDate', 'status'];

  filterType = 'ALL';
  searchText = '';

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private accountService: AccountService,
    private transactionService: TransactionService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadAccounts();
  }

  loadAccounts(): void {
    this.isLoadingAccounts = true;
    this.accountService.getAccounts().subscribe({
      next: (accs) => {
        this.accounts = accs;
        this.isLoadingAccounts = false;
        if (this.accounts.length > 0) {
          this.selectedAccountNumber = this.accounts[0].accountNumber;
          this.loadTransactions();
        }
      },
      error: () => {
        this.isLoadingAccounts = false;
        this.snackBar.open('Failed to load accounts.', 'Close', {
          duration: 3000,
          panelClass: ['error-snackbar']
        });
      }
    });
  }

  loadTransactions(): void {
    if (!this.selectedAccountNumber) return;
    
    this.isLoadingTransactions = true;
    this.transactionService.getTransactionHistory(this.selectedAccountNumber).subscribe({
      next: (txs) => {
        this.allTransactions = txs;
        this.applyFilters();
        this.isLoadingTransactions = false;
      },
      error: () => {
        this.isLoadingTransactions = false;
        this.snackBar.open('Failed to load transactions for selected account.', 'Close', {
          duration: 3000,
          panelClass: ['error-snackbar']
        });
      }
    });
  }

  onAccountChange(): void {
    this.loadTransactions();
  }

  applyFilters(): void {
    let filtered = this.allTransactions;

    // Filter by type
    if (this.filterType !== 'ALL') {
      filtered = filtered.filter(tx => tx.transactionType.toUpperCase() === this.filterType);
    }

    // Filter by search text (description or reference number)
    if (this.searchText && this.searchText.trim() !== '') {
      const query = this.searchText.toLowerCase().trim();
      filtered = filtered.filter(tx => 
        (tx.description && tx.description.toLowerCase().includes(query)) ||
        (tx.referenceNumber && tx.referenceNumber.toLowerCase().includes(query))
      );
    }

    this.dataSource.data = filtered;
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
  }

  resetFilters(): void {
    this.filterType = 'ALL';
    this.searchText = '';
    this.applyFilters();
  }
}
