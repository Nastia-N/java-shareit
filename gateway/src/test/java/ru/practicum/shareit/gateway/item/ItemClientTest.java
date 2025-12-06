package ru.practicum.shareit.gateway.item;

import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import ru.practicum.shareit.gateway.item.dto.CommentDto;
import ru.practicum.shareit.gateway.item.dto.ItemDto;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ItemClientTest {

    @Test
    void allMethods_shouldBeCovered() {
        var client = new ItemClient("http://unreachable-host", new RestTemplateBuilder());
        ItemDto itemDto = ItemDto.builder().build();
        CommentDto commentDto = CommentDto.builder().build();

        assertThrows(Exception.class, () -> client.getItemById(1L, 100L));
        assertThrows(Exception.class, () -> client.addComment(1L, 100L, commentDto));
    }
}