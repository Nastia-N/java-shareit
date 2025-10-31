package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserService userService;

    @Override
    public ItemDto createItem(ItemDto itemDto, Long ownerId) {
        UserDto userDto = userService.getUserById(ownerId);
        User owner = UserMapper.toUser(userDto);

        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            throw new IllegalArgumentException("Поле name не может быть пустым.");
        }
        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            throw new IllegalArgumentException("Поле description не может быть пустым.");
        }
        if (itemDto.getAvailable() == null) {
            throw new IllegalArgumentException("Поле available не может быть пустым.");
        }

        Item item = ItemMapper.toItem(itemDto, owner);
        Item savedItem = itemRepository.save(item);
        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    public ItemDto updateItem(Long itemId, ItemDto itemDto, Long ownerId) {
        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException("Вещь не найдена."));
        if (!ownerId.equals(existingItem.getOwner().getId())) {
            throw new SecurityException("Только владелец может обновить вещь.");
        }
        if (itemDto.getName() != null) {
            existingItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            existingItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existingItem.setAvailable(itemDto.getAvailable());
        }
        Item updatedItem = itemRepository.save(existingItem);
        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    public ItemDto getItemById(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException("Вещь не найдена."));
        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> getItemsByOwner(Long ownerId) {

        UserDto userDto = userService.getUserById(ownerId);
        User owner = UserMapper.toUser(userDto);

        List<Item> items = itemRepository.findByOwner(owner);
        return items.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        return itemRepository.search(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }
}
