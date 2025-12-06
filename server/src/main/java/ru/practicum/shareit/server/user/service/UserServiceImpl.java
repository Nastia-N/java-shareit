package ru.practicum.shareit.server.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.server.exception.ConflictException;
import ru.practicum.shareit.server.exception.NotFoundException;
import ru.practicum.shareit.server.exception.ValidationException;
import ru.practicum.shareit.server.user.UserMapper;
import ru.practicum.shareit.server.user.UserRepository;
import ru.practicum.shareit.server.user.dto.UserDto;
import ru.practicum.shareit.server.user.model.User;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserDto createUser(UserDto userDto) {
        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new ConflictException("Пользователь с email " + userDto.getEmail() + " уже существует");
        }

        User user = userMapper.toEntity(userDto);
        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    @Override
    public UserDto updateUser(Long userId, UserDto userDto) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        boolean isUpdated = false;

        if (userDto.getName() != null) {
            if (userDto.getName().isBlank()) {
                throw new ValidationException("Имя не может быть пустым");
            }
            user.setName(userDto.getName());
            isUpdated = true;
        }

        if (userDto.getEmail() != null) {
            if (userDto.getEmail().isBlank()) {
                throw new ValidationException("Email не может быть пустым");
            }

            if (!userDto.getEmail().equals(user.getEmail())) {
                Optional<User> existingUser = userRepository.findByEmail(userDto.getEmail());
                if (existingUser.isPresent() && !existingUser.get().getId().equals(userId)) {
                    throw new ConflictException("Email " + userDto.getEmail() + " уже используется");
                }
                user.setEmail(userDto.getEmail());
                isUpdated = true;
            }
        }

        if (!isUpdated) {
            throw new ValidationException("Не передано ни одного поля для обновления");
        }

        User updatedUser = userRepository.save(user);
        return userMapper.toDto(updatedUser);
    }

    @Override
    public UserDto getUserById(Long userId) {
        User user = getUserEntityById(userId);
        return userMapper.toDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id: " + userId + " не найден");
        }
        userRepository.deleteById(userId);
    }

    private User getUserEntityById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id: " + userId + " не найден"));
    }
}