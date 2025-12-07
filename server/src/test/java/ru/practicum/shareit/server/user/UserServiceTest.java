package ru.practicum.shareit.server.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.server.exception.ConflictException;
import ru.practicum.shareit.server.exception.NotFoundException;
import ru.practicum.shareit.server.exception.ValidationException;
import ru.practicum.shareit.server.user.dto.UserDto;
import ru.practicum.shareit.server.user.model.User;
import ru.practicum.shareit.server.user.service.UserServiceImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class UserServiceTest {
    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private UserRepository userRepository;

    private User existingUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        existingUser = User.builder()
                .name("Original Name")
                .email("original@email.com")
                .build();
        existingUser = userRepository.save(existingUser);
    }

    @Test
    void createUser_shouldCreateUserSuccessfully() {
        UserDto newUserDto = UserDto.builder()
                .name("New User")
                .email("new@email.com")
                .build();

        UserDto createdUser = userService.createUser(newUserDto);

        assertNotNull(createdUser.getId());
        assertEquals("New User", createdUser.getName());
        assertEquals("new@email.com", createdUser.getEmail());
    }

    @Test
    void createUser_shouldThrowForDuplicateEmail() {
        UserDto user1 = UserDto.builder()
                .name("User 1")
                .email("same@email.com")
                .build();

        userService.createUser(user1);

        UserDto user2 = UserDto.builder()
                .name("User 2")
                .email("same@email.com")
                .build();

        assertThrows(ConflictException.class, () -> {
            userService.createUser(user2);
        });
    }

    @Test
    void updateUser_shouldUpdateNameSuccessfully() {
        UserDto updateDto = UserDto.builder()
                .name("Updated Name")
                .build();

        UserDto updatedUser = userService.updateUser(existingUser.getId(), updateDto);

        assertEquals("Updated Name", updatedUser.getName());
        assertEquals("original@email.com", updatedUser.getEmail()); // email не изменился
    }

    @Test
    void updateUser_shouldUpdateEmailSuccessfully() {
        UserDto updateDto = UserDto.builder()
                .email("updated@email.com")
                .build();

        UserDto updatedUser = userService.updateUser(existingUser.getId(), updateDto);
        assertEquals("Original Name", updatedUser.getName()); // имя не изменилось
        assertEquals("updated@email.com", updatedUser.getEmail());
    }

    @Test
    void updateUser_shouldUpdateBothFieldsSuccessfully() {
        UserDto updateDto = UserDto.builder()
                .name("Updated Name")
                .email("updated@email.com")
                .build();

        UserDto updatedUser = userService.updateUser(existingUser.getId(), updateDto);
        assertEquals("Updated Name", updatedUser.getName());
        assertEquals("updated@email.com", updatedUser.getEmail());
    }

    @Test
    void updateUser_shouldThrowForEmptyName() {
        UserDto updateDto = UserDto.builder()
                .name("   ")
                .build();

        Long userId = existingUser.getId();
        assertThrows(ValidationException.class, () -> {
            userService.updateUser(userId, updateDto);
        });
    }

    @Test
    void updateUser_shouldThrowForEmptyEmail() {
        UserDto updateDto = UserDto.builder()
                .email("   ")
                .build();

        Long userId = existingUser.getId();
        assertThrows(ValidationException.class, () -> {
            userService.updateUser(userId, updateDto);
        });
    }

    @Test
    void updateUser_shouldThrowForDuplicateEmail() {
        User anotherUser = User.builder()
                .name("Another User")
                .email("another@email.com")
                .build();
        anotherUser = userRepository.save(anotherUser);

        UserDto updateDto = UserDto.builder()
                .email("another@email.com")
                .build();

        Long userId = existingUser.getId();
        assertThrows(ConflictException.class, () -> {
            userService.updateUser(userId, updateDto);
        });
    }

    @Test
    void updateUser_shouldNotThrowWhenEmailSameAsCurrent() {
        UserDto updateDto = UserDto.builder()
                .email("original@email.com")
                .name("Original Name")
                .build();

        Long userId = existingUser.getId();
        assertDoesNotThrow(() -> {
            UserDto updatedUser = userService.updateUser(userId, updateDto);
            assertEquals("original@email.com", updatedUser.getEmail());
            assertEquals("Original Name", updatedUser.getName());
        });
    }

    @Test
    void updateUser_shouldThrowWhenNoFieldsToUpdate() {
        UserDto updateDto = UserDto.builder().build();
        Long userId = existingUser.getId();
        assertThrows(ValidationException.class, () -> {
            userService.updateUser(userId, updateDto);
        });
    }

    @Test
    void updateUser_shouldThrowWhenUserNotFound() {
        UserDto updateDto = UserDto.builder()
                .name("Updated")
                .build();

        Long nonExistentUserId = 999L;
        assertThrows(NotFoundException.class, () -> {
            userService.updateUser(nonExistentUserId, updateDto);
        });
    }

    @Test
    void getUserById_shouldReturnUser() {
        UserDto foundUser = userService.getUserById(existingUser.getId());

        assertNotNull(foundUser);
        assertEquals(existingUser.getId(), foundUser.getId());
        assertEquals("Original Name", foundUser.getName());
        assertEquals("original@email.com", foundUser.getEmail());
    }

    @Test
    void getUserById_shouldThrowWhenUserNotFound() {
        Long nonExistentUserId = 999L;
        assertThrows(NotFoundException.class, () -> {
            userService.getUserById(nonExistentUserId);
        });
    }

    @Test
    void getAllUsers_shouldReturnAllUsers() {
        User user2 = User.builder()
                .name("User 2")
                .email("user2@email.com")
                .build();
        userRepository.save(user2);

        List<UserDto> users = userService.getAllUsers();

        assertEquals(2, users.size());
        assertTrue(users.stream().anyMatch(u -> u.getName().equals("Original Name")));
        assertTrue(users.stream().anyMatch(u -> u.getName().equals("User 2")));
    }

    @Test
    void getAllUsers_shouldReturnEmptyListWhenNoUsers() {
        userRepository.deleteAll();
        List<UserDto> users = userService.getAllUsers();
        assertTrue(users.isEmpty());
    }

    @Test
    void deleteUser_shouldDeleteUser() {
        Long userId = existingUser.getId();
        userService.deleteUser(userId);
        assertFalse(userRepository.existsById(userId));
    }

    @Test
    void deleteUser_shouldThrowWhenUserNotFound() {
        Long nonExistentUserId = 999L;
        assertThrows(NotFoundException.class, () -> {
            userService.deleteUser(nonExistentUserId);
        });
    }
}
