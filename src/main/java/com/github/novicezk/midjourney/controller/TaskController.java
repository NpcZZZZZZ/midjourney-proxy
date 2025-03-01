package com.github.novicezk.midjourney.controller;

import cn.hutool.core.comparator.CompareUtil;
import com.github.novicezk.midjourney.dto.TaskConditionDTO;
import com.github.novicezk.midjourney.service.TaskStoreService;
import com.github.novicezk.midjourney.support.Task;
import com.github.novicezk.midjourney.support.TaskQueueHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Tag(name = "任务查询")
@RestController
@RequestMapping("/task")
@RequiredArgsConstructor
public class TaskController {
    private final TaskStoreService taskStoreService;
    private final TaskQueueHelper taskQueueHelper;

    @Operation(summary = "查询所有任务")
    @GetMapping("/list")
    public List<Task> list() {
        return this.taskStoreService.list().stream()
                .sorted((t1, t2) -> CompareUtil.compare(t2.getSubmitTime(), t1.getSubmitTime()))
                .toList();
    }

    @Operation(summary = "指定ID获取任务")

    @GetMapping("/{id}/fetch")
    public Task fetch(@Parameter(name = "id", description = "任务id") @PathVariable String id) {
        return this.taskStoreService.get(id);
    }

    @Operation(summary = "查询任务队列")
    @GetMapping("/queue")
    public List<Task> queue() {
        Set<String> queueTaskIds = this.taskQueueHelper.getQueueTaskIds();
        return queueTaskIds.stream().map(this.taskStoreService::get).filter(Objects::nonNull)
                .sorted(Comparator.comparing(Task::getSubmitTime))
                .toList();
    }

    @Operation(summary = "根据条件查询任务")
    @PostMapping("/list-by-condition")
    public List<Task> listByCondition(@RequestBody TaskConditionDTO conditionDTO) {
        if (conditionDTO.getIds() == null) {
            return Collections.emptyList();
        }
        return conditionDTO.getIds().stream().map(this.taskStoreService::get).filter(Objects::nonNull).toList();
    }

}
