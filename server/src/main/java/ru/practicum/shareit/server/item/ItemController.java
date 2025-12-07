package ru.practicum.shareit.server.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.server.item.dto.CommentDto;
import ru.practicum.shareit.server.item.dto.ItemDto;
import ru.practicum.shareit.server.item.dto.ItemForOwnerDto;
import ru.practicum.shareit.server.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.server.item.service.ItemService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
@Slf4j
public class ItemController {
    private final ItemService itemService;
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public ItemDto createItem(@RequestBody ItemDto itemDto,
                              @RequestHeader(USER_ID_HEADER) Long ownerId) {
        log.info("Создание вещи: владелец={}, название='{}'", ownerId, itemDto.getName());
        return itemService.createItem(itemDto, ownerId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@PathVariable Long itemId,
                              @RequestBody ItemDto itemDto,
                              @RequestHeader(USER_ID_HEADER) Long ownerId) {
        log.info("Обновление вещи: ID={}, владелец={}", itemId, ownerId);
        return itemService.updateItem(itemId, itemDto, ownerId);
    }

    @GetMapping("/{itemId}")
    public ItemWithBookingsDto getItemById(@PathVariable Long itemId,
                                           @RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Получение вещи: ID={}, пользователь={}", itemId, userId);
        return itemService.getItemById(itemId, userId);
    }

    @GetMapping
    public List<ItemForOwnerDto> getItemsByOwner(@RequestHeader(USER_ID_HEADER) Long ownerId) {
        log.info("Получение вещей владельца: ID={}", ownerId);
        return itemService.getItemsByOwner(ownerId);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam String text,
                                     @RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Поиск вещей: текст='{}', пользователь={}", text, userId);
        return itemService.searchItems(text, userId);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@PathVariable Long itemId,
                                 @RequestHeader(USER_ID_HEADER) Long authorId,
                                 @RequestBody CommentDto commentDto) {
        log.info("Добавление комментария: вещь={}, автор={}", itemId, authorId);
        return itemService.addComment(itemId, authorId, commentDto);
    }
}
