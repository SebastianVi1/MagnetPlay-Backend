package org.sebas.magnetplay.mapper;

import org.sebas.magnetplay.dto.UserDto;
import org.sebas.magnetplay.model.Users;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {


    public UserDto toDto( Users user){
        UserDto userDto = new UserDto();
        userDto.setUsername(user.getUsername());
        userDto.setPassword(user.getPassword());
        userDto.setEmail(user.getEmail());

        return userDto;

    }

    public Users toModel(UserDto userDto){
        Users user = new Users();
        user.setUsername(userDto.getUsername());
        user.setPassword(userDto.getPassword());
        user.setEmail(userDto.getEmail());

        return user;
    }
}
