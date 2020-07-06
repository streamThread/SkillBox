package main.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User owner;
}
