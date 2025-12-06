package ru.practicum.shareit.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.server.booking.dto.BookingDto;
import ru.practicum.shareit.server.booking.model.BookingStatus;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@ActiveProfiles("test")
class BookingDtoJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldSerializeBookingDto() throws Exception {
        BookingDto.BookerDto booker = new BookingDto.BookerDto(1L, "Иван");
        BookingDto.ItemDto item = new BookingDto.ItemDto(1L, "Дрель");

        LocalDateTime start = LocalDateTime.of(2024, 1, 15, 10, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 20, 10, 0);

        BookingDto bookingDto = BookingDto.builder()
                .id(1L)
                .start(start)
                .end(end)
                .status(BookingStatus.WAITING)
                .booker(booker)
                .item(item)
                .itemId(1L)
                .bookerId(1L)
                .build();

        String json = objectMapper.writeValueAsString(bookingDto);

        assertThat(json).contains("\"id\":1");
        assertThat(json).contains("\"start\":\"2024-01-15T10:00:00\"");
        assertThat(json).contains("\"end\":\"2024-01-20T10:00:00\"");
        assertThat(json).contains("\"status\":\"WAITING\"");
        assertThat(json).contains("\"itemId\":1");
        assertThat(json).contains("\"bookerId\":1");
    }

    @Test
    void shouldDeserializeBookingDto() throws Exception {
        String json = """
                {
                  "id": 1,
                  "start": "2024-01-15T10:00:00",
                  "end": "2024-01-20T10:00:00",
                  "status": "WAITING",
                  "itemId": 1,
                  "bookerId": 1
                }""";

        BookingDto bookingDto = objectMapper.readValue(json, BookingDto.class);

        assertThat(bookingDto.getId()).isEqualTo(1L);
        assertThat(bookingDto.getStart()).isEqualTo(LocalDateTime.of(2024, 1, 15, 10, 0));
        assertThat(bookingDto.getEnd()).isEqualTo(LocalDateTime.of(2024, 1, 20, 10, 0));
        assertThat(bookingDto.getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(bookingDto.getItemId()).isEqualTo(1L);
        assertThat(bookingDto.getBookerId()).isEqualTo(1L);
    }

    @Test
    void shouldHandleNullFields() throws Exception {
        String json = "{\n" +
                "  \"itemId\": 1,\n" +
                "  \"start\": \"2024-01-15T10:00:00\",\n" +
                "  \"end\": \"2024-01-20T10:00:00\"\n" +
                "}";

        BookingDto bookingDto = objectMapper.readValue(json, BookingDto.class);

        assertThat(bookingDto.getItemId()).isEqualTo(1L);
        assertThat(bookingDto.getStart()).isNotNull();
        assertThat(bookingDto.getEnd()).isNotNull();
        assertThat(bookingDto.getId()).isNull();
        assertThat(bookingDto.getStatus()).isNull();
        assertThat(bookingDto.getBooker()).isNull();
        assertThat(bookingDto.getItem()).isNull();
        assertThat(bookingDto.getBookerId()).isNull();
    }
}
