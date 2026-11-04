import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../config';
import { PageResponse } from './models';
import {
  ConversationDetail,
  ConversationResume,
  ReponseRag,
} from './conversation-models';

@Injectable({ providedIn: 'root' })
export class ConversationsService {
  private http = inject(HttpClient);
  private base = `${API_URL}/conversations`;

  lister(): Observable<PageResponse<ConversationResume>> {
    return this.http.get<PageResponse<ConversationResume>>(`${this.base}?size=50`);
  }

  detail(id: number): Observable<ConversationDetail> {
    return this.http.get<ConversationDetail>(`${this.base}/${id}`);
  }

  // documentIds vide = on interroge tous les documents de l'utilisateur
  creer(documentIds: number[] = [], titre?: string): Observable<ConversationDetail> {
    return this.http.post<ConversationDetail>(this.base, { titre: titre ?? null, documentIds });
  }

  renommer(id: number, titre: string): Observable<ConversationResume> {
    return this.http.patch<ConversationResume>(`${this.base}/${id}`, { titre });
  }

  supprimer(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  poserQuestion(id: number, question: string): Observable<ReponseRag> {
    return this.http.post<ReponseRag>(`${this.base}/${id}/messages`, { question });
  }
}