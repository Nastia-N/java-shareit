package ru.practicum.shareit.gateway.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BaseClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Test
    void testAllMethods() {
        BaseClient client = new BaseClient(restTemplate);

        ResponseEntity<Object> mockResponse = ResponseEntity.ok("test");

        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(Object.class)))
                .thenReturn(mockResponse);

        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(Object.class), any(Map.class)))
                .thenReturn(mockResponse);

        assertNotNull(client.get("/test"));
        assertNotNull(client.get("/test", 1L));
        assertNotNull(client.get("/test", 1L, Map.of("key", "value")));

        assertNotNull(client.post("/test", "body"));
        assertNotNull(client.post("/test", 1L, "body"));
        assertNotNull(client.post("/test", 1L, Map.of("key", "value"), "body"));

        assertNotNull(client.put("/test", 1L, "body"));
        assertNotNull(client.put("/test", 1L, Map.of("key", "value"), "body"));

        assertNotNull(client.patch("/test", "body"));
        assertNotNull(client.patch("/test", 1L));
        assertNotNull(client.patch("/test", 1L, "body"));
        assertNotNull(client.patch("/test", 1L, Map.of("key", "value"), "body"));

        assertNotNull(client.delete("/test"));
        assertNotNull(client.delete("/test", 1L));
        assertNotNull(client.delete("/test", 1L, Map.of("key", "value")));
    }

    @Test
    void testWithParameters() {
        BaseClient client = new BaseClient(restTemplate);

        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(Object.class), any(Map.class)))
                .thenReturn(ResponseEntity.ok("test"));

        ResponseEntity<Object> response = client.get("/test", 1L, Map.of("id", 123));

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testErrorHandling() {
        BaseClient client = new BaseClient(restTemplate);

        HttpClientErrorException exception = new HttpClientErrorException(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                "{\"error\":\"message\"}".getBytes(),
                null
        );

        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(Object.class)))
                .thenThrow(exception);

        ResponseEntity<Object> response = client.get("/test");

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testHeaders() {
        BaseClient client = new BaseClient(restTemplate);

        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(Object.class)))
                .thenAnswer(invocation -> {
                    HttpEntity<?> entity = invocation.getArgument(2);
                    assertTrue(entity.getHeaders().containsKey("X-Sharer-User-Id"));
                    assertEquals("123", entity.getHeaders().getFirst("X-Sharer-User-Id"));
                    return ResponseEntity.ok("ok");
                });

        client.get("/test", 123L);
    }

    @Test
    void testNoUserIdHeader() {
        BaseClient client = new BaseClient(restTemplate);

        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(Object.class)))
                .thenAnswer(invocation -> {
                    HttpEntity<?> entity = invocation.getArgument(2);
                    assertFalse(entity.getHeaders().containsKey("X-Sharer-User-Id"));
                    return ResponseEntity.ok("ok");
                });

        client.get("/test");
    }

    @Test
    void testPrepareGatewayResponseSuccess() {
        BaseClient client = new BaseClient(restTemplate);

        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(Object.class)))
                .thenReturn(ResponseEntity.ok("success"));

        ResponseEntity<Object> response = client.get("/test");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("success", response.getBody());
    }

    @Test
    void testPrepareGatewayResponseErrorWithBody() {
        BaseClient client = new BaseClient(restTemplate);

        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(Object.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).body("not found"));

        ResponseEntity<Object> response = client.get("/test");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("not found", response.getBody());
    }

    @Test
    void testPrepareGatewayResponseErrorWithoutBody() {
        BaseClient client = new BaseClient(restTemplate);

        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(Object.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());

        ResponseEntity<Object> response = client.get("/test");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testHttpStatusCodeException() {
        BaseClient client = new BaseClient(restTemplate);

        byte[] errorBody = "Error message".getBytes();
        HttpClientErrorException exception = HttpClientErrorException.create(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                HttpHeaders.EMPTY,
                errorBody,
                null
        );

        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(Object.class)))
                .thenThrow(exception);

        ResponseEntity<Object> response = client.get("/test");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertArrayEquals(errorBody, (byte[]) response.getBody());
    }
}
