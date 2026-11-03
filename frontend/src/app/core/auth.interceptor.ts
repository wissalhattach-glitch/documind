import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { API_URL } from '../config';
import { AuthService } from './auth.service';

// ajoute "Authorization: Bearer <token>" aux appels vers notre API
export const authInterceptor: HttpInterceptorFn = (requete, suivant) => {
  const auth = inject(AuthService);
  const token = auth.tokenCourant();
  const versNotreApi = requete.url.startsWith(API_URL);

  const requeteFinale =
    token && versNotreApi
      ? requete.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
      : requete;

  return suivant(requeteFinale).pipe(
    catchError((erreur: HttpErrorResponse) => {
      // token refusé ou expiré (il dure 1 heure) : on se déconnecte
      if (erreur.status === 401 && token && versNotreApi) {
        auth.deconnecter();
      }
      return throwError(() => erreur);
    }),
  );
};