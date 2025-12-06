package ru.practicum.shareit.server.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.server.item.model.Comment;
import ru.practicum.shareit.server.item.model.Item;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByItemId(Long itemId);

    List<Comment> findByItemIn(List<Item> items);

    List<Comment> findByAuthorId(Long authorId);

}
