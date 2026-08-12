#!/bin/bash
# -------------------------------------------------------
# Vault configuration script
# Run this after every Vault restart (dev mode is ephemeral)
# Usage: ./scripts/setup-vault.sh
# -------------------------------------------------------
set -e

VAULT_POD="vault-0"
VAULT_NS="vault"

echo "Configuring Vault..."

kubectl exec -n $VAULT_NS $VAULT_POD -- sh -c "
  export VAULT_TOKEN=root
  export VAULT_ADDR=http://127.0.0.1:8200

  vault kv put secret/insurance/policy-service \
    db_username='insurance_user' \
    db_password='postgres-init-password'

  vault auth enable kubernetes 2>/dev/null || true

  vault write auth/kubernetes/config \
    kubernetes_host='https://kubernetes.default.svc.cluster.local:443'

  vault policy write policy-service - <<POLICY
path \"secret/data/insurance/policy-service\" {
  capabilities = [\"read\"]
}
POLICY

  vault write auth/kubernetes/role/policy-service \
    bound_service_account_names=policy-service \
    bound_service_account_namespaces=insurance \
    policies=policy-service \
    ttl=1h

  echo 'Vault configuration complete'
  vault kv get secret/insurance/policy-service
"

echo "Done. Restart policy-service pods if needed:"
echo "kubectl delete pods -n insurance -l app=policy-service"
