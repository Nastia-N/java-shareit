package ru.practicum.shareit.server.user;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.server.user.dto.UserDto;
import ru.practicum.shareit.server.user.model.User;

@Component
public class UserMapper {
    public UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public User toEntity(UserDto userDto) {
        return User.builder()
                .id(userDto.getId())
                .name(userDto.getName())
                .email(userDto.getEmail())
                .build();
    }
}
