# pokus-ewg

A small Quarkus 3 application that demonstrates:
- Reactive REST endpoints (RESTEasy Reactive + Mutiny)
- Persistence with Hibernate ORM with Panache
- Flyway database migrations
- Kafka messaging with SmallRye Reactive Messaging (startup message producer + simple consumer)

This repository is cleaned up and ready for presentation.

## Tech stack (modern versions)
- Java 21
- Quarkus 3.30.3
- Hibernate ORM 6 (managed by the Quarkus BOM)
- SmallRye Reactive Messaging Kafka
- Flyway for SQL migrations
- H2 (in-memory) for local development and tests

## Getting started
Run in dev mode with live reload:

```
./mvnw quarkus:dev
```

Dev UI: http://localhost:8080/q/dev/

OpenAPI/Swagger UI is enabled:
- Swagger UI: http://localhost:8080/q/swagger-ui
- OpenAPI JSON: http://localhost:8080/q/openapi

## REST API (Reactive)
Resource: /kamiony
- GET /kamiony → list trucks
- GET /kamiony/{id} → get by id
- POST /kamiony → create (JSON body)
- PUT /kamiony/{id} → partial update
- DELETE /kamiony/{id} → delete

The resource methods return Mutiny Uni<> and offload blocking persistence to a worker pool.

## Database & Flyway
Flyway runs automatically at startup and applies SQL migrations from `src/main/resources/db/migration`:
- V1__create_schema.sql
- V2__insert_kamiony.sql

Datasource defaults (local dev): H2 in-memory. See `src/main/resources/application.properties` for details.

## Kafka messaging
- Topic: `my.first.topic`
- Outgoing channel `ktop-out` sends a startup message with the current timestamp when the app boots.
- Incoming channel `ktop-in` logs received messages.

Kafka connection is configured in `application.properties`. By default it points to `${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}` so:
- Locally it uses `localhost:9092` if the env var is not set.
- In Kubernetes it is provided via env var in the Deployment: `KAFKA_BOOTSTRAP_SERVERS=kafka:9092`.
You can also override via the mounted ConfigMap by setting `mp.messaging.connector.smallrye-kafka.bootstrap.servers`.
Tests/dev can use in-memory connectors (see `src/test/resources/application.properties`).

### Kafka for Kubernetes (dev) — provided manifest
For convenience, the `helm` folder contains a simple, single‑node Kafka‑API compatible broker using Redpanda. It is suitable for local/dev use on Docker Desktop Kubernetes.

- Manifest: `helm/kafka.yaml`
- Service: `kafka` (ClusterIP), port `9092` — your app connects to `kafka:9092` (already set in the Deployment env)
- Topics: auto‑creation is enabled, so `my.first.topic` will be created on first use

How to deploy it before the application:

```
kubectl apply -f helm/kafka.yaml
kubectl wait --for=condition=available deployment/kafka --timeout=120s

# then deploy the app
kubectl apply -f helm/configmap.yaml
kubectl apply -f helm/deployment.yaml
kubectl apply -f helm/service.yaml
kubectl rollout restart deployment/pokus-ewg
kubectl rollout status deployment/pokus-ewg
```

Optional: Redpanda admin API (port 9644) is exposed only inside the cluster. For debugging you can port‑forward:

```
kubectl port-forward deploy/kafka 9644:9644
curl http://localhost:9644/v1/status/ready
```

Troubleshooting:
- If the app readiness shows DOWN because Kafka is not yet ready, wait for the `kafka` deployment to become available, or temporarily disable messaging health via ConfigMap (`quarkus.messaging.health.enabled=false`).
- Ensure the Service name `kafka` is resolvable in the same namespace where the app runs.

### Logs and Kibana (optional)

If you want to browse application/container logs in Kibana, simple dev manifests are provided in `helm`:

- `helm/elasticsearch.yaml` — single-node Elasticsearch (security disabled for dev)
- `helm/kibana.yaml` — Kibana connected to the above Elasticsearch
- `helm/fluent-bit.yaml` — DaemonSet that ships Kubernetes container logs to Elasticsearch

Deploy to the same namespace (example uses `pokus-ewg`):

