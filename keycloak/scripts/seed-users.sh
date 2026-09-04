#!/usr/bin/env bash
# ============================================================
# seed-users.sh — Provisiona usuários de desenvolvimento no Keycloak
# Uso: bash keycloak/scripts/seed-users.sh
# Pré-requisito: Keycloak rodando em http://localhost:8180
# ============================================================
set -euo pipefail

KC_URL="${KC_URL:-http://localhost:8180}"
REALM="${REALM:-salao-realm}"
ADMIN_USER="${KEYCLOAK_ADMIN:-admin}"
ADMIN_PASS="${KEYCLOAK_ADMIN_PASSWORD:?Defina KEYCLOAK_ADMIN_PASSWORD no .env}"

echo "Obtendo token de admin..."
TOKEN=$(curl -sf -X POST "$KC_URL/realms/master/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password&client_id=admin-cli&username=$ADMIN_USER&password=$ADMIN_PASS" \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['access_token'])")

create_user() {
  local username="$1"
  local email="$2"
  local role="$3"
  local temp_pass="Salao@Dev2026!"   # Senha temporária — force-change no primeiro acesso

  echo "Criando usuario: $username ($role)..."
  USER_ID=$(curl -sf -X POST "$KC_URL/admin/realms/$REALM/users" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
      \"username\": \"$username\",
      \"email\": \"$email\",
      \"enabled\": true,
      \"emailVerified\": true,
      \"requiredActions\": [\"UPDATE_PASSWORD\"],
      \"credentials\": [{\"type\": \"password\", \"value\": \"$temp_pass\", \"temporary\": true}]
    }" -D - | grep -i "^location:" | awk -F'/' '{print $NF}' | tr -d '\r')

  # Atribui role ao usuário
  ROLE_REP=$(curl -sf "$KC_URL/admin/realms/$REALM/roles/$role" \
    -H "Authorization: Bearer $TOKEN")
  curl -sf -X POST "$KC_URL/admin/realms/$REALM/users/$USER_ID/role-mappings/realm" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d "[$ROLE_REP]"
  echo "  -> $username criado com role $role (senha temporaria)"
}

create_user "recepcionista" "recepcao@salao.dev" "RECEPTION"
create_user "profissional1" "prof1@salao.dev" "PROFESSIONAL"
create_user "cliente1" "cliente1@salao.dev" "CUSTOMER"

echo "Seed de usuarios concluido."
