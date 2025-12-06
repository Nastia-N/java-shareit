package ru.practicum.shareit.server.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.server.exception.NotFoundException;
import ru.practicum.shareit.server.exception.ValidationException;
import ru.practicum.shareit.server.item.ItemRepository;
import ru.practicum.shareit.server.item.model.Item;
import ru.practicum.shareit.server.request.dto.ItemRequestDto;
import ru.practicum.shareit.server.request.model.ItemRequest;
import ru.practicum.shareit.server.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.server.user.UserRepository;
import ru.practicum.shareit.server.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class ItemRequestServiceTest {

    @Autowired
    private ItemRequestServiceImpl itemRequestService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private ItemRepository itemRepository;

    private User requester;
    private User otherUser;
    private User itemOwner;

    @BeforeEach
    void setUp() {
        itemRequestRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();

        requester = User.builder()
                .name("Requester")
                .email("requester@test.com")
                .build();
        requester = userRepository.save(requester);

        otherUser = User.builder()
                .name("Other User")
                .email("other@test.com")
                .build();
        otherUser = userRepository.save(otherUser);

        itemOwner = User.builder()
                .name("Item Owner")
                .email("owner@test.com")
                .build();
        itemOwner = userRepository.save(itemOwner);
    }

    @Test
    void createRequest_shouldCreateRequestSuccessfully() {
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("Нужна дрель для ремонта")
                .build();

        ItemRequestDto createdRequest = itemRequestService.createRequest(requestDto, requester.getId());

        assertNotNull(createdRequest.getId());
        assertEquals("Нужна дрель для ремонта", createdRequest.getDescription());
        assertEquals(requester.getId(), createdRequest.getRequestorId());
        assertNotNull(createdRequest.getCreated());
    }

    @Test
    void createRequest_shouldThrowForEmptyDescription() {
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("   ")
                .build();

        Long requesterId = requester.getId();

        assertThrows(ValidationException.class, () -> {
            itemRequestService.createRequest(requestDto, requesterId);
        });
    }

    @Test
    void createRequest_shouldThrowForNullDescription() {
        ItemRequestDto nullDto = ItemRequestDto.builder().build();

        Long requesterId = requester.getId();

        assertThrows(ValidationException.class, () -> {
            itemRequestService.createRequest(nullDto, requesterId);
        });
    }

    @Test
    void getUserRequests_shouldReturnUserRequests() {
        ItemRequest request1 = ItemRequest.builder()
                .description("Request 1")
                .requestor(requester)
                .created(LocalDateTime.now())
                .build();

        ItemRequest request2 = ItemRequest.builder()
                .description("Request 2")
                .requestor(requester)
                .created(LocalDateTime.now().minusDays(1))
                .build();

        itemRequestRepository.save(request1);
        itemRequestRepository.save(request2);

        List<ItemRequestDto> requests = itemRequestService.getUserRequests(requester.getId());

        assertEquals(2, requests.size());
        assertTrue(requests.get(0).getCreated().isAfter(requests.get(1).getCreated()));
    }

    @Test
    void getUserRequests_shouldReturnEmptyListWhenNoRequests() {
        List<ItemRequestDto> requests = itemRequestService.getUserRequests(requester.getId());

        assertTrue(requests.isEmpty());
    }

    @Test
    void getAllRequests_shouldReturnOtherUsersRequests() {
        ItemRequest request = ItemRequest.builder()
                .description("Request from other user")
                .requestor(otherUser)
                .created(LocalDateTime.now())
                .build();

        itemRequestRepository.save(request);

        List<ItemRequestDto> requests = itemRequestService.getAllRequests(requester.getId(), 0, 10);

        assertEquals(1, requests.size());
        assertEquals(otherUser.getId(), requests.get(0).getRequestorId());
    }

    @Test
    void getAllRequests_shouldNotReturnCurrentUserRequests() {
        ItemRequest userRequest = ItemRequest.builder()
                .description("My request")
                .requestor(requester)
                .created(LocalDateTime.now())
                .build();

        itemRequestRepository.save(userRequest);

        List<ItemRequestDto> requests = itemRequestService.getAllRequests(requester.getId(), 0, 10);

        assertTrue(requests.isEmpty());
    }

    @Test
    void getAllRequests_shouldHandlePagination() {
        for (int i = 0; i < 15; i++) {
            ItemRequest request = ItemRequest.builder()
                    .description("Request " + i)
                    .requestor(otherUser)
                    .created(LocalDateTime.now().minusHours(i))
                    .build();
            itemRequestRepository.save(request);
        }

        List<ItemRequestDto> page1 = itemRequestService.getAllRequests(requester.getId(), 0, 5);
        List<ItemRequestDto> page2 = itemRequestService.getAllRequests(requester.getId(), 5, 5);
        List<ItemRequestDto> page3 = itemRequestService.getAllRequests(requester.getId(), 10, 5);

        assertEquals(5, page1.size());
        assertEquals(5, page2.size());
        assertEquals(5, page3.size());
    }

    @Test
    void getAllRequests_shouldThrowForInvalidPagination() {
        Long requesterId = requester.getId();

        assertThrows(ValidationException.class, () -> {
            itemRequestService.getAllRequests(requesterId, -1, 10);
        });

        assertThrows(ValidationException.class, () -> {
            itemRequestService.getAllRequests(requesterId, 0, 0);
        });

        assertThrows(ValidationException.class, () -> {
            itemRequestService.getAllRequests(requesterId, 0, -5);
        });
    }

    @Test
    void getRequestById_shouldReturnRequestWithItems() {
        ItemRequest request = ItemRequest.builder()
                .description("Нужна дрель")
                .requestor(requester)
                .created(LocalDateTime.now())
                .build();
        request = itemRequestRepository.save(request);

        Item item = Item.builder()
                .name("Дрель")
                .description("Аккумуляторная дрель")
                .available(true)
                .owner(itemOwner)
                .request(request)
                .build();
        itemRepository.save(item);

        ItemRequestDto foundRequest = itemRequestService.getRequestById(request.getId(), requester.getId());

        assertNotNull(foundRequest);
        assertEquals(request.getId(), foundRequest.getId());
        assertEquals("Нужна дрель", foundRequest.getDescription());
        assertEquals(requester.getId(), foundRequest.getRequestorId());
        assertNotNull(foundRequest.getItems());
        assertEquals(1, foundRequest.getItems().size());
        assertEquals("Дрель", foundRequest.getItems().get(0).getName());
    }

    @Test
    void getRequestById_shouldReturnRequestWithoutItems() {
        ItemRequest request = ItemRequest.builder()
                .description("Нужна дрель")
                .requestor(requester)
                .created(LocalDateTime.now())
                .build();
        request = itemRequestRepository.save(request);

        ItemRequestDto foundRequest = itemRequestService.getRequestById(request.getId(), requester.getId());

        assertNotNull(foundRequest);
        assertEquals(request.getId(), foundRequest.getId());
        assertNotNull(foundRequest.getItems());
        assertTrue(foundRequest.getItems().isEmpty());
    }

    @Test
    void getRequestById_shouldThrowWhenRequestNotFound() {
        Long nonExistentRequestId = 999L;
        Long userId = requester.getId();

        assertThrows(NotFoundException.class, () -> {
            itemRequestService.getRequestById(nonExistentRequestId, userId);
        });
    }

    @Test
    void getUserRequests_shouldReturnRequestsSortedByDateDesc() {
        ItemRequest request1 = ItemRequest.builder()
                .description("Request 1")
                .requestor(requester)
                .created(LocalDateTime.now().minusDays(2))
                .build();

        ItemRequest request2 = ItemRequest.builder()
                .description("Request 2")
                .requestor(requester)
                .created(LocalDateTime.now().minusDays(1))
                .build();

        ItemRequest request3 = ItemRequest.builder()
                .description("Request 3")
                .requestor(requester)
                .created(LocalDateTime.now())
                .build();

        itemRequestRepository.save(request1);
        itemRequestRepository.save(request2);
        itemRequestRepository.save(request3);

        List<ItemRequestDto> requests = itemRequestService.getUserRequests(requester.getId());

        assertEquals(3, requests.size());
        assertEquals("Request 3", requests.get(0).getDescription());
        assertEquals("Request 2", requests.get(1).getDescription());
        assertEquals("Request 1", requests.get(2).getDescription());
    }

    @Test
    void getAllRequests_shouldReturnRequestsSortedByDateDesc() {
        for (int i = 0; i < 3; i++) {
            ItemRequest request = ItemRequest.builder()
                    .description("Request " + i)
                    .requestor(otherUser)
                    .created(LocalDateTime.now().minusDays(i))
                    .build();
            itemRequestRepository.save(request);
        }

        List<ItemRequestDto> requests = itemRequestService.getAllRequests(requester.getId(), 0, 10);

        assertEquals(3, requests.size());
        assertEquals("Request 0", requests.get(0).getDescription());
        assertEquals("Request 1", requests.get(1).getDescription());
        assertEquals("Request 2", requests.get(2).getDescription());
    }

    @Test
    void createRequest_shouldThrowWhenUserNotFound() {
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("Нужна дрель")
                .build();

        Long nonExistentUserId = 999L;

        assertThrows(NotFoundException.class, () -> {
            itemRequestService.createRequest(requestDto, nonExistentUserId);
        });
    }
}
