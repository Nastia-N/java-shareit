package ru.practicum.shareit.booking;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT b FROM Booking b JOIN FETCH b.booker JOIN FETCH b.item WHERE b.id = :id")
    Optional<Booking> findByIdWithRelations(@Param("id") Long id);

    @Query("SELECT b FROM Booking b JOIN FETCH b.item WHERE b.booker.id = :bookerId")
    List<Booking> findByBookerId(@Param("bookerId") Long bookerId, Sort sort);

    @Query("SELECT b FROM Booking b JOIN FETCH b.item WHERE b.booker.id = :bookerId " +
            "AND b.start <= :now AND b.end >= :now")
    List<Booking> findCurrentByBookerId(@Param("bookerId") Long bookerId,
                                        @Param("now") LocalDateTime now, Sort sort);

    @Query("SELECT b FROM Booking b JOIN FETCH b.item WHERE b.booker.id = :bookerId AND b.end < :now")
    List<Booking> findByBookerIdAndEndBefore(@Param("bookerId") Long bookerId,
                                             @Param("now") LocalDateTime now, Sort sort);

    @Query("SELECT b FROM Booking b JOIN FETCH b.item WHERE b.booker.id = :bookerId AND b.start > :now")
    List<Booking> findByBookerIdAndStartAfter(@Param("bookerId") Long bookerId,
                                              @Param("now") LocalDateTime now, Sort sort);

    @Query("SELECT b FROM Booking b JOIN FETCH b.item WHERE b.booker.id = :bookerId AND b.status = :status")
    List<Booking> findByBookerIdAndStatus(@Param("bookerId") Long bookerId,
                                          @Param("status") BookingStatus status, Sort sort);

    @Query("SELECT b FROM Booking b JOIN FETCH b.booker JOIN FETCH b.item " +
            "WHERE b.item.owner.id = :ownerId")
    List<Booking> findByItemOwnerId(@Param("ownerId") Long ownerId, Sort sort);

    @Query("SELECT b FROM Booking b JOIN FETCH b.booker JOIN FETCH b.item " +
            "WHERE b.item.owner.id = :ownerId AND b.start <= :now AND b.end >= :now")
    List<Booking> findCurrentByOwnerId(@Param("ownerId") Long ownerId,
                                       @Param("now") LocalDateTime now, Sort sort);

    @Query("SELECT b FROM Booking b JOIN FETCH b.booker JOIN FETCH b.item " +
            "WHERE b.item.owner.id = :ownerId AND b.end < :now")
    List<Booking> findByItemOwnerIdAndEndBefore(@Param("ownerId") Long ownerId,
                                                @Param("now") LocalDateTime now, Sort sort);

    @Query("SELECT b FROM Booking b JOIN FETCH b.booker JOIN FETCH b.item " +
            "WHERE b.item.owner.id = :ownerId AND b.start > :now")
    List<Booking> findByItemOwnerIdAndStartAfter(@Param("ownerId") Long ownerId,
                                                 @Param("now") LocalDateTime now, Sort sort);

    @Query("SELECT b FROM Booking b JOIN FETCH b.booker JOIN FETCH b.item " +
            "WHERE b.item.owner.id = :ownerId AND b.status = :status")
    List<Booking> findByItemOwnerIdAndStatus(@Param("ownerId") Long ownerId,
                                             @Param("status") BookingStatus status, Sort sort);

}