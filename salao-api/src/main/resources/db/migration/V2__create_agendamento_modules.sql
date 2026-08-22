CREATE TABLE profissional (
   id UUID PRIMARY KEY,
   nome VARCHAR(255) NOT NULL,
   especialidade VARCHAR(255),
   ativo BOOLEAN NOT NULL DEFAULT TRUE,
   data_criacao TIMESTAMP NOT NULL
);

CREATE TABLE servico (
   id UUID PRIMARY KEY,
   nome VARCHAR(255) NOT NULL,
   descricao TEXT,
   preco DECIMAL(10, 2) NOT NULL,
   duracao_minutos INT NOT NULL,
   ativo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE agendamento (
   id UUID PRIMARY KEY,
   cliente_id UUID NOT NULL,
   profissional_id UUID NOT NULL,
   servico_id UUID NOT NULL,
   data_hora_inicio TIMESTAMP NOT NULL,
   data_hora_fim TIMESTAMP NOT NULL,
   status VARCHAR(50) NOT NULL,
   data_criacao TIMESTAMP NOT NULL,
   CONSTRAINT fk_agendamento_cliente FOREIGN KEY (cliente_id) REFERENCES cliente(id),
   CONSTRAINT fk_agendamento_profissional FOREIGN KEY (profissional_id) REFERENCES profissional(id),
   CONSTRAINT fk_agendamento_servico FOREIGN KEY (servico_id) REFERENCES servico(id)
);
