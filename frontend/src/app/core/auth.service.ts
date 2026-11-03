import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { switchMap, tap } from 'rxjs';
import { API_URL } from '../config';

const CLE_TOKEN = 'documind_token';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  private readonly token = signal<string | null>(this.lireToken());
  readonly connecte = computed(() => this.token() !== null);

  tokenCourant(): string | null {
    return this.token();
  }

  connecter(email: string, motDePasse: string) {
    return this.http
      .post<{ token: string }>(`${API_URL}/auth/login`, { email, motDePasse })
      .pipe(tap((reponse) => this.enregistrer(reponse.token)));
  }

  // crée le compte puis connecte directement
  inscrire(nom: string, email: string, motDePasse: string) {
    return this.http
      .post(`${API_URL}/auth/register`, { nom, email, motDePasse })
      .pipe(switchMap(() => this.connecter(email, motDePasse)));
  }

  deconnecter(): void {
    this.token.set(null);
    try {
      localStorage.removeItem(CLE_TOKEN);
    } catch {
      // stockage indisponible : on ignore
    }
    this.router.navigateByUrl('/connexion');
  }

  private enregistrer(token: string): void {
    this.token.set(token);
    try {
      localStorage.setItem(CLE_TOKEN, token);
    } catch {
      // stockage indisponible : la connexion durera jusqu'au rechargement
    }
  }

  private lireToken(): string | null {
    try {
      return localStorage.getItem(CLE_TOKEN);
    } catch {
      return null;
    }
  }
}