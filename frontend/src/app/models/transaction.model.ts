export interface BankTransaction {
  transactionId?: number;
  accountId: number;
  transactionType: 'DEPOSIT' | 'WITHDRAW' | 'TRANSFER';
  amount: number;
  description: string;
  referenceNumber: string;
  transactionDate?: string;
  status: 'SUCCESS' | 'FAILED';
}
