# pokus-ewg

## Lombok a SLF4J

Do projektu bol pridaný Lombok a v aplikácii sa používa anotácia `@Slf4j` pre logovanie cez SLF4J.

Zmeny:
- `pom.xml`: pridaná závislosť `org.projectlombok:lombok` (scope `provided`) a annotation processor pre Maven Compiler Plugin.
- Pre SLF4J API je pridaná závislosť `org.slf4j:slf4j-api` a most/bridge `org.jboss.slf4j:slf4j-jboss-logmanager` pre Quarkus runtime.
- Triedy `KtopConsumer`, `KamionResource`, `KamionService`, `KamionRepository` používajú anotáciu `@Slf4j` a volajú `log.info/debug/...`.

Poznámky k IDE:
- V IntelliJ IDEA povoľte Annotation Processing: Settings > Build, Execution, Deployment > Compiler > Annotation Processors > Enable annotation processing.

Konfigurácia úrovne logovania:
- V `src/main/resources/application.properties` môžete nastaviť úrovne, napr.:
  - `quarkus.log.category."sk.lorman".level=DEBUG`

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.

## Swagger UI / OpenAPI

Swagger UI je po pridaní závislosti `quarkus-smallrye-openapi` dostupný automaticky v dev/test režime na:

- Swagger UI: http://localhost:8080/q/swagger-ui
- OpenAPI (JSON): http://localhost:8080/q/openapi

Spustite aplikáciu v dev móde (`./mvnw quarkus:dev`) a otvorte URL vyššie v prehliadači.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.

## Packaging and running the application

The application can be packaged using:

```shell script
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```shell script
./mvnw package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/pokus-ewg-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/maven-tooling>.

## Related Guides

- JDBC Driver - H2 ([guide](https://quarkus.io/guides/datasource)): Connect to the H2 database via JDBC
- RESTEasy Classic JSON-B ([guide](https://quarkus.io/guides/rest-json)): JSON-B serialization support for RESTEasy Classic
- Hibernate ORM with Panache ([guide](https://quarkus.io/guides/hibernate-orm-panache)): Simplify your persistence code for Hibernate ORM via the active record or the repository pattern

## Provided Code

### Hibernate ORM

Create your first JPA entity

[Related guide section...](https://quarkus.io/guides/hibernate-orm)

[Related Hibernate with Panache section...](https://quarkus.io/guides/hibernate-orm-panache)


### REST

Easily start your REST Web Services

[Related guide section...](https://quarkus.io/guides/getting-started-reactive#reactive-jax-rs-resources)

### RESTEasy JAX-RS

Easily start your RESTful Web Services

[Related guide section...](https://quarkus.io/guides/getting-started#the-jax-rs-resources)

## Kafka / Event-driven consumer

- Projekt obsahuje event-driven bean `KtopConsumer`, ktorý počúva na kanál/temu "ktop".
- V dev/test režime je predvolene použitý in-memory konektor (nepotrebujete bežiace Kafka broker): správy môžete posielať pomocou SmallRye in-memory kanála.
- V prod profile sa používa Kafka konektor. Konfigurácia je v `application.properties` (premenné `KAFKA_BOOTSTRAP_SERVERS`, `KAFKA_GROUP_ID`).

URL a nastavenia:
- `%prod.mp.messaging.incoming.ktop.connector=smallrye-kafka`
- `%prod.mp.messaging.incoming.ktop.topic=ktop`
- `%prod.mp.messaging.incoming.ktop.bootstrap.servers=${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}`
