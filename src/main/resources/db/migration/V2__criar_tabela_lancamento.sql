CREATE TABLE lancamento (
    id              UUID           PRIMARY KEY,
    contrato_id     UUID           NOT NULL REFERENCES contrato (id),
    data            DATE           NOT NULL,
    valor           NUMERIC(15, 2) NOT NULL CHECK (valor > 0),
    status          VARCHAR(20)    NOT NULL CHECK (status IN ('ATIVO', 'ESTORNADO')),
    data_estorno    TIMESTAMP,
    motivo_estorno  VARCHAR(255),
    CONSTRAINT ck_estorno_consistente
        CHECK ((status = 'ESTORNADO') = (data_estorno IS NOT NULL))
);

CREATE INDEX idx_lancamento_contrato_id ON lancamento (contrato_id);