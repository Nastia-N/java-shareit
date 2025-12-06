package ru.practicum.shareit.server.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.server.booking.BookingMapper;
import ru.practicum.shareit.server.booking.model.Booking;
import ru.practicum.shareit.server.booking.BookingRepository;
import ru.practicum.shareit.server.booking.model.BookingState;
import ru.practicum.shareit.server.booking.model.BookingStatus;
import ru.practicum.shareit.server.booking.dto.BookingDto;
import ru.practicum.shareit.server.exception.NotFoundException;
import ru.practicum.shareit.server.exception.ValidationException;
import ru.practicum.shareit.server.item.ItemRepository;
import ru.practicum.shareit.server.item.model.Item;
import ru.practicum.shareit.server.user.UserRepository;
import ru.practicum.shareit.server.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingMapper bookingMapper;

    @Override
    public BookingDto createBooking(BookingDto bookingDto, Long bookerId) {
        User booker = userRepository.findById(bookerId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        if (!item.getAvailable()) {
            throw new ValidationException("Вещь недоступна для бронирования");
        }

        if (item.getOwner().getId().equals(bookerId)) {
            throw new ValidationException("Владелец не может бронировать свою вещь");
        }

        if (bookingDto.getEnd().isBefore(bookingDto.getStart()) ||
                bookingDto.getEnd().isEqual(bookingDto.getStart())) {
            throw new ValidationException("Дата окончания бронирования должна быть позже даты начала");
        }

        Booking booking = Booking.builder()
                .start(bookingDto.getStart())
                .end(bookingDto.getEnd())
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();

        Booking savedBooking = bookingRepository.save(booking);
        return bookingMapper.toDto(savedBooking);
    }

    @Override
    public BookingDto approveBooking(Long bookingId, Long ownerId, boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new ValidationException("Только владелец вещи может подтвердить бронирование");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Статус бронирования не может быть изменен");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking updatedBooking = bookingRepository.save(booking);
        return bookingMapper.toDto(updatedBooking);
    }

    @Override
    public BookingDto getBookingById(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findByIdWithRelations(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        if (!booking.getBooker().getId().equals(userId) &&
                !booking.getItem().getOwner().getId().equals(userId)) {
            throw new SecurityException("Недостаточно прав для выполнения операции");
        }

        return bookingMapper.toDto(booking);
    }

    @Override
    public List<BookingDto> getUserBookings(Long userId, BookingState state, String sortBy, String direction) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = switch (state) {
            case ALL -> bookingRepository.findByBookerId(userId, sort);
            case CURRENT -> bookingRepository.findCurrentByBookerId(userId, now, sort);
            case PAST -> bookingRepository.findByBookerIdAndEndBefore(userId, now, sort);
            case FUTURE -> bookingRepository.findByBookerIdAndStartAfter(userId, now, sort);
            case WAITING, REJECTED -> {
                BookingStatus status = BookingStatus.valueOf(state.name());
                yield bookingRepository.findByBookerIdAndStatus(userId, status, sort);
            }
            default -> throw new IllegalArgumentException("Неизвестный статус: " + state);
        };

        return bookings.stream()
                .map(bookingMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getOwnerBookings(Long ownerId, BookingState state, String sortBy, String direction) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = switch (state) {
            case ALL -> bookingRepository.findByItemOwnerId(ownerId, sort);
            case CURRENT -> bookingRepository.findCurrentByOwnerId(ownerId, now, sort);
            case PAST -> bookingRepository.findByItemOwnerIdAndEndBefore(ownerId, now, sort);
            case FUTURE -> bookingRepository.findByItemOwnerIdAndStartAfter(ownerId, now, sort);
            case WAITING, REJECTED -> {
                BookingStatus status = BookingStatus.valueOf(state.name());
                yield bookingRepository.findByItemOwnerIdAndStatus(ownerId, status, sort);
            }
            default -> throw new IllegalArgumentException("Неизвестный статус: " + state);
        };

        return bookings.stream()
                .map(bookingMapper::toDto)
                .collect(Collectors.toList());
    }
}