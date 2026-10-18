CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE users (
    id                BIGSERIAL PRIMARY KEY,
    nom               VARCHAR(100) NOT NULL,
    email             VARCHAR(255) NOT NULL UNIQUE,
    mot_de_passe_hash VARCHAR(255) NOT NULL,
    date_creation     TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE documents (
    id               BIGSERIAL PRIMARY KEY,
    nom_fichier      VARCHAR(255) NOT NULL,
    chemin_stockage  VARCHAR(500) NOT NULL,
    taille_octets    BIGINT NOT NULL,
    nombre_pages     INTEGER,
    statut           VARCHAR(20) NOT NULL
        CHECK (statut IN ('UPLOADED','PROCESSING','READY','FAILED')),
    message_erreur   TEXT,
    date_upload      TIMESTAMP NOT NULL DEFAULT now(),
    user_id          BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE
);
CREATE INDEX idx_documents_user ON documents(user_id);

CREATE TABLE document_chunks (
    id               BIGSERIAL PRIMARY KEY,
    contenu          TEXT NOT NULL,
    numero_page      INTEGER NOT NULL,
    position         INTEGER NOT NULL,
    embedding        vector(1024) NOT NULL,
    modele_embedding VARCHAR(100) NOT NULL,
    document_id      BIGINT NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    user_id          BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE
);
CREATE INDEX idx_chunks_document ON document_chunks(document_id);
CREATE INDEX idx_chunks_user ON document_chunks(user_id);
CREATE INDEX idx_chunks_embedding
    ON document_chunks USING hnsw (embedding vector_cosine_ops);

CREATE TABLE conversations (
    id            BIGSERIAL PRIMARY KEY,
    titre         VARCHAR(255) NOT NULL,
    date_creation TIMESTAMP NOT NULL DEFAULT now(),
    user_id       BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE
);
CREATE INDEX idx_conversations_user ON conversations(user_id);

CREATE TABLE conversation_documents (
    conversation_id BIGINT NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    document_id     BIGINT NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    PRIMARY KEY (conversation_id, document_id)
);

CREATE TABLE messages (
    id              BIGSERIAL PRIMARY KEY,
    role            VARCHAR(20) NOT NULL CHECK (role IN ('USER','ASSISTANT')),
    contenu         TEXT NOT NULL,
    date_creation   TIMESTAMP NOT NULL DEFAULT now(),
    conversation_id BIGINT NOT NULL REFERENCES conversations(id) ON DELETE CASCADE
);
CREATE INDEX idx_messages_conversation ON messages(conversation_id);

CREATE TABLE source_references (
    id               BIGSERIAL PRIMARY KEY,
    nom_document     VARCHAR(255) NOT NULL,
    numero_page      INTEGER NOT NULL,
    extrait          TEXT NOT NULL,
    score_similarite DOUBLE PRECISION NOT NULL,
    message_id       BIGINT NOT NULL REFERENCES messages(id) ON DELETE CASCADE,
    chunk_id         BIGINT REFERENCES document_chunks(id) ON DELETE SET NULL
);
CREATE INDEX idx_sources_message ON source_references(message_id);