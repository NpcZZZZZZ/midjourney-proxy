package com.github.novicezk.midjourney.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "任务查询参数")
public class TaskConditionDTO {

    private List<String> ids;

}
