package com.ust.sdet.contract.pos;

import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public final class OmsClient {

    private final String baseUrl;

    public OmsClient(String url) {
        this.baseUrl = url;
    }

    // ---------------- GET ORDER ----------------
    public Order getOrder(int id) {

        Response response =
                given()
                        .baseUri(baseUrl)
                        .when()
                        .get("/orders/" + id);

        return new Order(
                response.statusCode(),
                ((Number) response.path("id")).intValue(),
                response.path("status"),
                ((Number) response.path("total")).doubleValue()
        );
    }

    // ---------------- CREATE ORDER ----------------
    public CreateOrder createOrder(String sku, int quantity) {

        String jsonBody = """
                {
                  "sku": "%s",
                  "quantity": %d
                }
                """.formatted(sku, quantity);

        Response response =
                given()
                        .baseUri(baseUrl)
                        .contentType("application/json")
                        .body(jsonBody)
                        .when()
                        .post("/orders/123");

        return new CreateOrder(
                response.statusCode(),
                response.path("sku"),
                ((Number) response.path("quantity")).intValue()
        );
    }

    // ---------------- GET INVENTORY ----------------
    public Inventory getInventory(int id) {

        Response response =
                given()
                        .baseUri(baseUrl)
                        .when()
                        .get("/Inventory/" + id);

        return new Inventory(
                response.statusCode(),
                ((Number) response.path("id")).intValue(),
                response.path("status"),
                ((Number) response.path("total")).doubleValue()
        );
    }

    // ---------------- RECORDS ----------------

    public record Order(
            int statusCode,
            int orderId,
            String status,
            double total
    ) {}

    public record CreateOrder(
            int statusCode,
            String sku,
            int quantity
    ) {}

    public record Inventory(
            int statusCode,
            int id,
            String status,
            double total
    ) {}
}