import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

// pages réservées aux utilisateurs connectés
export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.connecte() ? true : router.createUrlTree(['/connexion']);
};

// pages de connexion et d'inscription : inutiles si on est déjà connecté
export const visiteurGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.connecte() ? router.createUrlTree(['/documents']) : true;
};