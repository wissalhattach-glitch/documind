import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../core/auth.service';
import { messageErreur } from '../core/erreurs';

@Component({
  selector: 'app-register',
  imports: [FormsModule, RouterLink],
  template: `
    <main class="auth">
      <form class="carte" (ngSubmit)="soumettre()">
        <h1>Créer un compte</h1>

        <label>
          Nom
          <input type="text" name="nom" [(ngModel)]="nom" autocomplete="name" required />
        </label>

        <label>
          Email
          <input type="email" name="email" [(ngModel)]="email" autocomplete="email" required />
        </label>

        <label>
          Mot de passe (8 caractères minimum)
          <input
            type="password"
            name="motDePasse"
            [(ngModel)]="motDePasse"
            autocomplete="new-password"
            required
          />
        </label>

        @if (erreur()) {
          <p class="erreur" role="alert">{{ erreur() }}</p>
        }

        <button type="submit" [disabled]="chargement()">
          {{ chargement() ? 'Création…' : 'Créer mon compte' }}
        </button>

        <p class="lien">Déjà un compte ? <a routerLink="/connexion">Se connecter</a></p>
      </form>
    </main>
  `,
})
export class RegisterPage {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly nom = signal('');
  readonly email = signal('');
  readonly motDePasse = signal('');
  readonly chargement = signal(false);
  readonly erreur = signal<string | null>(null);

  soumettre(): void {
    if (!this.nom().trim() || !this.email().trim() || !this.motDePasse()) {
      this.erreur.set('Tous les champs sont obligatoires');
      return;
    }
    if (this.motDePasse().length < 8) {
      this.erreur.set('Le mot de passe doit faire au moins 8 caractères');
      return;
    }
    this.chargement.set(true);
    this.erreur.set(null);

    this.auth.inscrire(this.nom().trim(), this.email().trim(), this.motDePasse()).subscribe({
      next: () => this.router.navigateByUrl('/documents'),
      error: (e: HttpErrorResponse) => {
        this.erreur.set(messageErreur(e));
        this.chargement.set(false);
      },
    });
  }
}