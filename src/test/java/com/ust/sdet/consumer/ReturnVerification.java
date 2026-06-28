package com.ust.sdet.consumer;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.junitsupport.loader.PactBrokerConsumerVersionSelectors;
import au.com.dius.pact.provider.junitsupport.loader.SelectorBuilder;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@Provider("provider")
//@PactFolder("target/pacts")
@PactBroker(
        url = "http://127.0.0.1:9292",
        enablePendingPacts = "true",
        providerTags = "main",
        includeWipPactsSince = "2026-06-27"
)
public class ReturnVerification {
    @RegisterExtension
    private static final WireMockExtension wireMock =
            WireMockExtension.newInstance()
                    .options(wireMockConfig().port(4010))
                    .build();

    @PactBrokerConsumerVersionSelectors
    public static SelectorBuilder
    consumerVersionSelectors() {
        return new SelectorBuilder()
                .mainBranch()
                .deployedOrReleased();
    }


    @BeforeEach
    void setup(PactVerificationContext context) {
        context.setTarget(new HttpTestTarget("127.0.0.1", 4010));
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void verify(PactVerificationContext context) {
        context.verifyInteraction();
    }

    @State("GET /catalog/101")
    void isProductExists() {
        wireMock.stubFor(get(urlEqualTo("/catalog/101"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                    {"id": 1, "name": "test name", "sku": "sku-10"}
                """)));
    }

    @State("GET /catalog/1")
    void isProductNotExists() {
        wireMock.stubFor(get(urlEqualTo("/catalog/1"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                    {"message": "product not found"}
                """)
                ));
    }
}