```
kubectl apply -f helm/elasticsearch.yaml
kubectl wait --for=condition=ready pod -l app=elasticsearch --timeout=180s

kubectl apply -f helm/kibana.yaml
kubectl wait --for=condition=available deployment/kibana --timeout=180s

kubectl apply -f helm/fluent-bit.yaml
```

Where to find Kibana:

- Inside the cluster: Service `kibana` on port `5601`
- From your machine (port-forward):

```
kubectl port-forward svc/kibana 5601:5601
```

Then open http://localhost:5601 in your browser.

First time in Kibana, create an index pattern `logstash-*` to see logs collected by Fluent Bit (it sends logs using the
Logstash index format).

Troubleshooting Kibana OOM (JavaScript heap out of memory):

- If Kibana crashes with a message like "FATAL ERROR: Ineffective mark-compacts near heap limit Allocation failed -
  JavaScript heap out of memory", apply the updated `helm/kibana.yaml` which:
    - increases the container memory limit to 1Gi and request to 512Mi, and
    - sets `NODE_OPTIONS=--max-old-space-size=1024` to align the Node.js heap with the container limit.
- Redeploy after changes:

```
kubectl apply -f helm/kibana.yaml
kubectl rollout restart deployment/kibana
kubectl rollout status deployment/kibana
```

## Build
Standard build:

```
./mvnw package
```

Native build (optional):

```
./mvnw package -Dnative
```

## Container image (Dockerfile)
The project now uses a simple Dockerfile based on OpenJDK (Temurin 21 JRE).

Build the uber-jar and the Docker image:

```
./mvnw -DskipTests package
docker build -t lorma/pokus-ewg:snapshot .
```

Run locally:

```
docker run --rm -p 8080:8080 lorma/pokus-ewg:snapshot
```

External configuration: you can mount a directory with configuration and Quarkus will pick it up via the QUARKUS_CONFIG_LOCATIONS environment variable (preconfigured in the Dockerfile to `/etc/pokus-ewg-config`):

```
# Windows PowerShell
docker run --rm -p 8080:8080 -v %CD%\helm\app-config:/etc/pokus-ewg-config lorma/pokus-ewg:snapshot

# Linux/macOS
docker run --rm -p 8080:8080 -v $(pwd)/helm/app-config:/etc/pokus-ewg-config lorma/pokus-ewg:snapshot
```


### Docker Desktop: make the image available to Kubernetes (no push needed)
If you use the Kubernetes that is built into Docker Desktop, you can build the image locally and the deployment can use it without pushing to any registry:

1) Build the image locally (as above):

```
./mvnw -DskipTests package
docker build -t lorma/pokus-ewg:snapshot .
```

2) Adjust the image pull policy for development (so it doesn't try to pull from a registry):
- In `helm/deployment.yaml` set `imagePullPolicy` to `IfNotPresent` (or temporarily `Never`).

3) Deploy/rollout restart:

```
kubectl apply -f helm/deployment.yaml
kubectl rollout restart deployment/pokus-ewg
kubectl rollout status deployment/pokus-ewg
```

Note: Docker Desktop Kubernetes uses the same Docker daemon, so any image you see under "Images" in Docker Desktop is also available to the Kubernetes nodes on the same host.

## Logging & Lombok
Lombok `@Slf4j` is used for concise logging. Adjust log levels via `application.properties` (e.g., `quarkus.log.category."sk.lorman".level=DEBUG`).

## Notes
- The project uses up‑to‑date Quarkus/Hibernate/SmallRye/Flyway versions managed through the Quarkus 3.30.3 BOM.
- Requires Java 21.

## Health checks (readiness & liveness) and graceful shutdown

This application exposes Kubernetes‑friendly health endpoints and is configured for graceful shutdown. You can use them locally and in Kubernetes.

### Endpoints
- Liveness: `GET /q/health/live` — is the app process alive (no fatal errors).
- Readiness: `GET /q/health/ready` — is the app ready to receive traffic.
- All health: `GET /q/health`.

Examples:
- Local: `http://localhost:8080/q/health/ready`
- Via Ingress (if configured): `http://<INGRESS_HOST>/q/health/ready`

### Kubernetes probes (configured in helm/deployment.yaml)
The Deployment is already wired to these endpoints:

