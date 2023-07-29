package com.github.novicezk.midjourney.support;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.novicezk.midjourney.enums.TaskAction;
import com.github.novicezk.midjourney.enums.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
@Schema(description = "任务")
public class Task implements Serializable {
    @Serial
    private static final long serialVersionUID = -674915748204390789L;

    private TaskAction action;
    @Schema(description = "任务ID")
    private String id;
    @Schema(description = "提示词")
    private String prompt;
    @Schema(description = "提示词-英文")
    private String promptEn;

    @Schema(description = "任务描述")
    private String description;
    @Schema(description = "自定义参数")
    private String state;
    @Schema(description = "提交时间")
    private Long submitTime;
    @Schema(description = "开始执行时间")
    private Long startTime;
    @Schema(description = "结束时间")
    private Long finishTime;
    @Schema(description = "图片url")
    private String imageUrl;
    @Schema(description = "任务状态")
    private TaskStatus status = TaskStatus.NOT_START;
    @Schema(description = "任务进度")
    private String progress;
    @Schema(description = "失败原因")
    private String failReason;

    @Schema(description = "关联key, 绘图变化时需对应初始任务的账号")
    private String associationKey;

    // 任务扩展属性，仅支持基本类型
    private Map<String, Object> properties;

    @JsonIgnore
    private final transient Object lock = new Object();

    public void sleep() throws InterruptedException {
        synchronized (this.lock) {
            this.lock.wait();
        }
    }

    public void awake() {
        synchronized (this.lock) {
            this.lock.notifyAll();
        }
    }

    public void start() {
        this.startTime = System.currentTimeMillis();
        this.status = TaskStatus.SUBMITTED;
        this.progress = "0%";
    }

    public void success() {
        this.finishTime = System.currentTimeMillis();
        this.status = TaskStatus.SUCCESS;
        this.progress = "100%";
    }

    public void fail(String reason) {
        this.finishTime = System.currentTimeMillis();
        this.status = TaskStatus.FAILURE;
        this.failReason = reason;
        this.progress = "";
    }

    public Task setProperty(String name, Object value) {
        getProperties().put(name, value);
        return this;
    }

    public Task removeProperty(String name) {
        getProperties().remove(name);
        return this;
    }

    public Object getProperty(String name) {
        return getProperties().get(name);
    }

    @SuppressWarnings("unchecked")
    public <T> T getPropertyGeneric(String name) {
        return (T) getProperty(name);
    }

    public <T> T getProperty(String name, Class<T> clz) {
        return clz.cast(getProperty(name));
    }

    public Map<String, Object> getProperties() {
        if (this.properties == null) {
            this.properties = new HashMap<>();
        }
        return this.properties;
    }
}
