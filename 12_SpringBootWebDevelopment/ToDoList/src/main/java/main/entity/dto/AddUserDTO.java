package main.entity.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class AddUserDTO {
    @ApiModelProperty(value = "Login of user to add", required = true)
    private String login;
    @ApiModelProperty(value = "Password of user to add", required = true)
    private String password;
}
