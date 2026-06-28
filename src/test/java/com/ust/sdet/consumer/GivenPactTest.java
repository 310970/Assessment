package com.ust.sdet.consumer;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(
        providerName = "provider",
        pactVersion = PactSpecVersion.V4
)
public class GivenPactTest {
    @Pact(provider = "provider", consumer = "consumer")
    V4Pact getProduct(PactDslWithProvider builder) {
        return builder
                .given("GET /catalog/101")
                .uponReceiving("a request for product with id 101")
                .path("/catalog/101")
                .method("GET")
                .willRespondWith()
                .status(200)
                .matchHeader(
                        "Content-Type",
                        "application/json(;.*)?",
                        "application/json")

                .body(new PactDslJsonBody()
                        .integerType("id", 1)
                        .stringType("name", "test name")
                        .stringType("sku", "sku-10"))
                .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "getProduct")
    void testGetProduct(MockServer mockServer) {
        Response response =
                given()
                        .baseUri(mockServer.getUrl())
                        .when()
                        .get("/catalog/101");
        response.then()
                .statusCode(200);

        response.then().log().all();
    }

    @Pact(provider = "provider", consumer = "consumer")
    V4Pact getProduct404(PactDslWithProvider builder) {
        return builder
                .given("GET /catalog/1")
                .uponReceiving("a request for product with id 1")
                .path("/catalog/1")
                .method("GET")
                .willRespondWith()
                .status(404)
                .matchHeader(
                        "Content-Type",
                        "application/json(;.*)?",
                        "application/json")
                .body(new PactDslJsonBody()
                        .stringType("message", "product not found"))
                .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "getProduct404")
    void testGetProduct404(MockServer mockServer) {
        Response response =
                given()
                        .baseUri(mockServer.getUrl())
                        .when()
                        .get("/catalog/1");
        response.then()
                .statusCode(404)
                .body("message", notNullValue());

        response.then().log().all();
    }
}