import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { BankTransaction } from '../models/transaction.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class TransactionService {
  private apiUrl = `${environment.apiUrl}/transactions`;

  constructor(private http: HttpClient) {}

  deposit(accountNumber: string, amount: number, description: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/deposit`, { accountNumber, amount, description });
  }

  withdraw(accountNumber: string, amount: number, description: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/withdraw`, { accountNumber, amount, description });
  }

  transfer(senderAccountNumber: string, receiverAccountNumber: string, amount: number, description: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/transfer`, {
      senderAccountNumber,
      receiverAccountNumber,
      amount,
      description
    });
  }

  getTransactionHistory(accountNumber: string): Observable<BankTransaction[]> {
    return this.http.get<BankTransaction[]>(`${this.apiUrl}/${accountNumber}`);
  }
}
