---
name: add-migration
description: >-
  Use this skill when the user asks to create a new Flyway database migration
  for the salao project. Examples: "crie uma migration para adicionar o campo X",
  "adicione a tabela Y no banco", "crie a migration de schema para o módulo Z".
---

# Skill: Criar Migration Flyway

## Passos

### 1. Descobrir o próximo número de versão

```bash
ls salao-api/src/main/resources/db/migration/ | sort -V | tail -1
```

A próxima migration é `V{último+1}`.

### 2. Criar o arquivo de migration

Caminho: `salao-api/src/main/resources/db/migration/V{n}__{verbo}_{substantivo}.sql`

Template obrigatório de cabeçalho:

```sql
-- ─────────────────────────────────────────────────────────────────────────────
-- V{n} — {Descrição do propósito}
-- ─────────────────────────────────────────────────────────────────────────────
```

### 3. Regras de escrita

- Use `CREATE TABLE IF NOT EXISTS` e `ALTER TABLE ... ADD COLUMN IF NOT EXISTS`
- Colunas `NOT NULL` sem `DEFAULT`: sempre inclua `UPDATE ... SET col = valor WHERE col IS NULL`
- Dados de seed: use `ON CONFLICT (id) DO NOTHING`
- Índices: `CREATE INDEX IF NOT EXISTS idx_{tabela}_{coluna} ON {tabela} ({coluna})`
- Foreign keys: declare no final, após criação de todas as tabelas

### 4. Se o banco já está rodando localmente (docker-compose)

Aplicar manualmente para não recriar o container:

```bash
docker exec -i salao-postgres psql -U admin -d salao_db < \
  salao-api/src/main/resources/db/migration/V{n}__descricao.sql
```

Depois registrar no Flyway history:

```bash
# Calcular checksum
python3 -c "
import struct, zlib
data = open('salao-api/src/main/resources/db/migration/V{n}__descricao.sql','rb').read()
crc = zlib.crc32(data)
print(struct.unpack('i', struct.pack('I', crc & 0xFFFFFFFF))[0])
"

# Registrar no histórico
docker exec -i salao-postgres psql -U admin -d salao_db -c "
INSERT INTO flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success)
VALUES (
    (SELECT MAX(installed_rank)+1 FROM flyway_schema_history),
    '{n}', '{descrição}', 'SQL', 'V{n}__descricao.sql', {checksum}, 'admin', NOW(), 10, TRUE
);"
```

### 5. Verificar

```bash
./gradlew :salao-api:bootJar
```

Deve compilar e o Flyway deve validar sem erros (`ddl-auto: validate`).
