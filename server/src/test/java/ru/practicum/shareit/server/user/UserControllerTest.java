package ru.practicum.shareit.server.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.server.user.dto.UserDto;
import ru.practicum.shareit.server.user.service.UserService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void createUser_shouldReturnCreatedUser() throws Exception {
        UserDto requestDto = UserDto.builder()
                .name("Иван Иванов")
                .email("ivan@test.com")
                .build();

        UserDto responseDto = UserDto.builder()
                .id(1L)
                .name("Иван Иванов")
                .email("ivan@test.com")
                .build();

        when(userService.createUser(any(UserDto.class)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Иван Иванов")))
                .andExpect(jsonPath("$.email", is("ivan@test.com")));
    }

    @Test
    void updateUser_shouldReturnUpdatedUser() throws Exception {
        UserDto updateDto = UserDto.builder()
                .name("Обновленное имя")
                .build();

        UserDto responseDto = UserDto.builder()
                .id(1L)
                .name("Обновленное имя")
                .email("ivan@test.com")
                .build();

        when(userService.updateUser(eq(1L), any(UserDto.class)))
                .thenReturn(responseDto);

        mockMvc.perform(patch("/users/1")
                        .content(objectMapper.writeValueAsString(updateDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Обновленное имя")))
                .andExpect(jsonPath("$.email", is("ivan@test.com")));
    }

    @Test
    void getUserById_shouldReturnUser() throws Exception {
        UserDto responseDto = UserDto.builder()
                .id(1L)
                .name("Иван Иванов")
                .email("ivan@test.com")
                .build();

        when(userService.getUserById(eq(1L)))
                .thenReturn(responseDto);

        mockMvc.perform(get("/users/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Иван Иванов")));
    }

    @Test
    void getAllUsers_shouldReturnUsersList() throws Exception {
        UserDto user1 = UserDto.builder()
                .id(1L)
                .name("Иван Иванов")
                .email("ivan@test.com")
                .build();

        UserDto user2 = UserDto.builder()
                .id(2L)
                .name("Петр Петров")
                .email("petr@test.com")
                .build();

        when(userService.getAllUsers())
                .thenReturn(List.of(user1, user2));

        mockMvc.perform(get("/users")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Иван Иванов")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("Петр Петров")));
    }

    @Test
    void deleteUser_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());
    }
}
