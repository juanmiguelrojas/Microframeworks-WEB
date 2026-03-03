package com.eci.microframework;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

class HttpServerTest {

    @Test
    void shouldParseQueryValues() {
        Map<String, String> values = HttpServer.parseQueryParams("name=Pedro&course=TDSE");

        assertEquals("Pedro", values.get("name"));
        assertEquals("TDSE", values.get("course"));
    }

    @Test
    void shouldDecodeEncodedQueryValues() {
        Map<String, String> values = HttpServer.parseQueryParams("name=Juan+Pablo&city=Bogot%C3%A1");

        assertEquals("Juan Pablo", values.get("name"));
        assertEquals("Bogotá", values.get("city"));
    }

    @Test
    void requestShouldExposeQueryValuesByKey() {
        HttpServer.Request request = new HttpServer.Request("/hello", HttpServer.parseQueryParams("name=Ana"));

        assertEquals("Ana", request.getValues("name"));
        assertNull(request.getValues("unknown"));
    }
}
