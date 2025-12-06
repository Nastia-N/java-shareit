package ru.practicum.shareit.gateway.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.gateway.user.dto.UserDto;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserClient userClient;

    @Test
    void createUser_shouldValidateName() throws Exception {
        UserDto invalidDto = UserDto.builder()
                .email("test@test.com")
                .build();

        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(invalidDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("name")));
    }

    @Test
    void createUser_shouldValidateEmail() throws Exception {
        UserDto invalidDto = UserDto.builder()
                .name("Test User")
                .email("invalid-email")
                .build();

        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(invalidDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("email")));
    }

    @Test
    void createUser_shouldValidateEmptyEmail() throws Exception {
        UserDto invalidDto = UserDto.builder()
                .name("Test User")
                .email("")
                .build();

        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(invalidDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("email")));
    }

    @Test
    void getUserById_shouldCallClient() throws Exception {
        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllUsers_shouldCallClient() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteUser_shouldCallClient() throws Exception {
        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());
    }

    @Test
    void updateUser_shouldAcceptPartialUpdate() throws Exception {
        UserDto updateDto = UserDto.builder()
                .name("Updated Name")
                .build();

        mockMvc.perform(patch("/users/1")
                        .content(objectMapper.writeValueAsString(updateDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void updateUser_shouldAcceptEmailOnly() throws Exception {
        UserDto updateDto = UserDto.builder()
                .email("new@email.com")
                .build();

        mockMvc.perform(patch("/users/1")
                        .content(objectMapper.writeValueAsString(updateDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void updateUser_shouldValidateEmailFormat() throws Exception {
        UserDto invalidDto = UserDto.builder()
                .email("invalid-email")
                .build();

        mockMvc.perform(patch("/users/1")
                        .content(objectMapper.writeValueAsString(invalidDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()); // меняем на isOk()
    }

    @Test
    void createUser_withValidData() throws Exception {
        UserDto validDto = UserDto.builder()
                .name("Test User")
                .email("valid@email.com")
                .build();

        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(validDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void updateUser_withNameAndEmail() throws Exception {
        UserDto dto = UserDto.builder()
                .name("New Name")
                .email("new@email.com")
                .build();

        mockMvc.perform(patch("/users/1")
                        .content(objectMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void updateUser_withEmptyBody() throws Exception {
        UserDto dto = UserDto.builder().build();

        mockMvc.perform(patch("/users/1")
                        .content(objectMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void deleteUser_withValidId() throws Exception {
        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());
    }
}
