import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { EMPTY, catchError, filter, switchMap, timer } from 'rxjs';
import { AuthService } from '../core/auth.service';
import { DocumentsService } from '../core/documents.service';
import { messageErreur } from '../core/erreurs';
import { DocumentResponse, StatutDocument } from '../core/models';

const TAILLE_MAX = 50 * 1024 * 1024; // 50 Mo, comme côté Spring

@Component({
  selector: 'app-documents',
  imports: [DatePipe, RouterLink],
  template: `
    <header class="barre">
      <h1>DocuMind AI</h1>
      <a routerLink="/chat" class="bouton">Poser une question →</a>
      <button type="button" class="secondaire" (click)="deconnecter()">Se déconnecter</button>
    </header>

    <main class="contenu">
      <div class="entete">
        <h2>Mes documents</h2>

        <input
          #champFichier
          type="file"
          accept="application/pdf,.pdf"
          hidden
          (change)="choisir($event)"
        />
        <button type="button" [disabled]="envoiEnCours()" (click)="champFichier.click()">
          {{ envoiEnCours() ? 'Envoi…' : 'Importer un PDF' }}
        </button>
      </div>

      @if (erreur()) {
        <p class="erreur" role="alert">{{ erreur() }}</p>
      }

      @if (chargement()) {
        <p>Chargement…</p>
      } @else if (liste().length === 0) {
        <p>Aucun document pour le moment. Importe ton premier PDF.</p>
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

              @if (doc.statut === 'FAILED' && doc.messageErreur) {
                <span class="meta erreur">{{ doc.messageErreur }}</span>
              }

              <span class="actions">
                @if (doc.statut === 'FAILED') {
                  <button type="button" class="petit" (click)="relancer(doc)">
                    Relancer l'analyse
                  </button>
                }
                <button type="button" class="petit danger" (click)="supprimer(doc)">
                  Supprimer
                </button>
              </span>
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
  readonly envoiEnCours = signal(false);
  readonly erreur = signal<string | null>(null);

  constructor() {
    // toutes les 3 secondes, on rafraîchit la liste, mais seulement si un document n'est pas fini
    timer(3000, 3000)
      .pipe(
        filter(() => this.liste().some((d) => this.enCours(d))),
        switchMap(() => this.documents.lister().pipe(catchError(() => EMPTY))),
        takeUntilDestroyed(),
      )
      .subscribe((page) => this.liste.set(page.contenu));
  }

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

  choisir(evenement: Event): void {
    const champ = evenement.target as HTMLInputElement;
    const fichier = champ.files?.[0];
    champ.value = ''; // permet de choisir deux fois le même fichier
    if (!fichier) {
      return;
    }

    this.erreur.set(null);
    if (!fichier.name.toLowerCase().endsWith('.pdf')) {
      this.erreur.set('Seuls les fichiers PDF sont acceptés');
      return;
    }
    if (fichier.size > TAILLE_MAX) {
      this.erreur.set('Le fichier dépasse 50 Mo');
      return;
    }

    this.envoiEnCours.set(true);
    this.documents.importer(fichier).subscribe({
      next: (doc) => {
        this.liste.update((l) => [doc, ...l]);
        this.envoiEnCours.set(false);
      },
      error: (e: HttpErrorResponse) => {
        this.erreur.set(messageErreur(e));
        this.envoiEnCours.set(false);
      },
    });
  }

  supprimer(doc: DocumentResponse): void {
    if (!confirm(`Supprimer « ${doc.nomFichier} » ?`)) {
      return;
    }
    this.erreur.set(null);
    this.documents.supprimer(doc.id).subscribe({
      next: () => this.liste.update((l) => l.filter((d) => d.id !== doc.id)),
      error: (e: HttpErrorResponse) => this.erreur.set(messageErreur(e)),
    });
  }

  relancer(doc: DocumentResponse): void {
    this.erreur.set(null);
    this.documents.relancer(doc.id).subscribe({
      next: (maj) => this.liste.update((l) => l.map((d) => (d.id === maj.id ? maj : d))),
      error: (e: HttpErrorResponse) => this.erreur.set(messageErreur(e)),
    });
  }

  private enCours(doc: DocumentResponse): boolean {
    return doc.statut === 'UPLOADED' || doc.statut === 'PROCESSING';
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