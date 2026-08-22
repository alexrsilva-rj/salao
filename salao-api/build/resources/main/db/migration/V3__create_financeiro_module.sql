CREATE TABLE financeiro (
   id UUID PRIMARY KEY,
   agendamento_id UUID NOT NULL,
   profissional_id UUID NOT NULL,
   valor_total DECIMAL(10, 2) NOT NULL,
   percentual_comissao DECIMAL(5, 2) NOT NULL,
   valor_comissao DECIMAL(10, 2) NOT NULL,
   valor_liquido_salao DECIMAL(10, 2) NOT NULL,
   forma_pagamento VARCHAR(50) NOT NULL,
   status VARCHAR(50) NOT NULL,
   data_pagamento TIMESTAMP NOT NULL,
   CONSTRAINT fk_financeiro_agendamento FOREIGN KEY (agendamento_id) REFERENCES agendamento(id),
   CONSTRAINT fk_financeiro_profissional FOREIGN KEY (profissional_id) REFERENCES profissional(id)
);
