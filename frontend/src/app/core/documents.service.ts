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
}