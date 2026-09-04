# Checklist Mandatório de Provisionamento do Keycloak em Produção

Este documento consolida as diretrizes e o checklist de segurança para o provisionamento do realm `salao-realm` no ambiente de UAT/Produção.

---

### 1. Material Criptográfico e Chaves RSA (Issue 01 — Crítica)
- [ ] **Não versionar o bloco `keys`** (`org.keycloak.keys.KeyProvider`) no realm de produção.
- [ ] Gerar novo par de chaves RSA via CLI/API do Keycloak ou Admin Console (`Realm Settings -> Keys -> Providers`).
- [ ] Rotacionar imediatamente a chave RSA de todos os ambientes que utilizaram o export de desenvolvimento.

---

### 2. Client Secrets (Issue 02 — Crítica)
- [ ] **Nunca versionar `secret` literal** nos arquivos JSON do realm.
- [ ] Definir os segredos dos clients (`spring-api`, etc.) em tempo de provisionamento usando variáveis de ambiente ou via KC CLI (`kcadm.sh`).
- [ ] Rotacionar todos os secrets dos clients expostos no repositório de desenvolvimento.

---

### 3. Redirecionamentos e Origens CORS (Issue 06 — Alta)
- [ ] Configurar `redirectUris` e `webOrigins` de forma **estrita** no client `spring-api` e na SPA.
- [ ] Remover quaisquer coringas (`/*`). Declarar explicitamente apenas as URLs HTTPS e domínios válidos de produção (ex: `https://api.salao.com/*`, `https://app.salao.com`).

---

### 4. Fluxo de Autenticação da SPA (Issue 07 — Alta)
- [ ] Configurar a SPA (`react-client`) como **Client Público** (`publicClient: true`).
- [ ] **Desabilitar Direct Access Grants** (`directAccessGrantsEnabled: false`).
- [ ] Habilitar exclusivamente o fluxo **Authorization Code com PKCE** (`pkce.code.challenge.method: S256`).

---

### 5. Senhas de Usuários e Exports (Issue 14 — Média)
- [ ] **Não provisionar usuários utilizando hashes de senhas dos exports de dev** (`credentials` com `secretData`).
- [ ] Provisionar contas de produção via CLI/API com senhas fortes e individuais, forçando a alteração de senha no primeiro acesso (`requiredActions: ["UPDATE_PASSWORD"]`).

---

### 6. Matriz e Nomenclatura de Roles (Issue 17 — Alta, Pendência Condicional)
- [ ] Validar que o realm de produção contém exatamente as roles esperadas pelo Spring Security:
  - `RECEPTION` (mapeada para `ROLE_RECEPTION`)
  - `PROFESSIONAL` (mapeada para `ROLE_PROFESSIONAL`)
  - `CUSTOMER` (mapeada para `ROLE_CUSTOMER`)
- [ ] Confirmar que a normalização de roles no código backend se mantém alinhada com as roles configuradas no realm produtivo.
