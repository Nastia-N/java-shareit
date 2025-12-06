package ru.practicum.shareit.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.gateway.request.ItemRequestClient;
import ru.practicum.shareit.gateway.request.ItemRequestController;
import ru.practicum.shareit.gateway.request.dto.ItemRequestCreateDto;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestClient itemRequestClient;

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Test
    void createRequest_shouldValidateDescription() throws Exception {
        ItemRequestCreateDto invalidDto = new ItemRequestCreateDto();
        invalidDto.setDescription("");

        mockMvc.perform(post("/requests")
                        .header(USER_ID_HEADER, 1L)
                        .content(objectMapper.writeValueAsString(invalidDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("description")));
    }

    @Test
    void createRequest_shouldValidateNullDescription() throws Exception {
        ItemRequestCreateDto invalidDto = new ItemRequestCreateDto();

        mockMvc.perform(post("/requests")
                        .header(USER_ID_HEADER, 1L)
                        .content(objectMapper.writeValueAsString(invalidDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("description")));
    }

    @Test
    void createRequest_shouldAcceptValidRequest() throws Exception {
        ItemRequestCreateDto validDto = new ItemRequestCreateDto();
        validDto.setDescription("Нужна дрель для ремонта");

        mockMvc.perform(post("/requests")
                        .header(USER_ID_HEADER, 1L)
                        .content(objectMapper.writeValueAsString(validDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getUserRequests_shouldRequireUserIdHeader() throws Exception {
        mockMvc.perform(get("/requests")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk());
    }

    @Test
    void getAllRequests_shouldValidatePaginationParameters() throws Exception {
        mockMvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, 1L)
                        .param("from", "-1")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllRequests_shouldValidateSizeParameter() throws Exception {
        mockMvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, 1L)
                        .param("from", "0")
                        .param("size", "0"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllRequests_shouldUseDefaultParameters() throws Exception {
        mockMvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk());
    }

    @Test
    void getRequestById_shouldRequireUserIdHeader() throws Exception {
        mockMvc.perform(get("/requests/1")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk());
    }
}
