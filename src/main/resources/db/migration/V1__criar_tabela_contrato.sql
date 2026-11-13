CREATE TABLE contrato (
    id                UUID           PRIMARY KEY,
    nome              VARCHAR(150)   NOT NULL,
    valor_contratado  NUMERIC(15, 2) NOT NULL CHECK (valor_contratado > 0),
    data_inicio       DATE           NOT NULL,
    data_fim          DATE           NOT NULL CHECK (data_fim > data_inicio)
);