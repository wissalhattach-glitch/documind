import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../core/auth.service';
import { messageErreur } from '../core/erreurs';

@Component({
  selector: 'app-login',
  imports: [FormsModule, RouterLink],
  template: `
    <main class="auth">
      <form class="carte" (ngSubmit)="soumettre()">
        <h1>Connexion</h1>

        <label>
          Email
          <input type="email" name="email" [(ngModel)]="email" autocomplete="email" required />
        </label>

        <label>
          Mot de passe
          <input
            type="password"
            name="motDePasse"
            [(ngModel)]="motDePasse"
            autocomplete="current-password"
            required
          />
        </label>

        @if (erreur()) {
          <p class="erreur" role="alert">{{ erreur() }}</p>
        }

        <button type="submit" [disabled]="chargement()">
          {{ chargement() ? 'Connexion…' : 'Se connecter' }}
        </button>

        <p class="lien">Pas encore de compte ? <a routerLink="/inscription">Créer un compte</a></p>
      </form>
    </main>
  `,
})
export class LoginPage {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly email = signal('');
  readonly motDePasse = signal('');
  readonly chargement = signal(false);
  readonly erreur = signal<string | null>(null);

  soumettre(): void {
    if (!this.email().trim() || !this.motDePasse()) {
      this.erreur.set('Email et mot de passe obligatoires');
      return;
    }
    this.chargement.set(true);
    this.erreur.set(null);

    this.auth.connecter(this.email().trim(), this.motDePasse()).subscribe({
      next: () => this.router.navigateByUrl('/documents'),
      error: (e: HttpErrorResponse) => {
        this.erreur.set(messageErreur(e));
        this.chargement.set(false);
      },
    });
  }
}