package ru.practicum.shareit.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.server.item.dto.ItemDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemDtoJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldSerializeItemDto() throws JsonProcessingException {
        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("Дрель")
                .description("Аккумуляторная дрель")
                .available(true)
                .requestId(100L)
                .build();

        String json = objectMapper.writeValueAsString(itemDto);

        assertThat(json).contains("\"id\":1");
        assertThat(json).contains("\"name\":\"Дрель\"");
        assertThat(json).contains("\"description\":\"Аккумуляторная дрель\"");
        assertThat(json).contains("\"available\":true");
        assertThat(json).contains("\"requestId\":100");
    }

    @Test
    void shouldDeserializeItemDto() throws JsonProcessingException {
        String json = """
                {
                  "id": 1,
                  "name": "Дрель",
                  "description": "Аккумуляторная дрель",
                  "available": true,
                  "requestId": 100
                }
                """;

        ItemDto itemDto = objectMapper.readValue(json, ItemDto.class);

        assertThat(itemDto.getId()).isEqualTo(1L);
        assertThat(itemDto.getName()).isEqualTo("Дрель");
        assertThat(itemDto.getDescription()).isEqualTo("Аккумуляторная дрель");
        assertThat(itemDto.getAvailable()).isTrue();
        assertThat(itemDto.getRequestId()).isEqualTo(100L);
    }

    @Test
    void shouldHandlePartialUpdate() throws JsonProcessingException {
        String json = """
                {
                  "name": "Новое название",
                  "available": false
                }
                """;

        ItemDto itemDto = objectMapper.readValue(json, ItemDto.class);

        assertThat(itemDto.getName()).isEqualTo("Новое название");
        assertThat(itemDto.getAvailable()).isFalse();
        assertThat(itemDto.getId()).isNull();
        assertThat(itemDto.getDescription()).isNull();
        assertThat(itemDto.getRequestId()).isNull();
    }
}
