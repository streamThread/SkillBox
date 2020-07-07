package main.entity;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import main.util.View;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@ApiModel(description = "model to create action")
@Data
@JsonView(View.ActionWithOwnerLogin.class)
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
