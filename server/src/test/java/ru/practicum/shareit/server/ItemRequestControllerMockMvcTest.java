package ru.practicum.shareit.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.server.request.ItemRequestController;
import ru.practicum.shareit.server.request.dto.ItemRequestDto;
import ru.practicum.shareit.server.request.service.ItemRequestService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService itemRequestService;

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Test
    void createRequest_shouldReturnCreatedRequest() throws Exception {
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("Нужна дрель")
                .build();

        ItemRequestDto responseDto = ItemRequestDto.builder()
                .id(1L)
                .description("Нужна дрель")
                .requestorId(1L)
                .created(LocalDateTime.now())
                .build();

        when(itemRequestService.createRequest(any(ItemRequestDto.class), eq(1L)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/requests")
                        .header(USER_ID_HEADER, 1L)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.description", is("Нужна дрель")))
                .andExpect(jsonPath("$.requestorId", is(1)));
    }

    @Test
    void getUserRequests_shouldReturnUserRequests() throws Exception {
        // Given
        ItemRequestDto request1 = ItemRequestDto.builder()
                .id(1L)
                .description("Нужна дрель")
                .requestorId(1L)
                .created(LocalDateTime.now())
                .build();

        ItemRequestDto request2 = ItemRequestDto.builder()
                .id(2L)
                .description("Нужен молоток")
                .requestorId(1L)
                .created(LocalDateTime.now().minusDays(1))
                .build();

        when(itemRequestService.getUserRequests(eq(1L)))
                .thenReturn(List.of(request1, request2));

        // When & Then
        mockMvc.perform(get("/requests")
                        .header(USER_ID_HEADER, 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].description", is("Нужна дрель")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].description", is("Нужен молоток")));
    }

    @Test
    void getAllRequests_shouldReturnOtherUsersRequests() throws Exception {
        // Given
        ItemRequestDto request = ItemRequestDto.builder()
                .id(1L)
                .description("Нужна дрель")
                .requestorId(2L)
                .created(LocalDateTime.now())
                .build();

        when(itemRequestService.getAllRequests(eq(1L), eq(0), eq(10)))
                .thenReturn(List.of(request));

        // When & Then
        mockMvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, 1L)
                        .param("from", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].description", is("Нужна дрель")))
                .andExpect(jsonPath("$[0].requestorId", is(2)));
    }

    @Test
    void getRequestById_shouldReturnRequest() throws Exception {
        // Given
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .id(1L)
                .description("Нужна дрель")
                .requestorId(2L)
                .created(LocalDateTime.now())
                .build();

        when(itemRequestService.getRequestById(eq(1L), eq(1L)))
                .thenReturn(requestDto);

        // When & Then
        mockMvc.perform(get("/requests/1")
                        .header(USER_ID_HEADER, 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.description", is("Нужна дрель")));
    }
}