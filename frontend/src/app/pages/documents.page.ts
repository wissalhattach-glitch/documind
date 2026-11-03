import { HttpErrorResponse } from '@angular/common/http';
import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { AuthService } from '../core/auth.service';
import { DocumentsService } from '../core/documents.service';
import { messageErreur } from '../core/erreurs';
import { DocumentResponse, StatutDocument } from '../core/models';

@Component({
  selector: 'app-documents',
  imports: [DatePipe],
  template: `
    <header class="barre">
      <h1>DocuMind AI</h1>
      <button type="button" class="secondaire" (click)="deconnecter()">Se déconnecter</button>
    </header>

    <main class="contenu">
      <h2>Mes documents</h2>

      @if (chargement()) {
        <p>Chargement…</p>
      } @else if (erreur()) {
        <p class="erreur" role="alert">{{ erreur() }}</p>
      } @else if (liste().length === 0) {
        <p>Aucun document pour le moment.</p>
      } @else {
        <ul class="documents">
          @for (doc of liste(); track doc.id) {
            <li>
              <span class="nom">{{ doc.nomFichier }}</span>
              <span class="meta">
                {{ taille(doc.tailleOctets) }} · {{ doc.nombrePages ?? '?' }} pages ·
                {{ doc.dateUpload | date: 'dd/MM/yyyy HH:mm' }}
              </span>
              <span class="statut statut-{{ doc.statut }}">{{ etiquette(doc.statut) }}</span>
            </li>
          }
        </ul>
      }
    </main>
  `,
})
export class DocumentsPage implements OnInit {
  private readonly documents = inject(DocumentsService);
  private readonly auth = inject(AuthService);

  readonly liste = signal<DocumentResponse[]>([]);
  readonly chargement = signal(true);
  readonly erreur = signal<string | null>(null);

  ngOnInit(): void {
    this.documents.lister().subscribe({
      next: (page) => {
        this.liste.set(page.contenu);
        this.chargement.set(false);
      },
      error: (e: HttpErrorResponse) => {
        this.erreur.set(messageErreur(e));
        this.chargement.set(false);
      },
    });
  }

  etiquette(statut: StatutDocument): string {
    const libelles: Record<StatutDocument, string> = {
      UPLOADED: 'En attente',
      PROCESSING: 'Analyse en cours…',
      READY: 'Prêt',
      FAILED: 'Échec',
    };
    return libelles[statut];
  }

  taille(octets: number): string {
    if (octets < 1024 * 1024) {
      return `${Math.max(1, Math.round(octets / 1024))} Ko`;
    }
    return `${(octets / (1024 * 1024)).toFixed(1)} Mo`;
  }

  deconnecter(): void {
    this.auth.deconnecter();
  }
}