package ru.practicum.shareit.server.item.service;

import ru.practicum.shareit.server.item.dto.CommentDto;
import ru.practicum.shareit.server.item.dto.ItemDto;
import ru.practicum.shareit.server.item.dto.ItemForOwnerDto;
import ru.practicum.shareit.server.item.dto.ItemWithBookingsDto;

import java.util.List;

public interface ItemService {

    ItemDto createItem(ItemDto itemDto, Long ownerId);

    ItemDto updateItem(Long itemId, ItemDto itemDto, Long ownerId);

    ItemWithBookingsDto getItemById(Long itemId, Long userId);

    List<ItemForOwnerDto> getItemsByOwner(Long ownerId);

    List<ItemDto> searchItems(String text, Long userId);

    CommentDto addComment(Long itemId, Long authorId, CommentDto commentDto);

}