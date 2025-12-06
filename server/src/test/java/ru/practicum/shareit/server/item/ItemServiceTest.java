package ru.practicum.shareit.server.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.server.booking.BookingRepository;
import ru.practicum.shareit.server.booking.model.Booking;
import ru.practicum.shareit.server.booking.model.BookingStatus;
import ru.practicum.shareit.server.exception.NotFoundException;
import ru.practicum.shareit.server.exception.ValidationException;
import ru.practicum.shareit.server.item.dto.CommentDto;
import ru.practicum.shareit.server.item.dto.ItemDto;
import ru.practicum.shareit.server.item.dto.ItemForOwnerDto;
import ru.practicum.shareit.server.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.server.item.model.Item;
import ru.practicum.shareit.server.item.service.ItemServiceImpl;
import ru.practicum.shareit.server.request.ItemRequestRepository;
import ru.practicum.shareit.server.request.model.ItemRequest;
import ru.practicum.shareit.server.user.UserRepository;
import ru.practicum.shareit.server.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class ItemServiceTest {

    @Autowired
    private ItemServiceImpl itemService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private User owner;
    private User booker;
    private ItemRequest request;
    private Item item;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        itemRequestRepository.deleteAll();
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

        User requester = User.builder()
                .name("Requester Name")
                .email("requester@test.com")
                .build();
        requester = userRepository.save(requester);

        request = ItemRequest.builder()
                .description("Need a drill")
                .requestor(requester)
                .created(LocalDateTime.now())
                .build();
        request = itemRequestRepository.save(request);

        item = Item.builder()
                .name("Дрель")
                .description("Аккумуляторная дрель")
                .available(true)
                .owner(owner)
                .build();
        item = itemRepository.save(item);
    }

    @Test
    void createItem_shouldCreateItemSuccessfully() {
        ItemDto itemDto = ItemDto.builder()
                .name("Новая дрель")
                .description("Новая аккумуляторная дрель")
                .available(true)
                .requestId(request.getId())
                .build();

        ItemDto createdItem = itemService.createItem(itemDto, owner.getId());

        assertNotNull(createdItem.getId());
        assertEquals("Новая дрель", createdItem.getName());
        assertEquals("Новая аккумуляторная дрель", createdItem.getDescription());
        assertTrue(createdItem.getAvailable());
        assertEquals(request.getId(), createdItem.getRequestId());
    }

    @Test
    void createItem_shouldThrowExceptionWhenOwnerNotFound() {
        ItemDto itemDto = ItemDto.builder()
                .name("Дрель")
                .description("Аккумуляторная дрель")
                .available(true)
                .build();

        Long nonExistentOwnerId = 999L;

        assertThrows(NotFoundException.class,
                () -> itemService.createItem(itemDto, nonExistentOwnerId));
    }

    @Test
    void getItemById_shouldReturnItemWithBookingsForOwner() {
        ItemWithBookingsDto result = itemService.getItemById(item.getId(), owner.getId());

        assertNotNull(result);
        assertEquals(item.getId(), result.getId());
        assertEquals("Дрель", result.getName());
        assertEquals("Аккумуляторная дрель", result.getDescription());
        assertTrue(result.getAvailable());
    }

    @Test
    void getItemById_shouldReturnItemWithoutBookingsForOtherUser() {
        User otherUser = User.builder()
                .name("Other User")
                .email("other@test.com")
                .build();
        otherUser = userRepository.save(otherUser);

        ItemWithBookingsDto result = itemService.getItemById(item.getId(), otherUser.getId());

        assertNotNull(result);
        assertEquals(item.getId(), result.getId());
        assertNull(result.getLastBooking());
        assertNull(result.getNextBooking());
    }

    @Test
    void getItemsByOwner_shouldReturnAllOwnerItems() {
        Item item2 = Item.builder()
                .name("Отвертка")
                .description("Набор отверток")
                .available(false)
                .owner(owner)
                .build();
        itemRepository.save(item2);

        List<ItemForOwnerDto> items = itemService.getItemsByOwner(owner.getId());

        assertEquals(2, items.size());
        assertTrue(items.stream().anyMatch(i -> i.getName().equals("Дрель")));
        assertTrue(items.stream().anyMatch(i -> i.getName().equals("Отвертка")));
    }

    @Test
    void searchItems_shouldReturnAvailableItemsMatchingText() {
        itemRepository.deleteAll();

        Item availableItem = Item.builder()
                .name("Аккумуляторная ДРЕЛЬ Bosch")
                .description("Мощная дрель")
                .available(true)
                .owner(owner)
                .build();

        Item unavailableItem = Item.builder()
                .name("Дрель")
                .description("Старая дрель")
                .available(false)
                .owner(owner)
                .build();

        Item otherItem = Item.builder()
                .name("Молоток")
                .description("Строительный молоток")
                .available(true)
                .owner(owner)
                .build();

        itemRepository.save(availableItem);
        itemRepository.save(unavailableItem);
        itemRepository.save(otherItem);

        List<ItemDto> results = itemService.searchItems("дрель", owner.getId());

        assertEquals(1, results.size());
        assertEquals("Аккумуляторная ДРЕЛЬ Bosch", results.getFirst().getName());
        assertTrue(results.getFirst().getAvailable());
    }

    @Test
    void searchItems_shouldReturnEmptyListForBlankText() {
        List<ItemDto> results = itemService.searchItems("", owner.getId());

        assertTrue(results.isEmpty());
    }

    @Test
    void updateItem_shouldUpdateOnlyProvidedFields() {
        ItemDto updateDto = ItemDto.builder()
                .name("Новое название")
                .available(false)
                .build();

        ItemDto updatedItem = itemService.updateItem(item.getId(), updateDto, owner.getId());

        assertEquals("Новое название", updatedItem.getName());
        assertEquals("Аккумуляторная дрель", updatedItem.getDescription());
        assertFalse(updatedItem.getAvailable());
    }

    @Test
    void updateItem_shouldThrowExceptionWhenNotOwner() {
        User otherUser = User.builder()
                .name("Other User")
                .email("other@test.com")
                .build();
        otherUser = userRepository.save(otherUser);

        ItemDto updateDto = ItemDto.builder()
                .name("Новое название")
                .build();

        Long itemId = item.getId();
        Long otherUserId = otherUser.getId();

        assertThrows(NotFoundException.class,
                () -> itemService.updateItem(itemId, updateDto, otherUserId));
    }

    @Test
    void addComment_shouldThrowWhenUserNotBookedItem() {
        User otherUser = User.builder()
                .name("Other User")
                .email("other@test.com")
                .build();
        otherUser = userRepository.save(otherUser);

        CommentDto commentDto = CommentDto.builder()
                .text("Комментарий")
                .build();

        Long itemId = item.getId();
        Long otherUserId = otherUser.getId();

        assertThrows(ValidationException.class, () -> {
            itemService.addComment(itemId, otherUserId, commentDto);
        });
    }

    @Test
    void addComment_shouldCreateCommentWhenUserBookedItem() {
        LocalDateTime start = LocalDateTime.now().minusDays(3);
        LocalDateTime end = LocalDateTime.now().minusDays(1);

        Booking booking = Booking.builder()
                .start(start)
                .end(end)
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build();
        bookingRepository.save(booking);

        CommentDto commentDto = CommentDto.builder()
                .text("Отличная вещь!")
                .build();

        CommentDto result = itemService.addComment(item.getId(), booker.getId(), commentDto);

        assertNotNull(result.getId());
        assertEquals("Отличная вещь!", result.getText());
        assertEquals(booker.getName(), result.getAuthorName());
        assertNotNull(result.getCreated());
    }

    @Test
    void addComment_shouldThrowWhenBookingNotFinished() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        Booking booking = Booking.builder()
                .start(start)
                .end(end)
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build();
        bookingRepository.save(booking);

        CommentDto commentDto = CommentDto.builder()
                .text("Комментарий")
                .build();

        Long itemId = item.getId();
        Long bookerId = booker.getId();

        assertThrows(ValidationException.class, () -> {
            itemService.addComment(itemId, bookerId, commentDto);
        });
    }

    @Test
    void searchItems_shouldReturnEmptyListWhenNoMatches() {
        List<ItemDto> results = itemService.searchItems("несуществующий текст", owner.getId());

        assertTrue(results.isEmpty());
    }

    @Test
    void searchItems_shouldIgnoreCase() {
        itemRepository.deleteAll();

        Item item1 = Item.builder()
                .name("ДРЕЛЬ")
                .description("Инструмент")
                .available(true)
                .owner(owner)
                .build();

        Item item2 = Item.builder()
                .name("дрель")
                .description("Электрическая")
                .available(true)
                .owner(owner)
                .build();

        itemRepository.save(item1);
        itemRepository.save(item2);

        List<ItemDto> results = itemService.searchItems("Дрель", owner.getId());

        assertEquals(2, results.size());
    }

    @Test
    void getItemById_shouldThrowWhenItemNotFound() {
        Long nonExistentItemId = 999L;
        Long userId = owner.getId();

        assertThrows(NotFoundException.class, () -> {
            itemService.getItemById(nonExistentItemId, userId);
        });
    }

    @Test
    void updateItem_shouldThrowWhenItemNotFound() {
        ItemDto updateDto = ItemDto.builder()
                .name("Новое название")
                .build();

        Long nonExistentItemId = 999L;
        Long ownerId = owner.getId();

        assertThrows(NotFoundException.class, () -> {
            itemService.updateItem(nonExistentItemId, updateDto, ownerId);
        });
    }
}
