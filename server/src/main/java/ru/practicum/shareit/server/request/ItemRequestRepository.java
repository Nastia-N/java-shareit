package ru.practicum.shareit.server.request;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.server.request.model.ItemRequest;

import java.util.List;

@Repository
public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {

    List<ItemRequest> findByRequestorId(Long requestorId, Sort sort);

    List<ItemRequest> findByRequestorIdNot(Long requestorId, Sort sort);

}