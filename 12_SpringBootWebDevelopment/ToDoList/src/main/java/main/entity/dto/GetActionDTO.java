package main.entity.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GetActionDTO {

  @ApiModelProperty(value = "id number of your action")
  private Long id;
  @ApiModelProperty(value = "content of your action")
  private String content;
  @ApiModelProperty(value = "adding time in ISO-8601", example = "2020-08-01T11:01:14.421Z")
  private String time;

  public GetActionDTO(Long id, String content, String time) {
    this.id = id;
    this.content = content;
    this.time = time;
  }
}
