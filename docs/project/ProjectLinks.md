# Project links

Outbound links derived from portfolio metadata. Structured twin: fields under `links` in [`source/ProjectOverview.md`](source/ProjectOverview.md) plus repository URL from [`source/ProjectMetadata.md`](source/ProjectMetadata.md).

## Primary links

| Purpose | URL | Notes |
|---------|-----|--------|
| **Source code (GitHub)** | [https://github.com/alexistrejo11/bank-api](https://github.com/alexistrejo11/bank-api) | Canonical repository |
| **Documentation tree** | [https://github.com/alexistrejo11/bank-api/tree/main/docs](https://github.com/alexistrejo11/bank-api/tree/main/docs) | Includes this `docs/project/` section and versioned guides |
| **Interactive API (demo)** | `https://api.bank.prod.PLACEHOLDER.example.com/swagger-ui/index.html` | Placeholder ALB-style URL; swap for staging/production when deployed |
| **Docker Hub** | *Not published* | `null` in source — add a public image name when you push from CI |

## In-repo navigation

| Resource | Path |
|----------|------|
| Project docs hub | [README.md](README.md) |
| Machine-readable YAML | [source/](source/) |
| Environment template | [../../.env.example](../../.env.example) |
| Compose stack | [../../docker/compose.yml](../../docker/compose.yml) |
| Observability | [../OBSERVABILITY.md](../OBSERVABILITY.md) |
| Monitoring (Prometheus/Grafana) | [../../docker/MONITORING.md](../../docker/MONITORING.md) |
| Application image build | [../../docker/Dockerfile](../../docker/Dockerfile) |

## Suggested links after AWS cutover

- **Public OpenAPI JSON**: `https://<your-host>/v3/api-docs` (protect if not meant to be public).  
- **Grafana / dashboards**: private URL or Amazon Managed Grafana workspace.  
- **Status page** (optional): external provider for incident communication.
