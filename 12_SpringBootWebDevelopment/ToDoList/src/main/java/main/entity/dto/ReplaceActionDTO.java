package main.entity.dto;

import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ReplaceActionDTO {

  @ApiModelProperty(value = "id of the new action to add", required = true)
  private Long id;
  @ApiModelProperty(value = "content of the new action", required = true)
  private String content;
  @ApiModelProperty(value = "adding time of the new action", required = true)
  private LocalDateTime creationTime;
}
