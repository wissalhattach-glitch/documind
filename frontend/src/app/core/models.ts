export type StatutDocument = 'UPLOADED' | 'PROCESSING' | 'READY' | 'FAILED';

export interface DocumentResponse {
  id: number;
  nomFichier: string;
  tailleOctets: number;
  nombrePages: number | null;
  statut: StatutDocument;
  messageErreur: string | null;
  dateUpload: string;
}

export interface PageResponse<T> {
  contenu: T[];
  page: number;
  taille: number;
  totalElements: number;
  totalPages: number;
}