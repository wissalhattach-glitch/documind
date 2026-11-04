import { Routes } from '@angular/router';
import { authGuard, visiteurGuard } from './core/auth.guard';
import { DocumentsPage } from './pages/documents.page';
import { LoginPage } from './pages/login.page';
import { RegisterPage } from './pages/register.page';
import { ChatPage } from './pages/chat.page';

export const routes: Routes = [
  { path: 'connexion', component: LoginPage, canActivate: [visiteurGuard] },
  { path: 'inscription', component: RegisterPage, canActivate: [visiteurGuard] },
  { path: 'documents', component: DocumentsPage, canActivate: [authGuard] },
  { path: 'chat', component: ChatPage, canActivate: [authGuard] },
  { path: '', pathMatch: 'full', redirectTo: 'documents' },
  { path: '**', redirectTo: 'documents' },
];