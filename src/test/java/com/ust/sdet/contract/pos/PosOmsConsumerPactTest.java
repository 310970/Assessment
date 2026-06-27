package com.ust.sdet.contract.pos;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;

@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(
        providerName = "oms-provider",
        pactVersion = PactSpecVersion.V4
)
class PosOmsConsumerPactTest {

    // ---------------- GET ORDER ----------------
    @Pact(provider = "oms-provider", consumer = "pos-consumer")
    V4Pact getOrder(PactDslWithProvider builder) {

        return builder
                .given("Order 123 exists")
                .uponReceiving("GET order 123")
                .path("/orders/123")
                .method("GET")
                .willRespondWith()
                .status(200)
                .matchHeader("Content-Type", "application/json(;.*)?", "application/json")
                .body(new PactDslJsonBody()
                        .integerType("id", 123)
                        .stringType("status", "CONFIRMED")
                        .decimalType("total", 12.0)
                )
                .toPact(V4Pact.class);
    }

    // ---------------- CREATE ORDER ----------------
    @Pact(provider = "oms-provider", consumer = "pos-consumer")
    V4Pact createOrder(PactDslWithProvider builder) {

        return builder
                .given("Order created for inventory")
                .uponReceiving("POST create order")
                .path("/orders/123")
                .method("POST")
                .matchHeader("Content-Type", "application/json(;.*)?", "application/json")
                .body(new PactDslJsonBody()
                        .stringType("sku", "SKU-9")
                        .integerType("quantity", 20)
                )
                .willRespondWith()
                .status(201)
                .matchHeader("Content-Type", "application/json(;.*)?", "application/json")
                .body(new PactDslJsonBody()
                        .stringType("sku", "SKU-9")
                        .integerType("quantity", 20)
                )
                .toPact(V4Pact.class);
    }

    // ---------------- GET INVENTORY ----------------
    @Pact(provider = "oms-provider", consumer = "pos-consumer")
    V4Pact getInventoryShow(PactDslWithProvider builder) {

        return builder
                .given("Sku-9 has stock")
                .uponReceiving("GET inventory 7")
                .path("/Inventory/7")
                .method("GET")
                .willRespondWith()
                .status(200)
                .matchHeader("Content-Type", "application/json(;.*)?", "application/json")
                .body(new PactDslJsonBody()
                        .integerType("id", 7)
                        .stringType("status", "Confirmed")
                        .decimalType("total", 42.0)
                )
                .toPact(V4Pact.class);
    }

    // ---------------- TESTS ----------------

    @Test
    @PactTestFor(pactMethod = "getOrder")
    void testGetOrder(MockServer mockServer) {

        OmsClient client = new OmsClient(mockServer.getUrl());
        OmsClient.Order order = client.getOrder(123);

        assertEquals(200, order.statusCode());
        assertEquals(123, order.orderId());
        assertEquals("CONFIRMED", order.status());
        assertEquals(12.0, order.total());
    }

    @Test
    @PactTestFor(pactMethod = "createOrder")
    void testCreateOrder(MockServer mockServer) {

        OmsClient client = new OmsClient(mockServer.getUrl());
        OmsClient.CreateOrder order = client.createOrder("SKU-9", 20);

        assertEquals(201, order.statusCode());
        assertEquals("SKU-9", order.sku());
        assertEquals(20, order.quantity());
    }

    @Test
    @PactTestFor(pactMethod = "getInventoryShow")
    void testGetInventory(MockServer mockServer) {

        OmsClient client = new OmsClient(mockServer.getUrl());

        // FIX: should use Inventory, not Order
        OmsClient.Inventory inventory = client.getInventory(7);

        assertEquals(200, inventory.statusCode());
        assertEquals(7, inventory.id());
        assertEquals("Confirmed", inventory.status());
        assertEquals(42.0, inventory.total());
    }
}