import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8080/api/auth';

  constructor(private http: HttpClient, private router: Router) {}

  login(email: string, password: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/login`, { email, password }).pipe(
      tap(res => {
        if (res && res.token) {
          localStorage.setItem('auth_token', res.token);
          localStorage.setItem('customer_id', res.customerId.toString());
          localStorage.setItem('customer_name', res.fullName);
          localStorage.setItem('customer_email', res.email);
        }
      })
    );
  }

  logout(): void {
    const token = this.getToken();
    this.http.post(`${this.apiUrl}/logout`, {}).subscribe({
      next: () => this.clearSession(),
      error: () => this.clearSession()
    });
  }

  private clearSession(): void {
    localStorage.removeItem('auth_token');
    localStorage.removeItem('customer_id');
    localStorage.removeItem('customer_name');
    localStorage.removeItem('customer_email');
    this.router.navigate(['/login']);
  }

  isLoggedIn(): boolean {
    return !!localStorage.getItem('auth_token');
  }

  getToken(): string | null {
    return localStorage.getItem('auth_token');
  }

  getCustomerId(): number | null {
    const id = localStorage.getItem('customer_id');
    return id ? parseInt(id, 10) : null;
  }

  getCustomerName(): string | null {
    return localStorage.getItem('customer_name');
  }

  getCustomerEmail(): string | null {
    return localStorage.getItem('customer_email');
  }
}
