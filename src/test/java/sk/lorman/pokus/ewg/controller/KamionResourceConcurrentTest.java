package sk.lorman.pokus.ewg.controller;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sk.lorman.pokus.ewg.wiremock.MyWireMockResource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
@QuarkusTest
@QuarkusTestResource(MyWireMockResource.class)
class KamionResourceConcurrentTest {

    @Test
    void get_kamiony_in_10_threads_concurrently() throws Exception {
        int threads = 10;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CyclicBarrier barrier = new CyclicBarrier(threads);

        List<Future<Integer>> futures = new ArrayList<>(threads);
        for (int i = 0; i < threads; i++) {
            futures.add(pool.submit(() -> {
                // wait until all workers are ready to start at the same time
                barrier.await(10, TimeUnit.SECONDS);
                return RestAssured.given()
                        .when()
                        .get("/kamiony/")
                        .then()
                        .extract()
                        .statusCode();
            }));
        }

        List<Integer> statuses = new ArrayList<>(threads);
        for (Future<Integer> f : futures) {
            statuses.add(f.get(30, TimeUnit.SECONDS));
        }

        pool.shutdownNow();

        // All calls should succeed with HTTP 200
        Assertions.assertEquals(threads, statuses.size());
        Assertions.assertTrue(statuses.stream().allMatch(sc -> sc == 200),
                "All concurrent GET /kamiony/ calls should return 200");
    }
}
