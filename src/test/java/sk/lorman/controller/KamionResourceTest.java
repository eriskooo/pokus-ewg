package sk.lorman.controller;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sk.lorman.repository.KamionRepository;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class KamionResourceTest {

    @Inject
    KamionRepository repository;

    @BeforeEach
    @Transactional
    void cleanDb() {
        repository.deleteAll();
    }

    private String spz() {
        return ("BA" + UUID.randomUUID().toString().substring(0, 6)).toUpperCase().replace("-", "");
    }

    @Test
    void create_and_get_and_list() {
        String spz = spz();
        String body = String.format("{\n  \"spz\": \"%s\",\n  \"znacka\": \"Volvo\",\n  \"nosnostKg\": 15000\n}", spz);

        // create
        String location =
            given()
                .contentType(ContentType.JSON)
                .body(body)
            .when()
                .post("/kamiony")
            .then()
                .statusCode(201)
                .header("Location", notNullValue())
                .body("id", notNullValue())
                .body("spz", equalTo(spz))
                .extract().header("Location");

        // get by location
        given()
        .when()
            .get(location)
        .then()
            .statusCode(200)
            .body("spz", equalTo(spz))
            .body("znacka", equalTo("Volvo"))
            .body("nosnostKg", equalTo(15000));

        // list
        given()
        .when()
            .get("/kamiony")
        .then()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(1))
            .body("spz", hasItem(spz));
    }

    @Test
    void create_invalid_returns_400() {
        String invalid = "{\n  \"znacka\": \"X\",\n  \"nosnostKg\": 1\n}"; // missing spz

        given()
            .contentType(ContentType.JSON)
            .body(invalid)
        .when()
            .post("/kamiony")
        .then()
            .statusCode(400);
    }

    @Test
    void update_and_delete_flow() {
        // create first
        String spz = spz();
        String createBody = String.format("{\n  \"spz\": \"%s\",\n  \"znacka\": \"MAN\",\n  \"nosnostKg\": 10000\n}", spz);
        Long id =
            given()
                .contentType(ContentType.JSON)
                .body(createBody)
            .when()
                .post("/kamiony")
            .then()
                .statusCode(201)
                .extract().body().jsonPath().getLong("id");

        // update partial (change znacka)
        String updateBody = "{\n  \"znacka\": \"Scania\"\n}";
        given()
            .contentType(ContentType.JSON)
            .body(updateBody)
        .when()
            .put("/kamiony/" + id)
        .then()
            .statusCode(200)
            .body("id", equalTo(id.intValue()))
            .body("znacka", equalTo("Scania"))
            .body("spz", equalTo(spz));

        // delete
        given()
        .when()
            .delete("/kamiony/" + id)
        .then()
            .statusCode(204);

        // get should be 404 now
        given()
        .when()
            .get("/kamiony/" + id)
        .then()
            .statusCode(404);

        // delete missing -> 404
        given()
        .when()
            .delete("/kamiony/" + id)
        .then()
            .statusCode(404);
    }
}
