package ru.practicum.shareit.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.server.item.ItemController;
import ru.practicum.shareit.server.item.dto.CommentDto;
import ru.practicum.shareit.server.item.dto.ItemDto;
import ru.practicum.shareit.server.item.dto.ItemForOwnerDto;
import ru.practicum.shareit.server.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.server.item.service.ItemService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Test
    void createItem_shouldReturnCreatedItem() throws Exception {
        ItemDto requestDto = ItemDto.builder()
                .name("Дрель")
                .description("Аккумуляторная дрель")
                .available(true)
                .build();

        ItemDto responseDto = ItemDto.builder()
                .id(1L)
                .name("Дрель")
                .description("Аккумуляторная дрель")
                .available(true)
                .build();

        when(itemService.createItem(any(ItemDto.class), eq(1L)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1L)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Дрель")))
                .andExpect(jsonPath("$.description", is("Аккумуляторная дрель")))
                .andExpect(jsonPath("$.available", is(true)));
    }

    @Test
    void updateItem_shouldReturnUpdatedItem() throws Exception {
        ItemDto updateDto = ItemDto.builder()
                .name("Обновленная дрель")
                .available(false)
                .build();

        ItemDto responseDto = ItemDto.builder()
                .id(1L)
                .name("Обновленная дрель")
                .description("Старое описание")
                .available(false)
                .build();

        when(itemService.updateItem(eq(1L), any(ItemDto.class), eq(1L)))
                .thenReturn(responseDto);

        mockMvc.perform(patch("/items/1")
                        .header(USER_ID_HEADER, 1L)
                        .content(objectMapper.writeValueAsString(updateDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Обновленная дрель")))
                .andExpect(jsonPath("$.available", is(false)));
    }

    @Test
    void getItemById_shouldReturnItem() throws Exception {
        ItemWithBookingsDto responseDto = ItemWithBookingsDto.builder()
                .id(1L)
                .name("Дрель")
                .description("Описание")
                .available(true)
                .build();

        when(itemService.getItemById(eq(1L), eq(1L)))
                .thenReturn(responseDto);

        mockMvc.perform(get("/items/1")
                        .header(USER_ID_HEADER, 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Дрель")));
    }

    @Test
    void getItemsByOwner_shouldReturnItemsList() throws Exception {
        ItemForOwnerDto item1 = ItemForOwnerDto.builder()
                .id(1L)
                .name("Дрель")
                .available(true)
                .build();

        ItemForOwnerDto item2 = ItemForOwnerDto.builder()
                .id(2L)
                .name("Молоток")
                .available(false)
                .build();

        when(itemService.getItemsByOwner(eq(1L)))
                .thenReturn(List.of(item1, item2));

        mockMvc.perform(get("/items")
                        .header(USER_ID_HEADER, 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Дрель")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("Молоток")));
    }

    @Test
    void searchItems_shouldReturnSearchResults() throws Exception {
        ItemDto item1 = ItemDto.builder()
                .id(1L)
                .name("Аккумуляторная дрель")
                .available(true)
                .build();

        when(itemService.searchItems(eq("дрель"), eq(1L)))
                .thenReturn(List.of(item1));

        mockMvc.perform(get("/items/search")
                        .header(USER_ID_HEADER, 1L)
                        .param("text", "дрель")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Аккумуляторная дрель")));
    }

    @Test
    void addComment_shouldReturnCreatedComment() throws Exception {
        CommentDto requestDto = CommentDto.builder()
                .text("Отличная дрель!")
                .build();

        CommentDto responseDto = CommentDto.builder()
                .id(1L)
                .text("Отличная дрель!")
                .authorName("Иван")
                .created(LocalDateTime.now())
                .build();

        when(itemService.addComment(eq(1L), eq(1L), any(CommentDto.class)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/items/1/comment")
                        .header(USER_ID_HEADER, 1L)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.text", is("Отличная дрель!")))
                .andExpect(jsonPath("$.authorName", is("Иван")));
    }
}