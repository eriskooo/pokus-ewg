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

## Container image (Jib) – local registry
The project is configured to build and push a container image to a local registry on every Maven build:

```
docker run -d -p 5000:5000 --name registry registry:2  # once
./mvnw -DskipTests package
```

Resulting image: `localhost:5000/pokus-ewg:snapshot` (using Quarkus Container Image Jib extension).

## Build
Standard build:

```
./mvnw package
```

Native build (optional):

```
./mvnw package -Dnative
```

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

Prerequisites:
- A working Kubernetes cluster and kubectl configured to point to it
- A container image accessible by the cluster (default in manifests is lorma/pokus-ewg:snapshot)
  - If you build locally, either push to an accessible registry or change the image in helm/deployment.yaml accordingly

Optional: build and push the image (example using local registry already described above):

```
./mvnw -DskipTests package
# Image is pushed to localhost:5000/pokus-ewg:snapshot if local registry is running
```

1) Choose namespace (recommended)
- Either use default, or create/use a dedicated one, e.g. pokus-ewg:

```
kubectl create namespace pokus-ewg
```

2) Apply manifests
- If you created a namespace:

```
kubectl apply -n pokus-ewg -f helm/configmap.yaml
kubectl apply -n pokus-ewg -f helm/deployment.yaml
kubectl apply -n pokus-ewg -f helm/service.yaml
```

- If you use the default namespace:

```
kubectl apply -f helm/configmap.yaml
kubectl apply -f helm/deployment.yaml
kubectl apply -f helm/service.yaml
```

Tip: You can also apply the whole folder:

```
kubectl apply -n pokus-ewg -f helm/
```

3) Verify

```
kubectl get pods -n pokus-ewg
kubectl logs -n pokus-ewg deploy/pokus-ewg
```

In the logs you should see the startup message and an INFO line with the random property value, for example:

- Random property app.random=nr2

4) Troubleshooting
- ConfigMap not found / FailedMount: Ensure the ConfigMap is applied in the same namespace as the Deployment and that you applied configmap.yaml before the deployment (or apply the whole helm/ directory in one command). Example error: MountVolume.SetUp failed for volume "app-config": configmap "pokus-ewg-config" not found
- ImagePullBackOff: Make sure the image name in helm/deployment.yaml exists and the cluster can pull it (push to a reachable registry or use an image present on all nodes).
- Port/access: The included Service is ClusterIP. To reach it from outside the cluster, use port-forward or create a NodePort/Ingress as needed.

5) Remove

```
kubectl delete -n pokus-ewg -f helm/
# or, if you used default namespace:
kubectl delete -f helm/
```
