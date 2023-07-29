package com.github.novicezk.midjourney.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@Schema(description = "提交结果")
public class SubmitResultVO {

    @Schema(description = "状态码: 1(提交成功), 21(已存在), 22(排队中), other(错误)", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private int code;

    @Schema(description = "描述", requiredMode = Schema.RequiredMode.REQUIRED, example = "提交成功")
    private String description;

    @Schema(description = "任务ID", example = "1320098173412546")
    private String result;

    @Schema(description = "扩展字段")
    private Map<String, Object> properties = new HashMap<>();

    public SubmitResultVO setProperty(String name, Object value) {
        this.properties.put(name, value);
        return this;
    }

    public SubmitResultVO removeProperty(String name) {
        this.properties.remove(name);
        return this;
    }

    public Object getProperty(String name) {
        return this.properties.get(name);
    }

    @SuppressWarnings("unchecked")
    public <T> T getPropertyGeneric(String name) {
        return (T) getProperty(name);
    }

    public <T> T getProperty(String name, Class<T> clz) {
        return clz.cast(getProperty(name));
    }

    public static SubmitResultVO of(int code, String description, String result) {
        return new SubmitResultVO(code, description, result);
    }

    public static SubmitResultVO fail(int code, String description) {
        return new SubmitResultVO(code, description, null);
    }

    private SubmitResultVO(int code, String description, String result) {
        this.code = code;
        this.description = description;
        this.result = result;
    }
}
