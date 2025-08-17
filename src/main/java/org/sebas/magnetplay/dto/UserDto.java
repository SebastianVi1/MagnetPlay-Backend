package org.sebas.magnetplay.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    @NotBlank(message = "The username canno't be empty")
    private String username;
    private String email;
    @NotBlank(message = "Add a password")
    private String password;

}
