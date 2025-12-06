package ru.practicum.shareit.gateway.request;

import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import ru.practicum.shareit.gateway.request.dto.ItemRequestCreateDto;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ItemRequestClientTest {

    @Test
    void allMethods_shouldBeCovered() {
        var client = new ItemRequestClient("http://unreachable-host", new RestTemplateBuilder());
        ItemRequestCreateDto dto = new ItemRequestCreateDto();

        assertThrows(Exception.class, () -> client.createRequest(dto, 1L));
        assertThrows(Exception.class, () -> client.getUserRequests(1L));
        assertThrows(Exception.class, () -> client.getAllRequests(1L, 0, 10));
        assertThrows(Exception.class, () -> client.getRequestById(100L, 1L));
    }
}
