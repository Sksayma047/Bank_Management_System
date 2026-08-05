import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Customer } from '../models/customer.model';

@Injectable({
  providedIn: 'root'
})
export class CustomerService {
  private apiUrl = 'http://localhost:8080/api/customers';

  constructor(private http: HttpClient) {}

  register(customer: Customer, password: String): Observable<any> {
    return this.http.post<any>(this.apiUrl, {
      fullName: customer.fullName,
      email: customer.email,
      phone: customer.phone,
      address: customer.address,
      dateOfBirth: customer.dateOfBirth,
      password: password
    });
  }

  getProfile(): Observable<Customer> {
    return this.http.get<Customer>(this.apiUrl);
  }

  updateProfile(customer: Customer): Observable<Customer> {
    return this.http.put<Customer>(this.apiUrl, customer);
  }
}
