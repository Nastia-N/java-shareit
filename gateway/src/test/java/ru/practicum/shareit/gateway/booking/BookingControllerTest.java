package ru.practicum.shareit.gateway.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.gateway.booking.dto.BookingDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

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

    @Test
    void createBooking_shouldValidateStartDate() throws Exception {
        BookingDto invalidDto = BookingDto.builder()
                .itemId(1L)
                .end(LocalDateTime.now().plusDays(3))
                .start(LocalDateTime.now().minusDays(1)) // Прошлая дата
                .build();

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .content(objectMapper.writeValueAsString(invalidDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("start")));
    }

    @Test
    void createBooking_shouldValidateEndDate() throws Exception {
        BookingDto invalidDto = BookingDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now()) // Текущая дата, должна быть в будущем
                .build();

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .content(objectMapper.writeValueAsString(invalidDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("end")));
    }

    @Test
    void approveBooking_shouldRequireUserIdHeader() throws Exception {
        mockMvc.perform(patch("/bookings/1")
                        .param("approved", "true"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBookingById_shouldRequireUserIdHeader() throws Exception {
        mockMvc.perform(get("/bookings/1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserBookings_shouldValidateStateParameter() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .param("state", "INVALID_STATE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("Unknown state")));
    }

    @Test
    void getUserBookings_shouldValidatePagination() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .param("from", "-1")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getOwnerBookings_shouldValidateStateParameter() throws Exception {
        mockMvc.perform(get("/bookings/owner")
                        .header(USER_ID_HEADER, 1L)
                        .param("state", "INVALID_STATE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("Unknown state")));
    }

    @Test
    void createBooking_shouldValidateAllFields() throws Exception {
        // Пустой DTO
        BookingDto emptyDto = BookingDto.builder().build();

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .content(objectMapper.writeValueAsString(emptyDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void createBooking_shouldAcceptValidData() throws Exception {
        BookingDto validDto = BookingDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(3))
                .build();

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .content(objectMapper.writeValueAsString(validDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void approveBooking_shouldRequireApprovedParam() throws Exception {
        mockMvc.perform(patch("/bookings/1")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserBookings_shouldUseDefaultParameters() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk());
    }

    @Test
    void getUserBookings_shouldHandleValidState() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .param("state", "WAITING"))
                .andExpect(status().isOk());
    }

    @Test
    void getOwnerBookings_shouldUseDefaultParameters() throws Exception {
        mockMvc.perform(get("/bookings/owner")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk());
    }
}
