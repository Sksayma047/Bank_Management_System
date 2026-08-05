import { Routes } from '@angular/router';
import { LoginComponent } from './pages/login/login.component';
import { RegisterComponent } from './pages/register/register.component';
import { DashboardComponent } from './pages/dashboard/dashboard.component';
import { DepositComponent } from './pages/deposit/deposit.component';
import { WithdrawComponent } from './pages/withdraw/withdraw.component';
import { TransferComponent } from './pages/transfer/transfer.component';
import { TransactionsComponent } from './pages/transactions/transactions.component';
import { ProfileComponent } from './pages/profile/profile.component';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'dashboard', component: DashboardComponent, canActivate: [authGuard] },
  { path: 'deposit', component: DepositComponent, canActivate: [authGuard] },
  { path: 'withdraw', component: WithdrawComponent, canActivate: [authGuard] },
  { path: 'transfer', component: TransferComponent, canActivate: [authGuard] },
  { path: 'transactions', component: TransactionsComponent, canActivate: [authGuard] },
  { path: 'profile', component: ProfileComponent, canActivate: [authGuard] },
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  { path: '**', redirectTo: '/dashboard' }
];
