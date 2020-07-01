package main.entity.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReplaceActionDTO {
    @ApiModelProperty(value = "id of the new action to add", required = true)
    private Long id;
    @ApiModelProperty(value = "content of the new action", required = true)
    private String content;
    @ApiModelProperty(value = "adding time of the new action", required = true)
    private LocalDateTime timeStamp;
}
