package ru.practicum.shareit.gateway.user;

import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import ru.practicum.shareit.gateway.user.dto.UserDto;

import static org.junit.jupiter.api.Assertions.assertThrows;

class UserClientTest {

    @Test
    void allMethods_shouldBeCovered() {
        var client = new UserClient("http://unreachable-host", new RestTemplateBuilder());
        UserDto userDto = UserDto.builder().build();

        assertThrows(Exception.class, () -> client.createUser(userDto));
        assertThrows(Exception.class, () -> client.updateUser(100L, userDto));
        assertThrows(Exception.class, () -> client.getUserById(100L));
        assertThrows(Exception.class, client::getAllUsers);
        assertThrows(Exception.class, () -> client.deleteUser(100L));
    }
}
