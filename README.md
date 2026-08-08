# Kubernetes Vault Secrets Management
> Production-grade secrets injection for Spring Boot microservices

## The Problem

In regulated environments — insurance platforms, fintech APIs — Spring Boot services connect to databases using credentials stored in `application.properties`, environment variables, or base64-encoded Helm values.

**Base64 is not encryption.** One compromised pod exposes your entire database.

## The Solution

A secrets management pattern where:
- ✅ Credentials **never** enter the repository
- ✅ Credentials **never** appear in Kubernetes manifests  
- ✅ Secrets **rotate** without restarting pods
- ✅ Every access is **audited**

## Architecture

┌─────────────────────────────────────────────┐
│ Kubernetes Cluster │
│ │
│ ┌─────────────────┐ ┌────────────────┐ │
│ │ Policy Service │◄───│ Vault Agent │ │
│ │ (Spring Boot) │ │ (Sidecar) │ │
│ └─────────────────┘ └───────┬────────┘ │
│ │ │
│ ┌────────────▼────────┐ │
│ │ HashiCorp Vault │ │
│ └─────────────────────┘ │
│ │
│ ┌─────────────────┐ │
│ │ PostgreSQL │ │
│ └─────────────────┘ │
└─────────────────────────────────────────────┘


**Flow:**
1. Pod starts → Vault Agent sidecar authenticates via K8s Service Account
2. Vault validates the service account against its K8s auth backend
3. Vault Agent writes secrets to a shared in-memory volume
4. Spring Boot reads credentials from files — zero hardcoding, zero leaks

## Stack

| Component | Tool | Purpose |
|-----------|------|---------|
| Local K8s | kind | Cluster runtime |
| Secret Store | HashiCorp Vault | Credential management |
| Database | PostgreSQL | Data persistence |
| Application | Spring Boot | Policy Service API |
| Injector | Vault Agent Sidecar | Runtime secret injection |

## Project Structure

k8s-vault-secrets-management/
├── app/ # Spring Boot Policy Service
├── k8s/
│ ├── vault/ # Vault Helm values + policies
│ ├── postgres/ # PostgreSQL manifests
│ └── app/ # Policy Service manifests
├── docs/
│ └── architecture.md # Detailed architecture
└── README.md


## Getting Started

> Prerequisites: Docker, kind, kubectl, helm

```bash
# 1. Create the cluster
kind create cluster --name vault-demo

# 2. Install Vault
helm repo add hashicorp https://helm.releases.hashicorp.com
helm install vault hashicorp/vault -n vault --create-namespace \
  -f k8s/vault/values.yaml

# 3. Deploy PostgreSQL
kubectl apply -f k8s/postgres/

# 4. Configure Vault
./scripts/setup-vault.sh

# 5. Deploy the Policy Service
kubectl apply -f k8s/app/
```

## The Wow Moment

Rotate the database password in Vault — the app picks it up with **zero pod restarts**:

```bash
vault kv put secret/policy-service/db password="rotated-password"
kubectl logs -f deployment/policy-service | grep "Refreshed credentials"
```

## Context

This pattern is directly inspired by real challenges in regulated insurance platforms where credential sprawl is a genuine security risk.

---

Built by [Beldine Oluoch](https://github.com/Bels3)