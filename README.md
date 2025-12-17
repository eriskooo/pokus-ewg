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

Kafka connection is configured in `application.properties`. Tests/dev can use in-memory connectors (see `src/test/resources/application.properties`).

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
kubectl apply -n pokus-ewg -f helm/deployment.yaml
kubectl rollout restart deployment/pokus-ewg -n pokus-ewg
kubectl rollout status deployment/pokus-ewg -n pokus-ewg
```

Note: Docker Desktop Kubernetes uses the same Docker daemon, so any image you see under "Images" in Docker Desktop is also available to the Kubernetes nodes on the same host.

## Logging & Lombok
Lombok `@Slf4j` is used for concise logging. Adjust log levels via `application.properties` (e.g., `quarkus.log.category."sk.lorman".level=DEBUG`).

## Notes
- The project uses up‑to‑date Quarkus/Hibernate/SmallRye/Flyway versions managed through the Quarkus 3.30.3 BOM.
- Requires Java 21.

## Deploy to Kubernetes using the helm directory
The helm directory contains plain Kubernetes manifests (not a Helm chart) that you can apply directly with kubectl. They configure:
- A ConfigMap with application.properties overriding app.random=nr2
- A Deployment that mounts the ConfigMap and sets QUARKUS_CONFIG_LOCATIONS so Quarkus picks it up
- A Service to expose the pod inside the cluster
 - A Namespace resource (pokus-ewg) to keep things isolated

Prerequisites:
- A working Kubernetes cluster and kubectl configured to point to it
- A container image accessible by the cluster (the default in the manifests is `lorma/pokus-ewg:snapshot`)
  - If you build the image locally from the Dockerfile, push it to your registry and update `helm/deployment.yaml` field `spec.template.spec.containers[0].image` to your name/tag

Optional: build the image from the Dockerfile and push to your registry (example):

### AWS EKS nasadenie – krok za krokom

Nižšie je príkladový postup, ako aplikáciu nasadiť do AWS (ECR + EKS + voliteľne MSK):

1) Build a push Docker image do ECR
- Prihlásenie do AWS a získanie loginu do ECR:
  - aws ecr get-login-password --region eu-central-1 | docker login --username AWS --password-stdin <AWS_ACCOUNT_ID>.dkr.ecr.eu-central-1.amazonaws.com
- Vytvoriť ECR repozitár (ak ešte neexistuje):
  - aws ecr create-repository --repository-name pokus-ewg --region eu-central-1
- Postaviť a otagovať image:
  - ./mvnw -DskipTests package
  - docker build -t pokus-ewg:snapshot .
  - docker tag pokus-ewg:snapshot <AWS_ACCOUNT_ID>.dkr.ecr.eu-central-1.amazonaws.com/pokus-ewg:snapshot
- Pushnúť do ECR:
  - docker push <AWS_ACCOUNT_ID>.dkr.ecr.eu-central-1.amazonaws.com/pokus-ewg:snapshot

2) Nastaviť Kubernetes namespace a manifesty
- Vytvoriť namespace:
  - kubectl apply -f helm/namespace.yaml
- Upraviť image v helm/deployment.yaml na ECR URL:
  - spec.template.spec.containers[0].image: <AWS_ACCOUNT_ID>.dkr.ecr.eu-central-1.amazonaws.com/pokus-ewg:snapshot
- Service je nastavený na typ LoadBalancer (AWS vytvorí verejný NLB/ELB)

3) Konfigurácia Kafka (AWS MSK alebo iný broker)
- Ak používate AWS MSK (bez SASL/SSL vnútri VPC), nastavte v ConfigMap bootstrap servers:
  - Otvorte helm/configmap.yaml a v časti application.properties odkomentujte/pridajte riadok:
    mp.messaging.connector.smallrye-kafka.bootstrap.servers=b-1.msk-...:9092,b-2.msk-...:9092
- Ak potrebujete SASL/SSL, odporúča sa použiť Kubernetes Secret s potrebnými client.properties a načítať ich cez env/volume. Táto ukážka to nezahŕňa, ale README obsahuje poznámky nižšie.

4) Aplikovať manifesty do EKS
- kubectl apply -n pokus-ewg -f helm/configmap.yaml
- kubectl apply -n pokus-ewg -f helm/deployment.yaml
- kubectl apply -n pokus-ewg -f helm/service.yaml
- Overiť rollout:
  - kubectl rollout status deployment/pokus-ewg -n pokus-ewg

5) Získať verejnú adresu služby
- kubectl get svc pokus-ewg -n pokus-ewg
- Po vzniku externého adresára (EXTERNAL-IP) otvorte http://EXTERNAL-IP:8080/q/swagger-ui alebo /kamiony

6) Logy a zdravie aplikácie
- Liveness: /q/health/live
- Readiness: /q/health/ready
- Logy:
  - kubectl logs -n pokus-ewg deploy/pokus-ewg
  - JSON logging je v k8s zapnutý cez ConfigMap (vhodné pre CloudWatch/ELK)

7) Aktualizácia verzie
- Upravte image tag v deployment.yaml a použite rollout restart:
  - kubectl apply -n pokus-ewg -f helm/deployment.yaml
  - kubectl rollout restart deployment/pokus-ewg -n pokus-ewg
  - kubectl rollout status deployment/pokus-ewg -n pokus-ewg

Poznámky k bezpečnosti a sieti:
- Ak chcete obmedziť prístup, použite Internal LoadBalancer anotácie (napr. service.beta.kubernetes.io/aws-load-balancer-internal: "true") a/alebo Ingress s ALB.
- Pre Kafka cez TLS/SASL pripravte Secret a env premenné podľa požiadaviek MSK (SASL/SCRAM alebo IAM) a upravte SmallRye Kafka konfiguráciu (mp.messaging.*) v ConfigMap/Secret.