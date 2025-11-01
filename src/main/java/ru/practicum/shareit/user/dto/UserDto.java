package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserDto {
    private Long id;
    @NotBlank(message = "Поле name не может быть пустым.")
    private String name;
    @NotBlank(message = "Поле email не может быть пустым.")
    @Email(message = "Электронная почта должна содержать символ @.")
    private String email;
}
