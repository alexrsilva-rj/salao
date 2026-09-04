# Realm Keycloak — Ambiente de Desenvolvimento

## ⚠️ Chaves Criptográficas

O bloco `org.keycloak.keys.KeyProvider` foi **intencionalmente removido** deste arquivo de exportação.

**Por quê?** Versionar chaves privadas RSA no repositório permite que qualquer pessoa com acesso ao código forje tokens JWT válidos para qualquer identidade.

**Como funciona sem o bloco?** O Keycloak **gera automaticamente** um novo par de chaves RSA ao importar o realm (`--import-realm`). As chaves geradas ficam armazenadas apenas no banco de dados do Keycloak e nunca são versionadas.

## Uso em Desenvolvimento Local

```bash
# 1. Copiar o arquivo de variáveis de ambiente
cp .env.example .env
# 2. Editar .env com as credenciais desejadas
# 3. Subir o ambiente
docker-compose up -d
```

## Provisionamento de Produção

> **NUNCA** exportar o realm de produção com as chaves incluídas.
> No console do Keycloak: `Realm Settings → Keys → Providers` — as chaves são gerenciadas internamente.

Para criar client secrets no ambiente produtivo, use a API REST ou o KC CLI:
```bash
kcadm.sh create clients/{client-id}/client-secret -r salao-realm
```
