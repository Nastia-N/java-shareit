package ru.practicum.shareit.server.booking;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.server.booking.dto.BookingDto;
import ru.practicum.shareit.server.booking.model.Booking;

@Component
public class BookingMapper {

    public BookingDto toDto(Booking booking) {
        return BookingDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .booker(new BookingDto.BookerDto(booking.getBooker().getId(), booking.getBooker().getName()))
                .item(new BookingDto.ItemDto(booking.getItem().getId(), booking.getItem().getName()))
                .build();
    }
}