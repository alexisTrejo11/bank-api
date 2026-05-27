# Monitoreo — quién lee qué y desde dónde

## Idea clave

**Spring no tiene URL de Grafana ni de Prometheus.** No existe `spring.grafana.*` en el proyecto.

Hay **tres actores** con **tres configuraciones distintas**:

| Actor | ¿Se conecta a quién? | ¿Dónde se define? | Variables `.env` |
|-------|----------------------|-------------------|------------------|
| **Spring (app)** | Nadie — solo **expone** HTTP | `bank-boot/.../application.yaml` | `MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE` |
| **Prometheus** | **Va a** la app (scrape) | `docker/infra/prometheus/prometheus.yml.template` | `PROMETHEUS_SCRAPE_TARGET`, `PROMETHEUS_METRICS_PATH` |
| **Grafana** | **Va a** Prometheus (datasource) | `docker/infra/grafana/.../prometheus.yml.template` | `GRAFANA_PROMETHEUS_URL` |

```text
Spring expone:  http://<app-host>:<APP_HTTP_PORT>/actuator/prometheus
                         ▲
                         │  PROMETHEUS_SCRAPE_TARGET + PROMETHEUS_METRICS_PATH
                    Prometheus
                         ▲
                         │  GRAFANA_PROMETHEUS_URL
                      Grafana
```

## Dónde se “lee” cada cosa (archivos concretos)

### 1. Spring — qué path publica la app

**Archivo:** `bank-boot/src/main/resources/application.yaml`

```yaml
management:
  endpoints:
    web:
      exposure:
        include: ${MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE:?Set in .env}
```

**`.env`:** `MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,prometheus`

Eso habilita por ejemplo `/actuator/prometheus`. **No hay variable de Grafana aquí.**

### 2. Prometheus — a qué host/path hace scrape

**Template:** `docker/infra/prometheus/prometheus.yml.template`  
**Se renderiza en:** `docker/compose.local.yml` (servicio `prometheus`) al arrancar el contenedor.

**`.env` (local):**
```env
PROMETHEUS_SCRAPE_TARGET=app:8080
PROMETHEUS_METRICS_PATH=/actuator/prometheus
```

**`.env` (AWS — Prometheus en otro EC2):**
```env
PROMETHEUS_SCRAPE_TARGET=10.0.1.20:8080
# o: bank-api.midominio.com:8080
PROMETHEUS_METRICS_PATH=/actuator/prometheus
```

### 3. Grafana — URL del datasource Prometheus

**Template:** `docker/infra/grafana/provisioning/datasources/prometheus.yml.template`  
**Se renderiza en:** `docker/compose.local.yml` (servicio `grafana`).

**`.env` (local):**
```env
GRAFANA_PROMETHEUS_URL=http://prometheus:9090
```

**`.env` (AWS):**
```env
GRAFANA_PROMETHEUS_URL=http://10.0.1.21:9090
# o DNS del EC2 donde corre Prometheus
```

**Puertos UI (host):** `PROMETHEUS_PORT`, `GRAFANA_PORT`, credenciales `GRAFANA_ADMIN_*` — solo Compose local.

## Local vs AWS — qué cambias en `.env`

| Variable | Local (Docker) | AWS (ejemplo) |
|----------|----------------|---------------|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://postgres:5432/...` o RDS local | URL de **RDS** |
| `SPRING_DATA_REDIS_HOST` | `redis` (servicio Compose) | **ElastiCache** hostname |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | `kafka:9092` o tu `192.168.x.x:9092` | MSK / EC2 Kafka |
| `PROMETHEUS_SCRAPE_TARGET` | `app:8080` | IP/DNS del **EC2 de la app**:8080 |
| `GRAFANA_PROMETHEUS_URL` | `http://prometheus:9090` | `http://<ec2-prometheus>:9090` |

Con **`docker/compose.yml`** (solo app en EC2) Spring usa las variables `SPRING_*`.  
Las variables `PROMETHEUS_*` / `GRAFANA_*` las usa **otro** Compose o tu Prometheus en la nube, no la app.

## Validación sin defaults

Antes de levantar contenedores:

```bash
./docker/validate-env.sh app      # deploy / compose.yml
./docker/validate-env.sh local    # compose.local.yml (+ monitoring)
```

Si falta una variable, el script falla. Spring también falla al arrancar con `${VAR:?...}` en YAML.

## Seguridad actuator

`bank-config/.../SecurityConfig.java` — `/actuator/**` sin JWT.  
Prometheus debe poder llegar a `/actuator/prometheus` (red interna o firewall).

## Logs — Loki, files, optional ELK

Full guide: [docs/OBSERVABILITY.md](../docs/OBSERVABILITY.md).

| Channel | Destination | Console? |
|---------|-------------|----------|
| Application (`root`) | JSON stdout + optional Loki push | Yes |
| HTTP access (`ACCESS`) | `logs/access.json` → Promtail → Loki | **No** |
| Compliance (`AUDIT`) | `logs/audit.json` → Promtail → Loki | **No** |

**`.env` (local Compose):**
```env
BANK_LOGGING_LOKI_ENABLED=true
LOKI_URL=http://loki:3100/loki/api/v1/push
GRAFANA_LOKI_URL=http://loki:3100
BANK_LOGGING_DIRECTORY=/app/logs
```

**Grafana:** datasource `Loki` is provisioned from `docker/infra/grafana/provisioning/datasources/loki.yml.template`.

**Optional ELK:** `docker compose --profile elk -f docker/compose.local.yml up -d` starts Elasticsearch, Logstash (port 5044), Kibana. Set `BANK_LOGGING_LOGSTASH_ENABLED=true`. Logstash drops DEBUG/TRACE and actuator noise (`docker/infra/logstash/pipeline/bank.conf`).
