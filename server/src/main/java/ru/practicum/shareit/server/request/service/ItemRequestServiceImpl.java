package ru.practicum.shareit.server.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.server.exception.NotFoundException;
import ru.practicum.shareit.server.exception.ValidationException;
import ru.practicum.shareit.server.item.ItemRepository;
import ru.practicum.shareit.server.item.dto.ItemDto;
import ru.practicum.shareit.server.item.model.Item;
import ru.practicum.shareit.server.request.ItemRequestRepository;
import ru.practicum.shareit.server.request.dto.ItemRequestDto;
import ru.practicum.shareit.server.request.model.ItemRequest;
import ru.practicum.shareit.server.user.UserRepository;
import ru.practicum.shareit.server.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public ItemRequestDto createRequest(ItemRequestDto requestDto, Long requestorId) {
        User requestor = userRepository.findById(requestorId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        if (requestDto.getDescription() == null || requestDto.getDescription().isBlank()) {
            throw new ValidationException("Описание запроса не может быть пустым");
        }

        ItemRequest request = ItemRequest.builder()
                .description(requestDto.getDescription())
                .requestor(requestor)
                .created(LocalDateTime.now())
                .build();

        ItemRequest savedRequest = itemRequestRepository.save(request);
        return mapToItemRequestDto(savedRequest);
    }

    @Override
    public List<ItemRequestDto> getUserRequests(Long requestorId) {
        userRepository.findById(requestorId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        List<ItemRequest> requests = itemRequestRepository.findByRequestorId(
                requestorId, Sort.by(Sort.Direction.DESC, "created"));

        return requests.stream()
                .map(this::mapToItemRequestDtoWithItems)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getAllRequests(Long userId, int from, int size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        if (from < 0) {
            throw new ValidationException("Параметр 'from' не может быть отрицательным");
        }
        if (size <= 0) {
            throw new ValidationException("Параметр 'size' должен быть положительным");
        }

        List<ItemRequest> requests = itemRequestRepository.findByRequestorIdNot(
                userId, Sort.by(Sort.Direction.DESC, "created"));

        List<ItemRequest> paginatedRequests = requests.stream()
                .skip(from)
                .limit(size)
                .toList();

        return paginatedRequests.stream()
                .map(this::mapToItemRequestDtoWithItems)
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestDto getRequestById(Long requestId, Long userId) {
        ItemRequest request = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос не найден"));

        return mapToItemRequestDtoWithItems(request);
    }

    private ItemRequestDto mapToItemRequestDtoWithItems(ItemRequest request) {
        ItemRequestDto dto = mapToItemRequestDto(request);

        List<Item> items = itemRepository.findByRequestId(request.getId());
        dto.setItems(items.stream()
                .map(this::mapToItemDto)
                .collect(Collectors.toList()));

        return dto;
    }

    private ItemRequestDto mapToItemRequestDto(ItemRequest request) {
        return ItemRequestDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .requestorId(request.getRequestor().getId())
                .created(request.getCreated())
                .build();
    }

    private ItemDto mapToItemDto(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .requestId(item.getRequest() != null ? item.getRequest().getId() : null)
                .build();
    }
}