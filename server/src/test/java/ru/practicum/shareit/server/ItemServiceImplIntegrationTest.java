package ru.practicum.shareit.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.server.exception.NotFoundException;
import ru.practicum.shareit.server.item.ItemRepository;
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
class ItemServiceImplIntegrationTest {

    @Autowired
    private ItemServiceImpl itemService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    private User owner;
    private ItemRequest request;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .name("Owner Name")
                .email("owner@test.com")
                .build();
        owner = userRepository.save(owner);

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
    }

    @Test
    void createItem_shouldCreateItemSuccessfully() {
        ItemDto itemDto = ItemDto.builder()
                .name("Дрель")
                .description("Аккумуляторная дрель")
                .available(true)
                .requestId(request.getId())
                .build();

        ItemDto createdItem = itemService.createItem(itemDto, owner.getId());

        assertNotNull(createdItem.getId());
        assertEquals("Дрель", createdItem.getName());
        assertEquals("Аккумуляторная дрель", createdItem.getDescription());
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

        assertThrows(NotFoundException.class,
                () -> itemService.createItem(itemDto, 999L));
    }

    @Test
    void getItemById_shouldReturnItemWithBookingsForOwner() {
        Item item = Item.builder()
                .name("Дрель")
                .description("Аккумуляторная дрель")
                .available(true)
                .owner(owner)
                .build();
        item = itemRepository.save(item);

        ItemWithBookingsDto result = itemService.getItemById(item.getId(), owner.getId());

        assertNotNull(result);
        assertEquals(item.getId(), result.getId());
        assertEquals("Дрель", result.getName());
        assertEquals("Аккумуляторная дрель", result.getDescription());
        assertTrue(result.getAvailable());
    }

    @Test
    void getItemById_shouldReturnItemWithoutBookingsForOtherUser() {
        Item item = Item.builder()
                .name("Дрель")
                .description("Аккумуляторная дрель")
                .available(true)
                .owner(owner)
                .build();
        item = itemRepository.save(item);

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
        Item item1 = Item.builder()
                .name("Дрель")
                .description("Аккумуляторная дрель")
                .available(true)
                .owner(owner)
                .build();

        Item item2 = Item.builder()
                .name("Отвертка")
                .description("Набор отверток")
                .available(false)
                .owner(owner)
                .build();

        itemRepository.save(item1);
        itemRepository.save(item2);

        List<ItemForOwnerDto> items = itemService.getItemsByOwner(owner.getId());

        assertEquals(2, items.size());
        assertTrue(items.stream().anyMatch(i -> i.getName().equals("Дрель")));
        assertTrue(items.stream().anyMatch(i -> i.getName().equals("Отвертка")));
    }

    @Test
    void searchItems_shouldReturnAvailableItemsMatchingText() {
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
        assertEquals("Аккумуляторная ДРЕЛЬ Bosch", results.get(0).getName());
        assertTrue(results.get(0).getAvailable());
    }

    @Test
    void searchItems_shouldReturnEmptyListForBlankText() {
        List<ItemDto> results = itemService.searchItems("", owner.getId());

        assertTrue(results.isEmpty());
    }

    @Test
    void updateItem_shouldUpdateOnlyProvidedFields() {
        Item existingItem = Item.builder()
                .name("Старое название")
                .description("Старое описание")
                .available(true)
                .owner(owner)
                .build();
        existingItem = itemRepository.save(existingItem);

        ItemDto updateDto = ItemDto.builder()
                .name("Новое название")
                .available(false)
                .build();

        ItemDto updatedItem = itemService.updateItem(existingItem.getId(), updateDto, owner.getId());

        assertEquals("Новое название", updatedItem.getName());
        assertEquals("Старое описание", updatedItem.getDescription()); // Не изменилось
        assertFalse(updatedItem.getAvailable());
    }

    @Test
    void updateItem_shouldThrowExceptionWhenNotOwner() {
        Item existingItem = Item.builder()
                .name("Дрель")
                .description("Описание")
                .available(true)
                .owner(owner)
                .build();
        existingItem = itemRepository.save(existingItem);

        User otherUser = User.builder()
                .name("Other User")
                .email("other@test.com")
                .build();
        otherUser = userRepository.save(otherUser);

        ItemDto updateDto = ItemDto.builder()
                .name("Новое название")
                .build();

        Item finalExistingItem = existingItem;
        User finalOtherUser = otherUser;
        assertThrows(NotFoundException.class,
                () -> itemService.updateItem(finalExistingItem.getId(), updateDto, finalOtherUser.getId()));
    }
}
