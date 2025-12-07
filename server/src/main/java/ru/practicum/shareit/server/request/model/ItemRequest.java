package ru.practicum.shareit.server.request.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.shareit.server.user.model.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "requests")
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "description", nullable = false)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requestor_id")
    private User requestor;

    @Column(name = "created")
    private LocalDateTime created;
}
