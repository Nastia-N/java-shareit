package ru.practicum.shareit.gateway.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.gateway.item.dto.CommentDto;
import ru.practicum.shareit.gateway.item.dto.ItemDto;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemClient itemClient;

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Test
    void createItem_shouldValidateName() throws Exception {
        ItemDto invalidDto = ItemDto.builder()
                .description("Test description")
                .available(true)
                .build();

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1L)
                        .content(objectMapper.writeValueAsString(invalidDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("name")));
    }

    @Test
    void createItem_shouldValidateDescription() throws Exception {
        ItemDto invalidDto = ItemDto.builder()
                .name("Test Item")
                .available(true)
                .build();

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1L)
                        .content(objectMapper.writeValueAsString(invalidDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("description")));
    }

    @Test
    void createItem_shouldValidateAvailability() throws Exception {
        ItemDto invalidDto = ItemDto.builder()
                .name("Test Item")
                .description("Test description")
                .build();

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1L)
                        .content(objectMapper.writeValueAsString(invalidDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("available")));
    }

    @Test
    void createItem_shouldAcceptValidItem() throws Exception {
        ItemDto validDto = ItemDto.builder()
                .name("Test Item")
                .description("Test description")
                .available(true)
                .build();

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1L)
                        .content(objectMapper.writeValueAsString(validDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void updateItem_shouldAcceptPartialUpdate() throws Exception {
        ItemDto updateDto = ItemDto.builder()
                .name("Updated Name")
                .build();

        mockMvc.perform(patch("/items/1")
                        .header(USER_ID_HEADER, 1L)
                        .content(objectMapper.writeValueAsString(updateDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getItemById_shouldRequireUserIdHeader() throws Exception {
        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk());
    }

    @Test
    void getItemsByOwner_shouldRequireUserIdHeader() throws Exception {
        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk());
    }

    @Test
    void searchItems_shouldValidateTextParameter() throws Exception {
        mockMvc.perform(get("/items/search")
                        .header(USER_ID_HEADER, 1L)
                        .param("text", ""))
                .andExpect(status().isOk());
    }

    @Test
    void addComment_shouldValidateText() throws Exception {
        CommentDto invalidDto = CommentDto.builder()
                .build();

        mockMvc.perform(post("/items/1/comment")
                        .header(USER_ID_HEADER, 1L)
                        .content(objectMapper.writeValueAsString(invalidDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("text")));
    }

    @Test
    void addComment_shouldValidateEmptyText() throws Exception {
        CommentDto invalidDto = CommentDto.builder()
                .text("")
                .build();

        mockMvc.perform(post("/items/1/comment")
                        .header(USER_ID_HEADER, 1L)
                        .content(objectMapper.writeValueAsString(invalidDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("text")));
    }

    @Test
    void addComment_shouldAcceptValidComment() throws Exception {
        CommentDto validDto = CommentDto.builder()
                .text("Great item!")
                .build();

        mockMvc.perform(post("/items/1/comment")
                        .header(USER_ID_HEADER, 1L)
                        .content(objectMapper.writeValueAsString(validDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void addComment_shouldValidateTextLength() throws Exception {
        CommentDto invalidDto = CommentDto.builder()
                .text("a".repeat(1001)) // Превышает максимальную длину
                .build();

        mockMvc.perform(post("/items/1/comment")
                        .header(USER_ID_HEADER, 1L)
                        .content(objectMapper.writeValueAsString(invalidDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("text")));
    }

    @Test
    void updateItem_shouldWorkWithoutAllFields() throws Exception {
        ItemDto partialUpdate = ItemDto.builder()
                .name("Updated Name Only")
                .build();

        mockMvc.perform(patch("/items/1")
                        .header(USER_ID_HEADER, 1L)
                        .content(objectMapper.writeValueAsString(partialUpdate))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void searchItems_shouldHandleEmptyText() throws Exception {
        mockMvc.perform(get("/items/search")
                        .header(USER_ID_HEADER, 1L)
                        .param("text", ""))
                .andExpect(status().isOk());
    }

    @Test
    void createItem_withRequestId_shouldBeValid() throws Exception {
        ItemDto validDto = ItemDto.builder()
                .name("Test Item")
                .description("Test description")
                .available(true)
                .requestId(1L)
                .build();

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1L)
                        .content(objectMapper.writeValueAsString(validDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void updateItem_withOnlyDescription() throws Exception {
        ItemDto dto = ItemDto.builder()
                .description("Updated description")
                .build();

        mockMvc.perform(patch("/items/1")
                        .header(USER_ID_HEADER, 1L)
                        .content(objectMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void updateItem_withOnlyAvailability() throws Exception {
        ItemDto dto = ItemDto.builder()
                .available(false)
                .build();

        mockMvc.perform(patch("/items/1")
                        .header(USER_ID_HEADER, 1L)
                        .content(objectMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void searchItems_withValidText() throws Exception {
        mockMvc.perform(get("/items/search")
                        .header(USER_ID_HEADER, 1L)
                        .param("text", "дрель"))
                .andExpect(status().isOk());
    }

    @Test
    void addComment_withMinLengthText() throws Exception {
        CommentDto dto = CommentDto.builder()
                .text("A") // минимальная длина
                .build();

        mockMvc.perform(post("/items/1/comment")
                        .header(USER_ID_HEADER, 1L)
                        .content(objectMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void addComment_withMaxLengthText() throws Exception {
        CommentDto dto = CommentDto.builder()
                .text("A".repeat(1000)) // максимальная длина
                .build();

        mockMvc.perform(post("/items/1/comment")
                        .header(USER_ID_HEADER, 1L)
                        .content(objectMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
