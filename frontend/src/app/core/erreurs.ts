import { HttpErrorResponse } from '@angular/common/http';

// transforme une erreur HTTP en message lisible (le message vient de l'API Spring)
export function messageErreur(erreur: HttpErrorResponse): string {
  if (erreur.status === 0) {
    return 'Impossible de joindre le serveur';
  }
  return erreur.error?.message ?? 'Une erreur est survenue';
}