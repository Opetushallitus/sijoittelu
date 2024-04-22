CREATE TABLE IF NOT EXISTS jatkuvat (
    haku_oid        TEXT PRIMARY KEY,
    jatkuva_paalla  BOOLEAN,
    viimeksi_ajettu TIMESTAMP,
    aloitus         TIMESTAMP,
    ajotiheys       INTEGER
);

CREATE TABLE IF NOT EXISTS jatkuva_virheet (
    haku_oid        TEXT NOT NULL,
    aika            TIMESTAMP,
    virhe           TEXT,
    CONSTRAINT fk_haku_oid FOREIGN KEY (haku_oid) REFERENCES jatkuvat(haku_oid) ON DELETE CASCADE
);

DROP INDEX IF EXISTS jatkuva_virheet_haku_oid;

CREATE INDEX jatkuva_virheet_haku_oid ON jatkuva_virheet (haku_oid);
