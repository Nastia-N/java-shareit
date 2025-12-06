package ru.practicum.shareit.server.exception;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.practicum.shareit.server.booking.BookingController;
import ru.practicum.shareit.server.booking.dto.BookingDto;
import ru.practicum.shareit.server.booking.service.BookingService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BookingController.class)
class ErrorHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Test
    void handleNotFoundException() throws Exception {
        when(bookingService.getBookingById(anyLong(), anyLong()))
                .thenThrow(new NotFoundException("Бронирование не найдено"));

        mockMvc.perform(get("/bookings/999")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Бронирование не найдено"));
    }

    @Test
    void handleValidationException() throws Exception {
        BookingDto bookingDto = BookingDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(3))
                .build();

        when(bookingService.createBooking(any(BookingDto.class), anyLong()))
                .thenThrow(new ValidationException("Вещь недоступна для бронирования"));

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itemId\":1,\"start\":\"2024-01-15T10:00:00\",\"end\":\"2024-01-20T10:00:00\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Вещь недоступна для бронирования"));
    }

    @Test
    void handleConflictException() throws Exception {
        BookingDto bookingDto = BookingDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(3))
                .build();

        when(bookingService.createBooking(any(BookingDto.class), anyLong()))
                .thenThrow(new ConflictException("Пользователь с таким email уже существует"));

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itemId\":1,\"start\":\"2024-01-15T10:00:00\",\"end\":\"2024-01-20T10:00:00\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Пользователь с таким email уже существует"));
    }

    @Test
    void handleMethodArgumentTypeMismatchException_forBookingState() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .param("state", "INVALID"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Неизвестный статус: INVALID"));
    }

    @Test
    void handleIllegalArgumentException_withConflictMessage() throws Exception {
        BookingDto bookingDto = BookingDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(3))
                .build();

        when(bookingService.createBooking(any(BookingDto.class), anyLong()))
                .thenThrow(new IllegalArgumentException("Пользователь с таким email уже существует."));

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itemId\":1,\"start\":\"2024-01-15T10:00:00\",\"end\":\"2024-01-20T10:00:00\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Пользователь с таким email уже существует."));
    }

    @Test
    void handleSecurityException() throws Exception {
        when(bookingService.getBookingById(anyLong(), anyLong()))
                .thenThrow(new SecurityException("Недостаточно прав для выполнения операции"));

        mockMvc.perform(get("/bookings/1")
                        .header(USER_ID_HEADER, 999L))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Недостаточно прав для выполнения операции"));
    }

    @Test
    void handleNoSuchElementException() throws Exception {
        when(bookingService.getBookingById(anyLong(), anyLong()))
                .thenThrow(new java.util.NoSuchElementException("Элемент не найден"));

        mockMvc.perform(get("/bookings/999")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Элемент не найден"));
    }

    @Test
    void testBadRequestExceptionDirectly() {
        BadRequestException exception = new BadRequestException("Неверный запрос");

        assertEquals("Неверный запрос", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getClass()
                .getAnnotation(ResponseStatus.class).value());
    }

    @Test
    void handleIllegalArgumentException_withoutConflictMessage() throws Exception {
        when(bookingService.createBooking(any(BookingDto.class), anyLong()))
                .thenThrow(new IllegalArgumentException("Другая ошибка"));

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itemId\":1,\"start\":\"2024-01-15T10:00:00\",\"end\":\"2024-01-20T10:00:00\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Другая ошибка"));
    }

    @Test
    void handleGenericException() throws Exception {
        when(bookingService.getBookingById(anyLong(), anyLong()))
                .thenThrow(new RuntimeException("Неожиданная ошибка"));

        mockMvc.perform(get("/bookings/1")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal server error"));
    }

    @Test
    void testValidationExceptionQuick() {
        ErrorHandler handler = new ErrorHandler();
        assertNotNull(handler);
    }

    @Test
    void testHandleMethodArgumentTypeMismatchForState() {
        ErrorHandler errorHandler = new ErrorHandler();

        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getName()).thenReturn("state");
        when(ex.getValue()).thenReturn("INVALID");

        Map<String, String> result = errorHandler.handleMethodArgumentTypeMismatch(ex);

        assertEquals("Неизвестный статус: INVALID", result.get("error"));
    }

    @Test
    void testHandleMethodArgumentNotValidException() {
        ErrorHandler errorHandler = new ErrorHandler();
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        List<FieldError> fieldErrors = List.of(
                new FieldError("itemDto", "name", "Название не может быть пустым"),
                new FieldError("itemDto", "description", "Описание обязательно")
        );

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

        ResponseEntity<Map<String, String>> response = errorHandler.handleValidationExceptions(ex);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("name: Название не может быть пустым", response.getBody().get("error"));
    }

}
