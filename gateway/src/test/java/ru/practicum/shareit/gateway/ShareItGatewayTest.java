package ru.practicum.shareit.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.practicum.shareit.gateway.user.dto.UserDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ShareItGatewayTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void contextLoads() {
    }

    @Test
    void createUser_withInvalidData_shouldReturnBadRequest() {
        UserDto invalidUser = UserDto.builder()
                .name("")
                .email("invalid")
                .build();

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/users",
                invalidUser,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("error");
    }

    @Test
    void getItems_withoutAuthHeader_shouldFail() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/items/1",
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void mainMethodShouldStartApplication() {
        assertDoesNotThrow(() -> ShareItGateway.main(new String[]{}));
    }

    @Test
    void shareItGatewayClassExists() {
        assertNotNull(ShareItGateway.class);
        assertEquals("ShareItGateway", ShareItGateway.class.getSimpleName());
    }
}
