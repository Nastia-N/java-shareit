package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemForOwnerDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;

import java.util.List;

public interface ItemService {

    ItemDto createItem(ItemDto itemDto, Long ownerId);

    ItemDto updateItem(Long itemId, ItemDto itemDto, Long ownerId);

    ItemWithBookingsDto getItemById(Long itemId, Long userId);

    List<ItemForOwnerDto> getItemsByOwner(Long ownerId);

    List<ItemDto> searchItems(String text, Long userId);

    CommentDto addComment(Long itemId, Long authorId, CommentDto commentDto);

}
