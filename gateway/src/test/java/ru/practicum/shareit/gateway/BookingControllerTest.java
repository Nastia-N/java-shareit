package ru.practicum.shareit.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.gateway.booking.BookingClient;
import ru.practicum.shareit.gateway.booking.BookingController;
import ru.practicum.shareit.gateway.booking.dto.BookingDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingClient bookingClient;

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Test
    void createBooking_shouldValidateItemId() throws Exception {
        BookingDto invalidDto = BookingDto.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(3))
                .build();

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .content(objectMapper.writeValueAsString(invalidDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("itemId")));
    }
}
