import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { API_URL } from '../config';
import { DocumentResponse, PageResponse } from './models';

@Injectable({ providedIn: 'root' })
export class DocumentsService {
  private readonly http = inject(HttpClient);

  lister() {
    return this.http.get<PageResponse<DocumentResponse>>(`${API_URL}/documents?size=50`);
  }

  importer(fichier: File) {
    const formulaire = new FormData();
    formulaire.append('fichier', fichier); // même nom de champ que côté Spring
    return this.http.post<DocumentResponse>(`${API_URL}/documents`, formulaire);
  }

  supprimer(id: number) {
    return this.http.delete<void>(`${API_URL}/documents/${id}`);
  }

  relancer(id: number) {
    return this.http.post<DocumentResponse>(`${API_URL}/documents/${id}/traitement`, {});
  }
}