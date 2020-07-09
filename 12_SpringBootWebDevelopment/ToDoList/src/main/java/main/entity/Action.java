package main.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.Data;

@Entity
@ApiModel(description = "model to create action")
@Data
public class Action {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @ApiModelProperty(value = "id number of your action")
  private Long id;

  @ApiModelProperty(value = "content of your action")
  private String content;

  @ApiModelProperty(value = "adding time")
  private LocalDateTime timeStamp;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "user_id")
  private User owner;
}
