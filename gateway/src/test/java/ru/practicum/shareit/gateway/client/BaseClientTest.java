package ru.practicum.shareit.gateway.client;

import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import static org.mockito.Mockito.*;

class BaseClientTest {

    @Test
    void prepareGatewayResponse_with2xxResponse() {
        BaseClient client = new BaseClient(mock(RestTemplate.class));

        ResponseEntity<Object> response = ResponseEntity.ok("Success");
    }

    @Test
    void handleHttpStatusCodeException() {
    }
}
