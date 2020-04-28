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

    @ApiModelProperty(value = "content of your action", required = true)
    String content;

    @ApiModelProperty(value = "You don't need to use it. This parameter is just for displaying\r\n" +
            "adding time in a human friendly format when you have GET request")
    String addingTime;

    @ApiModelProperty(value = "adding time in 64bit-number format", required = true)
    long timeStamp;

    public Action(String content, long timeStamp) {
        this.content = content;
        this.addingTime = new Timestamp(timeStamp)
                .toLocalDateTime()
                .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM));
        this.timeStamp = timeStamp;
    }
}
