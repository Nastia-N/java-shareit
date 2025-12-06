package ru.practicum.shareit.server.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.server.user.dto.UserDto;
import ru.practicum.shareit.server.user.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;

    @PostMapping
    public UserDto createUser(@RequestBody UserDto userDto) {
        log.info("Создание пользователя: email={}, имя='{}'",
                userDto.getEmail(), userDto.getName());
        return userService.createUser(userDto);
    }

    @PatchMapping("/{userId}")
    public UserDto updateUser(@PathVariable Long userId,
                              @RequestBody UserDto userDto) {
        log.info("Обновление пользователя: ID={}", userId);
        return userService.updateUser(userId, userDto);
    }

    @GetMapping("/{userId}")
    public UserDto getUserById(@PathVariable Long userId) {
        log.info("Получение пользователя: ID={}", userId);
        return userService.getUserById(userId);
    }

    @GetMapping
    public List<UserDto> getAllUsers() {
        log.info("Получение всех пользователей");
        return userService.getAllUsers();
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable Long userId) {
        log.info("Удаление пользователя: ID={}", userId);
        userService.deleteUser(userId);
    }
}