```
livenessProbe:
  httpGet:
    path: /q/health/live
    port: 8080
  initialDelaySeconds: 15
  periodSeconds: 10

readinessProbe:
  httpGet:
    path: /q/health/ready
    port: 8080
  initialDelaySeconds: 5
  periodSeconds: 5
```

Notes about readiness composition:
- By default, Quarkus readiness includes several checks (e.g., database). Reactive Messaging (Kafka) health can also be included when enabled.
- If you are running in Kubernetes without a reachable Kafka broker, enable/disable the messaging health accordingly to avoid keeping the pod unready:
  - In `helm/configmap.yaml` you can set:
    - `quarkus.messaging.health.enabled=true` to include Kafka in health (production, when Kafka is reachable).
    - `quarkus.messaging.health.enabled=false` to exclude Kafka from health (development, when Kafka is not available to the cluster).

### Graceful shutdown
The app and Kubernetes are configured to shut down gracefully to minimize dropped requests:

- Kubernetes (helm/deployment.yaml):
  - `terminationGracePeriodSeconds: 30` — time given to the pod to shut down.
  - `preStop` hook: `sleep 10` — gives time for endpoints to be removed from the Service and for inflight requests to drain before the process starts shutting down.
- Quarkus (application.properties):
  - `quarkus.http.graceful-shutdown=true` — stop accepting new HTTP connections during shutdown while allowing in‑flight ones to finish.
  - `quarkus.shutdown.timeout=30s` — maximum time Quarkus will wait for ongoing work to complete.
- Application code:
  - `GracefulShutdown` bean observes `ShutdownEvent` and logs a message; a safe place for custom cleanup in the future.

Shutdown sequence:
1) Kubernetes removes the pod from Service endpoints; `preStop` waits 10s to let traffic drain.
2) Kubernetes sends SIGTERM; Quarkus begins graceful shutdown and waits up to 30s for in‑flight work to complete.
3) If the app does not exit within the grace period, Kubernetes will SIGKILL it.

Troubleshooting:
- If `/q/health/ready` reports DOWN because of Kafka when you don’t have Kafka reachable from the cluster, set `quarkus.messaging.health.enabled=false` via ConfigMap and restart the Deployment.

## Deploy to Kubernetes on Docker Desktop (using the helm directory)
The helm directory contains plain Kubernetes manifests (not a Helm chart) that you can apply directly with kubectl on Docker Desktop. They configure:
- A ConfigMap with application.properties overriding app.random=nr2
- A Deployment that mounts the ConfigMap and sets QUARKUS_CONFIG_LOCATIONS so Quarkus picks it up
- A Service to expose the pod inside the cluster

Prerequisites:
- Docker Desktop with Kubernetes enabled, and kubectl context set to Docker Desktop
- A local container image built as shown in the previous section (Docker Desktop shares the same image store with its Kubernetes)

Notes for Docker Desktop:
- `imagePullPolicy: IfNotPresent` is already set in `helm/deployment.yaml`, so the pod will use your locally built image without pulling from a registry.
- If you change the image tag/name, update it in `helm/deployment.yaml` accordingly and redeploy.

### Ingress vs. Swagger UI (important)

In this project, the Ingress is configured to expose only the REST API under the path `/kamiony/` (see `helm/ingress.yaml`).

- Access via Ingress: suitable for API calls, for example:
  - `http://<INGRESS_HOST>/kamiony/`
  - `http://<INGRESS_HOST>/kamiony/{id}`

- Swagger UI is NOT exposed through Ingress: the `http://.../q/swagger-ui` interface is not routed via Ingress in this setup. Swagger UI is available only through the Service of type NodePort (or via port-forward).

How to open Swagger UI:

1) via NodePort

```
# get the NodePort of the pokus-ewg service
kubectl get svc pokus-ewg -o jsonpath="{.spec.ports[0].nodePort}"
```

- Docker Desktop (node on localhost):
  - Swagger UI: `http://localhost:<NODEPORT>/q/swagger-ui`

2) alternative via port-forward (without NodePort):

```
kubectl port-forward svc/pokus-ewg 8080:8080
# then open in the browser
http://localhost:8080/q/swagger-ui
```

Note: The repository currently exposes `/q` through the Ingress so health endpoints are reachable. If you also want to expose Swagger UI, access it at `http://<INGRESS_HOST>/q/swagger-ui` (consider securing it in production).