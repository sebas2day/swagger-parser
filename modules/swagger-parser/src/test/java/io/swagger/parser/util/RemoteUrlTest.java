package io.swagger.parser.util;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.swagger.models.auth.AuthorizationValue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.testng.Assert.assertEquals;

public class RemoteUrlTest {

    private static final int WIRE_MOCK_PORT = 8089;
    private static final String EXPECTED_ACCEPTS_HEADER = "application/json, application/yaml, */*";
    private static WireMockServer wireMockServer;

    @BeforeClass
    public void setUpClass() throws Exception {
        wireMockServer = new WireMockServer(WIRE_MOCK_PORT);
        wireMockServer.start();
        WireMock.configureFor(WIRE_MOCK_PORT);
    }

    @AfterClass
    public void tearDownClass() throws Exception {
        wireMockServer.stop();
    }

    @BeforeMethod
    public void setUp() throws Exception {
        WireMock.reset();
    }

    @Test
    public void testReadARemoteUrl() throws Exception {

        final String expectedBody = setupStub();

        final String actualBody = RemoteUrl.urlToString("http://localhost:8089/v2/pet/1", null);
        assertEquals(actualBody, expectedBody);

        verify(getRequestedFor(urlEqualTo("/v2/pet/1"))
                .withHeader("Accept", equalTo(EXPECTED_ACCEPTS_HEADER)));

    }

    @Test
    public void testAuthorizationHeader() throws Exception {

        final String expectedBody = setupStub();

        final String headerName = "Authorization";
        final String headerValue = "foobar";
        final AuthorizationValue authorizationValue = new AuthorizationValue(headerName, headerValue, "header");
        final String actualBody = RemoteUrl.urlToString("http://localhost:8089/v2/pet/1", Arrays.asList(authorizationValue));

        assertEquals(actualBody, expectedBody);

        verify(getRequestedFor(urlEqualTo("/v2/pet/1"))
                        .withHeader("Accept", equalTo(EXPECTED_ACCEPTS_HEADER))
                        .withHeader(headerName, equalTo(headerValue))
        );
    }

    @Test
    public void testAuthorizationQueryParam() throws Exception {
        final String queryParamName = "Authorization";
        final String queryParamValue = "foobar";
        final String expectedBody = "a really good body";


        stubFor(get(urlPathEqualTo("/v2/pet/1"))
                .withQueryParam(queryParamName, equalTo(queryParamValue))
                .willReturn(aResponse()
                        .withBody(expectedBody)
                        .withHeader("Content-Type", "application/json"))

        );

        final AuthorizationValue authorizationValue = new AuthorizationValue(queryParamName, queryParamValue, "query");
        final String actualBody = RemoteUrl.urlToString("http://localhost:8089/v2/pet/1", Arrays.asList(authorizationValue));

        assertEquals(actualBody, expectedBody);

        verify(getRequestedFor(urlPathEqualTo("/v2/pet/1"))
                        .withHeader("Accept", equalTo(EXPECTED_ACCEPTS_HEADER))
                        .withQueryParam(queryParamName, equalTo(queryParamValue))
        );
    }

    private String setupStub() {
        final String expectedBody = "a really good body";
        stubFor(get(urlEqualTo("/v2/pet/1"))
                .willReturn(aResponse()
                        .withBody(expectedBody)
                        .withHeader("Content-Type", "application/json")
                        ));
        return expectedBody;
    }
}
