package main.entity.dto;

import lombok.Data;

@Data
public class AddUserDTO {
    private String login;
    private String password;
}
