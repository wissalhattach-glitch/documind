export interface ConversationResume {
  id: number;
  titre: string;
  dateCreation: string;
}

// ajusté à l'étape 2 quand tu m'auras montré DocumentResume.java
export interface DocumentResume {
  id: number;
  nomFichier?: string;
}

export interface SourceHistorique {
  nomDocument: string;
  numeroPage: number;
  extrait: string;
  score: number;
}

export interface MessageResponse {
  id: number;
  role: 'USER' | 'ASSISTANT';
  contenu: string;
  dateCreation: string;
  sources: SourceHistorique[];
}

export interface ConversationDetail {
  id: number;
  titre: string;
  dateCreation: string;
  documents: DocumentResume[];
  messages: MessageResponse[];
}

export interface SourceReponse {
  chunkId: number;
  documentId: number;
  nomDocument: string;
  numeroPage: number;
  extrait: string;
  score: number;
}

export interface ReponseRag {
  reponse: string;
  trouve: boolean;
  sources: SourceReponse[];
}