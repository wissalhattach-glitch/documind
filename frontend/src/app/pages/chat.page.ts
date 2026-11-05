import { HttpErrorResponse } from '@angular/common/http';
import { Component, ElementRef, effect, inject, signal, viewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../core/auth.service';
import {
  ConversationDetail,
  ConversationResume,
  MessageResponse,
} from '../core/conversation-models';
import { ConversationsService } from '../core/conversations.service';
import { DocumentsService } from '../core/documents.service';
import { messageErreur } from '../core/erreurs';
import { MarkdownPipe } from '../core/markdown.pipe';
import { DocumentResponse } from '../core/models';

const MAX_DOCUMENTS = 20; // limite côté Spring

@Component({
  selector: 'app-chat',
  imports: [FormsModule, RouterLink, MarkdownPipe],
  template: `
    <div class="chat">
      <aside class="cote">
        <button (click)="nouvelle()"><i class="fa-solid fa-plus"></i> Nouvelle conversation</button>
        <ul class="liste-conv">
          @for (c of conversations(); track c.id) {
            <li [class.active]="c.id === courante()?.id" (click)="ouvrir(c.id)">
              @if (enEdition() === c.id) {
                <input
                  #champTitre
                  class="titre-input"
                  maxlength="200"
                  [(ngModel)]="titreEdite"
                  (click)="$event.stopPropagation()"
                  (keydown.enter)="valider(c)"
                  (keydown.escape)="annuler()"
                  (blur)="annuler()"
                />
              } @else {
                <span class="titre-conv">{{ c.titre || 'Nouvelle conversation' }}</span>
                <span class="conv-actions">
                  <button
                    type="button"
                    class="icone"
                    title="Renommer"
                    aria-label="Renommer"
                    (click)="commencer(c, $event)"
                  >
                    <i class="fa-solid fa-pen"></i>
                  </button>
                  <button
                    type="button"
                    class="icone supprimer"
                    title="Supprimer"
                    aria-label="Supprimer"
                    (click)="supprimerConv(c, $event)"
                  >
                    <i class="fa-solid fa-trash"></i>
                  </button>
                </span>
              }
            </li>
          } @empty {
            <li class="vide">Aucune conversation</li>
          }
        </ul>
        <a routerLink="/documents" class="bouton">
          <i class="fa-solid fa-arrow-left"></i> Mes documents
        </a>
        <button class="secondaire petit" (click)="auth.deconnecter()">
          <i class="fa-solid fa-right-from-bracket"></i> Déconnexion
        </button>
      </aside>

      <section class="principal">
        <div class="contexte">
          @if (courante(); as conv) {
            <span class="discret">
              <i class="fa-solid fa-file-pdf"></i>
              @if (conv.documents.length) {
                {{ noms(conv) }}
              } @else {
                Tous mes documents
              }
            </span>
          } @else {
            <span class="discret">
              <i class="fa-solid fa-file-pdf"></i> Documents à interroger (aucun coché = tous) :
            </span>
            <div class="choix-docs">
              @for (d of documentsPrets(); track d.id) {
                <label class="doc-choix">
                  <input
                    type="checkbox"
                    [checked]="selection().includes(d.id)"
                    (change)="basculer(d.id)"
                  />
                  {{ d.nomFichier }}
                </label>
              } @empty {
                <span class="discret">
                  Aucun document prêt. Importe un PDF dans « Mes documents ».
                </span>
              }
            </div>
          }
        </div>

        <div class="messages">
          @if (!courante() && !envoi()) {
            <p class="vide">Pose une question sur tes documents pour commencer.</p>
          }
          @for (m of messages(); track m.id) {
            <div class="msg" [class.moi]="m.role === 'USER'">
              @if (m.role === 'ASSISTANT') {
                <div class="bulle markdown" [innerHTML]="m.contenu | markdown"></div>
              } @else {
                <div class="bulle">{{ m.contenu }}</div>
              }
              @if (m.sources.length) {
                <details class="sources">
                  <summary>Sources ({{ m.sources.length }})</summary>
                  @for (s of m.sources; track $index) {
                    <p>
                      <strong>{{ s.nomDocument }}</strong>, page {{ s.numeroPage }}<br />
                      <em>{{ s.extrait }}</em>
                    </p>
                  }
                </details>
              }
            </div>
          }
          @if (envoi()) {
            <div class="msg"><div class="bulle">Recherche dans tes documents…</div></div>
          }
          <div #fin></div>
        </div>

        @if (erreur()) {
          <p class="erreur">{{ erreur() }}</p>
        }

        <form class="saisie" (submit)="envoyer($event)">
          <textarea
            name="question"
            rows="2"
            maxlength="1000"
            placeholder="Pose ta question…"
            [(ngModel)]="question"
            [disabled]="envoi()"
            (keydown.enter)="touche($event)"
          ></textarea>
          <button type="submit" [disabled]="envoi() || !question().trim()">
            <i class="fa-solid fa-paper-plane"></i> Envoyer
          </button>
        </form>
      </section>
    </div>
  `,
})
export class ChatPage {
  protected auth = inject(AuthService);
  private service = inject(ConversationsService);
  private documents = inject(DocumentsService);

  protected conversations = signal<ConversationResume[]>([]);
  protected courante = signal<ConversationDetail | null>(null);
  protected messages = signal<MessageResponse[]>([]);
  protected question = signal('');
  protected envoi = signal(false);
  protected erreur = signal('');

  // choix des documents pour une nouvelle conversation
  protected documentsPrets = signal<DocumentResponse[]>([]);
  protected selection = signal<number[]>([]);

  // renommage : id de la conversation en cours d'édition et texte saisi
  protected enEdition = signal<number | null>(null);
  protected titreEdite = signal('');

  private fin = viewChild<ElementRef<HTMLElement>>('fin');
  private champTitre = viewChild<ElementRef<HTMLInputElement>>('champTitre');
  private compteur = -1; // ids temporaires négatifs pour les messages ajoutés côté client

  constructor() {
    this.charger();
    this.chargerDocuments();
    // fait défiler vers le bas à chaque nouveau message
    effect(() => {
      this.messages();
      this.envoi();
      setTimeout(() => this.fin()?.nativeElement.scrollIntoView({ behavior: 'smooth' }));
    });
  }

  private charger() {
    this.service.lister().subscribe({
      next: (page) => this.conversations.set(page.contenu),
      error: (e: HttpErrorResponse) => this.erreur.set(messageErreur(e)),
    });
  }

  private chargerDocuments() {
    this.documents.lister().subscribe({
      next: (page) => this.documentsPrets.set(page.contenu.filter((d) => d.statut === 'READY')),
      error: (e: HttpErrorResponse) => this.erreur.set(messageErreur(e)),
    });
  }

  nouvelle() {
    this.courante.set(null);
    this.messages.set([]);
    this.selection.set([]);
    this.erreur.set('');
    this.chargerDocuments(); // un document a pu devenir « Prêt » entre-temps
  }

  ouvrir(id: number) {
    this.erreur.set('');
    this.service.detail(id).subscribe({
      next: (d) => {
        this.courante.set(d);
        this.messages.set(d.messages);
      },
      error: (e: HttpErrorResponse) => this.erreur.set(messageErreur(e)),
    });
  }

  // --- choix des documents ---

  basculer(id: number) {
    const actuelle = this.selection();
    if (actuelle.includes(id)) {
      this.selection.set(actuelle.filter((x) => x !== id));
    } else if (actuelle.length >= MAX_DOCUMENTS) {
      this.erreur.set(`${MAX_DOCUMENTS} documents maximum par conversation`);
    } else {
      this.erreur.set('');
      this.selection.set([...actuelle, id]);
    }
  }

  noms(conv: ConversationDetail): string {
    return conv.documents.map((d) => d.nomFichier ?? '').join(', ');
  }

  // --- renommer ---

  commencer(c: ConversationResume, event: Event) {
    event.stopPropagation(); // ne pas ouvrir la conversation
    this.titreEdite.set(c.titre ?? '');
    this.enEdition.set(c.id);
    setTimeout(() => {
      const champ = this.champTitre()?.nativeElement;
      champ?.focus();
      champ?.select();
    });
  }

  valider(c: ConversationResume) {
    const titre = this.titreEdite().trim();
    if (!titre || titre === c.titre) {
      this.annuler();
      return;
    }
    this.erreur.set('');
    this.service.renommer(c.id, titre).subscribe({
      next: (maj) => {
        this.conversations.update((l) => l.map((x) => (x.id === maj.id ? maj : x)));
        this.courante.update((d) => (d && d.id === maj.id ? { ...d, titre: maj.titre } : d));
        this.enEdition.set(null);
      },
      error: (e: HttpErrorResponse) => {
        this.erreur.set(messageErreur(e));
        this.enEdition.set(null);
      },
    });
  }

  annuler() {
    this.enEdition.set(null);
  }

  // --- supprimer ---

  supprimerConv(c: ConversationResume, event: Event) {
    event.stopPropagation();
    if (!confirm(`Supprimer la conversation « ${c.titre || 'Nouvelle conversation'} » ?`)) {
      return;
    }
    this.erreur.set('');
    this.service.supprimer(c.id).subscribe({
      next: () => {
        this.conversations.update((l) => l.filter((x) => x.id !== c.id));
        if (this.courante()?.id === c.id) {
          this.nouvelle();
        }
      },
      error: (e: HttpErrorResponse) => this.erreur.set(messageErreur(e)),
    });
  }

  // --- poser une question ---

  // Entrée envoie, Maj+Entrée fait un retour à la ligne
  touche(event: Event) {
    if (!(event as KeyboardEvent).shiftKey) {
      this.envoyer(event);
    }
  }

  envoyer(event: Event) {
    event.preventDefault();
    const texte = this.question().trim();
    if (!texte || this.envoi()) return;

    this.erreur.set('');
    this.envoi.set(true);
    this.question.set('');
    this.ajouter('USER', texte, []);

    const conv = this.courante();
    if (conv) {
      this.poser(conv.id, texte);
    } else {
      // pas de conversation ouverte : on en crée une avec les documents cochés (aucun = tous)
      this.service.creer(this.selection()).subscribe({
        next: (nouvelle) => {
          this.courante.set(nouvelle);
          this.poser(nouvelle.id, texte);
        },
        error: (e: HttpErrorResponse) => this.echec(e),
      });
    }
  }

  private poser(id: number, texte: string) {
    this.service.poserQuestion(id, texte).subscribe({
      next: (rep) => {
        this.ajouter('ASSISTANT', rep.reponse, rep.sources);
        this.envoi.set(false);
        this.charger(); // le titre est généré à la première question
      },
      error: (e: HttpErrorResponse) => this.echec(e),
    });
  }

  private echec(e: HttpErrorResponse) {
    this.erreur.set(messageErreur(e));
    this.envoi.set(false);
  }

  private ajouter(role: 'USER' | 'ASSISTANT', contenu: string, sources: MessageResponse['sources']) {
    const message: MessageResponse = {
      id: this.compteur--,
      role,
      contenu,
      dateCreation: new Date().toISOString(),
      sources,
    };
    this.messages.update((liste) => [...liste, message]);
  }
}