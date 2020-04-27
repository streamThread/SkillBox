package main.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

@Value
@AllArgsConstructor(access = AccessLevel.NONE)
@ApiModel(description = "model to create action")
public class Action {

    @ApiModelProperty(notes = "content of your action")
    String content;
    @ApiModelProperty(hidden = true)
    String addingTime;
    @ApiModelProperty(notes = "adding time in 64bit-number format")
    Long timeStamp;

    public Action(String content, Long timeStamp) {
        this.content = content;
        this.addingTime = new Timestamp(timeStamp)
                .toLocalDateTime()
                .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM));
        this.timeStamp = timeStamp;
    }
}
