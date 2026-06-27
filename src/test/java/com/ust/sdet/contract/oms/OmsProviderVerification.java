package com.ust.sdet.contract.oms;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.junitsupport.loader.PactBrokerConsumerVersionSelectors;
import au.com.dius.pact.provider.junitsupport.loader.SelectorBuilder;

@Provider("oms-provider")
@PactBroker(url = "${pact.broker.url}")   // ✅ FIXED (NO host error)
public class OmsProviderVerification {

    @RegisterExtension
    static WireMockExtension wireMock =
            WireMockExtension.newInstance()
                    .options(wireMockConfig().port(4010))
                    .build();

    @PactBrokerConsumerVersionSelectors
    public static SelectorBuilder selectors() {
        return new SelectorBuilder()
                .mainBranch()
                .deployedOrReleased();
    }

    @BeforeEach
    void setup(PactVerificationContext context) {
        context.setTarget(new HttpTestTarget("localhost", 4010));
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void verify(PactVerificationContext context) {
        context.verifyInteraction();
    }

    @State("Order 123 exists")
    void setupOrder() {
        wireMock.stubFor(
                com.github.tomakehurst.wiremock.client.WireMock.get("/orders/123")
                        .willReturn(
                                com.github.tomakehurst.wiremock.client.WireMock.aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody("""
                                                {"id":123,"status":"CONFIRMED","total":12.0}
                                        """)
                        )
        );
    }
}