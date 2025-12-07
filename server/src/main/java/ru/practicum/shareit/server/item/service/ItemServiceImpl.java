package ru.practicum.shareit.server.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.server.booking.model.Booking;
import ru.practicum.shareit.server.booking.BookingRepository;
import ru.practicum.shareit.server.booking.model.BookingStatus;
import ru.practicum.shareit.server.booking.dto.BookingInfoDto;
import ru.practicum.shareit.server.exception.NotFoundException;
import ru.practicum.shareit.server.exception.ValidationException;
import ru.practicum.shareit.server.item.CommentRepository;
import ru.practicum.shareit.server.item.ItemMapper;
import ru.practicum.shareit.server.item.ItemRepository;
import ru.practicum.shareit.server.item.dto.*;
import ru.practicum.shareit.server.item.model.Comment;
import ru.practicum.shareit.server.item.model.Item;
import ru.practicum.shareit.server.request.ItemRequestRepository;
import ru.practicum.shareit.server.request.model.ItemRequest;
import ru.practicum.shareit.server.user.UserRepository;
import ru.practicum.shareit.server.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Override
    public ItemDto createItem(ItemDto itemDto, Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            throw new ValidationException("Название вещи не может быть пустым");
        }

        ItemRequest request = null;
        if (itemDto.getRequestId() != null) {
            request = itemRequestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new NotFoundException("Запрос с id " + itemDto.getRequestId() + " не найден"));
        }

        Item item = ItemMapper.toItem(itemDto, owner, request);
        Item savedItem = itemRepository.save(item);
        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    public ItemDto updateItem(Long itemId, ItemDto itemDto, Long ownerId) {
        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        if (!existingItem.getOwner().getId().equals(ownerId)) {
            throw new NotFoundException("Только владелец может обновлять вещь");
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
    public ItemWithBookingsDto getItemById(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        ItemWithBookingsDto dto = mapToItemWithBookingsDto(item);

        if (item.getOwner().getId().equals(userId)) {
            LocalDateTime now = LocalDateTime.now();

            List<Booking> lastBookings = bookingRepository.findByItemOwnerIdAndEndBefore(
                    userId, now, Sort.by(Sort.Direction.DESC, "end"));
            if (!lastBookings.isEmpty()) {
                dto.setLastBooking(mapToBookingInfoDto(lastBookings.getFirst()));
            }

            List<Booking> nextBookings = bookingRepository.findByItemOwnerIdAndStartAfter(
                    userId, now, Sort.by(Sort.Direction.ASC, "start"));
            if (!nextBookings.isEmpty()) {
                dto.setNextBooking(mapToBookingInfoDto(nextBookings.getFirst()));
            }
        }

        List<Comment> comments = commentRepository.findByItemId(itemId);
        dto.setComments(mapToCommentDtoList(comments));

        return dto;
    }

    @Override
    public List<ItemForOwnerDto> getItemsByOwner(Long ownerId) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        List<Item> items = itemRepository.findByOwnerId(ownerId);
        LocalDateTime now = LocalDateTime.now();

        return items.stream().map(item -> {
            ItemForOwnerDto dto = mapToItemForOwnerDto(item);

            List<Booking> lastBookings = bookingRepository.findByItemOwnerIdAndEndBefore(
                    ownerId, now, Sort.by(Sort.Direction.DESC, "end"));
            List<Booking> nextBookings = bookingRepository.findByItemOwnerIdAndStartAfter(
                    ownerId, now, Sort.by(Sort.Direction.ASC, "start"));

            if (!lastBookings.isEmpty()) {
                dto.setLastBooking(mapToBookingInfoDto(lastBookings.getFirst()));
            }
            if (!nextBookings.isEmpty()) {
                dto.setNextBooking(mapToBookingInfoDto(nextBookings.getFirst()));
            }

            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(String text, Long userId) {
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }

        List<Item> items = itemRepository.searchAvailableItems(text);
        return items.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto addComment(Long itemId, Long authorId, CommentDto commentDto) {
        List<Booking> userBookings = bookingRepository.findByBookerIdAndEndBefore(
                authorId, LocalDateTime.now(), Sort.by(Sort.Direction.DESC, "end"));

        boolean hasBookedItem = userBookings.stream()
                .anyMatch(booking -> booking.getItem().getId().equals(itemId)
                        && booking.getStatus() == BookingStatus.APPROVED);

        if (!hasBookedItem) {
            throw new ValidationException("Можно комментировать только ранее забронированные вещи");
        }

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Comment comment = Comment.builder()
                .text(commentDto.getText())
                .item(item)
                .author(author)
                .created(LocalDateTime.now())
                .build();

        Comment savedComment = commentRepository.save(comment);
        return mapToCommentDto(savedComment);
    }

    private ItemWithBookingsDto mapToItemWithBookingsDto(Item item) {
        return ItemWithBookingsDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .build();
    }

    private ItemForOwnerDto mapToItemForOwnerDto(Item item) {
        return ItemForOwnerDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .build();
    }

    private BookingInfoDto mapToBookingInfoDto(Booking booking) {
        return BookingInfoDto.builder()
                .id(booking.getId())
                .bookerId(booking.getBooker().getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .build();
    }

    private CommentDto mapToCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(comment.getAuthor().getName())
                .created(comment.getCreated())
                .build();
    }

    private List<CommentDto> mapToCommentDtoList(List<Comment> comments) {
        return comments.stream()
                .map(this::mapToCommentDto)
                .collect(Collectors.toList());
    }
}