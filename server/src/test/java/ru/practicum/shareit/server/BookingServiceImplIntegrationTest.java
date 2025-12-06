package ru.practicum.shareit.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.server.booking.BookingRepository;
import ru.practicum.shareit.server.booking.dto.BookingDto;
import ru.practicum.shareit.server.booking.model.Booking;
import ru.practicum.shareit.server.booking.model.BookingState;
import ru.practicum.shareit.server.booking.model.BookingStatus;
import ru.practicum.shareit.server.booking.service.BookingServiceImpl;
import ru.practicum.shareit.server.exception.NotFoundException;
import ru.practicum.shareit.server.exception.ValidationException;
import ru.practicum.shareit.server.item.ItemRepository;
import ru.practicum.shareit.server.item.model.Item;
import ru.practicum.shareit.server.user.UserRepository;
import ru.practicum.shareit.server.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class BookingServiceImplIntegrationTest {

    @Autowired
    private BookingServiceImpl bookingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private User owner;
    private User booker;
    private Item item;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();

        owner = User.builder()
                .name("Owner Name")
                .email("owner@test.com")
                .build();
        owner = userRepository.save(owner);

        booker = User.builder()
                .name("Booker Name")
                .email("booker@test.com")
                .build();
        booker = userRepository.save(booker);

        item = Item.builder()
                .name("Дрель")
                .description("Аккумуляторная дрель")
                .available(true)
                .owner(owner)
                .build();
        item = itemRepository.save(item);
    }

    @Test
    void createBooking_shouldThrowExceptionWhenItemUnavailable() {
        item.setAvailable(false);
        itemRepository.save(item);

        LocalDateTime start = LocalDateTime.now().plusDays(1).withNano(0);
        LocalDateTime end = LocalDateTime.now().plusDays(3).withNano(0);

        BookingDto bookingDto = BookingDto.builder()
                .itemId(item.getId())
                .start(start)
                .end(end)
                .build();

        assertThrows(ValidationException.class,
                () -> bookingService.createBooking(bookingDto, booker.getId()));
    }

    @Test
    void createBooking_shouldThrowExceptionWhenOwnerBooksOwnItem() {
        LocalDateTime start = LocalDateTime.now().plusDays(1).withNano(0);
        LocalDateTime end = LocalDateTime.now().plusDays(3).withNano(0);

        BookingDto bookingDto = BookingDto.builder()
                .itemId(item.getId())
                .start(start)
                .end(end)
                .build();

        assertThrows(ValidationException.class,
                () -> bookingService.createBooking(bookingDto, owner.getId()));
    }

    @Test
    void createBooking_shouldThrowExceptionWhenEndBeforeStart() {
        LocalDateTime start = LocalDateTime.now().plusDays(3).withNano(0);
        LocalDateTime end = LocalDateTime.now().plusDays(1).withNano(0); // end раньше start!

        BookingDto bookingDto = BookingDto.builder()
                .itemId(item.getId())
                .start(start)
                .end(end)
                .build();

        assertThrows(ValidationException.class,
                () -> bookingService.createBooking(bookingDto, booker.getId()));
    }

    @Test
    void approveBooking_shouldApproveBookingSuccessfully() {
        LocalDateTime start = LocalDateTime.now().plusDays(1).withNano(0);
        LocalDateTime end = LocalDateTime.now().plusDays(3).withNano(0);

        Booking booking = Booking.builder()
                .start(start)
                .end(end)
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();
        booking = bookingRepository.save(booking);

        BookingDto approvedBooking = bookingService.approveBooking(booking.getId(), owner.getId(), true);

        assertEquals(BookingStatus.APPROVED, approvedBooking.getStatus());
    }

    @Test
    void approveBooking_shouldRejectBookingSuccessfully() {
        LocalDateTime start = LocalDateTime.now().plusDays(1).withNano(0);
        LocalDateTime end = LocalDateTime.now().plusDays(3).withNano(0);

        Booking booking = Booking.builder()
                .start(start)
                .end(end)
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();
        booking = bookingRepository.save(booking);

        BookingDto rejectedBooking = bookingService.approveBooking(booking.getId(), owner.getId(), false);

        assertEquals(BookingStatus.REJECTED, rejectedBooking.getStatus());
    }

    @Test
    void approveBooking_shouldThrowExceptionWhenNotOwner() {
        LocalDateTime start = LocalDateTime.now().plusDays(1).withNano(0);
        LocalDateTime end = LocalDateTime.now().plusDays(3).withNano(0);

        Booking booking = Booking.builder()
                .start(start)
                .end(end)
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();
        booking = bookingRepository.save(booking);

        User otherUser = User.builder()
                .name("Other User")
                .email("other@test.com")
                .build();
        otherUser = userRepository.save(otherUser);

        Booking finalBooking = booking;
        User finalOtherUser = otherUser;
        assertThrows(ValidationException.class,
                () -> bookingService.approveBooking(finalBooking.getId(), finalOtherUser.getId(), true));
    }

    @Test
    void approveBooking_shouldThrowExceptionWhenStatusNotWaiting() {
        LocalDateTime start = LocalDateTime.now().plusDays(1).withNano(0);
        LocalDateTime end = LocalDateTime.now().plusDays(3).withNano(0);

        Booking booking = Booking.builder()
                .start(start)
                .end(end)
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build();
        booking = bookingRepository.save(booking);

        Booking finalBooking = booking;
        assertThrows(ValidationException.class,
                () -> bookingService.approveBooking(finalBooking.getId(), owner.getId(), true));
    }

    @Test
    void getBookingById_shouldReturnBookingForBooker() {
        LocalDateTime start = LocalDateTime.now().plusDays(1).withNano(0);
        LocalDateTime end = LocalDateTime.now().plusDays(3).withNano(0);

        Booking booking = Booking.builder()
                .start(start)
                .end(end)
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();
        booking = bookingRepository.save(booking);

        BookingDto result = bookingService.getBookingById(booking.getId(), booker.getId());

        assertNotNull(result);
        assertEquals(booking.getId(), result.getId());
        assertEquals(BookingStatus.WAITING, result.getStatus());
    }

    @Test
    void getBookingById_shouldReturnBookingForOwner() {
        LocalDateTime start = LocalDateTime.now().plusDays(1).withNano(0);
        LocalDateTime end = LocalDateTime.now().plusDays(3).withNano(0);

        Booking booking = Booking.builder()
                .start(start)
                .end(end)
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();
        booking = bookingRepository.save(booking);

        BookingDto result = bookingService.getBookingById(booking.getId(), owner.getId());

        assertNotNull(result);
        assertEquals(booking.getId(), result.getId());
        assertEquals(BookingStatus.WAITING, result.getStatus());
    }

    @Test
    void getBookingById_shouldThrowExceptionWhenUserNotRelated() {
        LocalDateTime start = LocalDateTime.now().plusDays(1).withNano(0);
        LocalDateTime end = LocalDateTime.now().plusDays(3).withNano(0);

        Booking booking = Booking.builder()
                .start(start)
                .end(end)
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();
        booking = bookingRepository.save(booking);

        User unrelatedUser = User.builder()
                .name("Unrelated User")
                .email("unrelated@test.com")
                .build();
        unrelatedUser = userRepository.save(unrelatedUser);

        Booking finalBooking = booking;
        User finalUnrelatedUser = unrelatedUser;
        assertThrows(SecurityException.class,
                () -> bookingService.getBookingById(finalBooking.getId(), finalUnrelatedUser.getId()));
    }

    @Test
    void getBookingById_shouldThrowExceptionWhenBookingNotFound() {
        assertThrows(NotFoundException.class,
                () -> bookingService.getBookingById(999L, booker.getId()));
    }

    @Test
    void getUserBookings_shouldReturnAllBookingsForUser() {
        LocalDateTime start1 = LocalDateTime.now().plusDays(1).withNano(0);
        LocalDateTime end1 = LocalDateTime.now().plusDays(3).withNano(0);

        LocalDateTime start2 = LocalDateTime.now().plusDays(5).withNano(0);
        LocalDateTime end2 = LocalDateTime.now().plusDays(7).withNano(0);

        Booking booking1 = Booking.builder()
                .start(start1)
                .end(end1)
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();

        Booking booking2 = Booking.builder()
                .start(start2)
                .end(end2)
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build();

        bookingRepository.save(booking1);
        bookingRepository.save(booking2);

        List<BookingDto> bookings = bookingService.getUserBookings(
                booker.getId(), BookingState.ALL, "start", "DESC");

        assertEquals(2, bookings.size());
        assertTrue(bookings.get(0).getStart().isAfter(bookings.get(1).getStart()));
    }

    @Test
    void getUserBookings_shouldReturnEmptyListWhenNoBookings() {
        List<BookingDto> bookings = bookingService.getUserBookings(
                booker.getId(), BookingState.ALL, "start", "DESC");

        assertTrue(bookings.isEmpty());
    }

    @Test
    void getOwnerBookings_shouldReturnBookingsForOwner() {
        LocalDateTime start = LocalDateTime.now().plusDays(1).withNano(0);
        LocalDateTime end = LocalDateTime.now().plusDays(3).withNano(0);

        Booking booking = Booking.builder()
                .start(start)
                .end(end)
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();
        bookingRepository.save(booking);

        List<BookingDto> bookings = bookingService.getOwnerBookings(
                owner.getId(), BookingState.ALL, "start", "DESC");

        assertEquals(1, bookings.size());
        assertEquals(booking.getId(), bookings.get(0).getId());
    }
}