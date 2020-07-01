package main.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

@Entity
@ApiModel(description = "model to create action")
@Data
@NoArgsConstructor
public class Action {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @ApiModelProperty(value = "id number of your action")
    private Long id;

    @ApiModelProperty(value = "content of your action")
    private String content;

    @ApiModelProperty(value = "You don't need to use it. This parameter is just for displaying\r\n" +
            "adding time in a human friendly format when you have GET request")
    private String addingTime;

    @ApiModelProperty(value = "adding time in 64bit-number format")
    private Long timeStamp;

    public Action(String content, Long timeStamp) {
        this.content = content;
        setTimeStamp(timeStamp);
    }

    public Action(Long id, String content, Long timeStamp) {
        this.id = id;
        this.content = content;
        setTimeStamp(timeStamp);
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
        this.addingTime = new Timestamp(timeStamp)
                .toLocalDateTime()
                .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM));
    }
}
