export interface Account {
  accountId?: number;
  accountNumber: string;
  customerId: number;
  accountType: 'SAVINGS' | 'CURRENT';
  balance: number;
  status: 'ACTIVE' | 'CLOSED' | 'BLOCKED';
  createdAt?: string;
  interestRate?: number;
}
