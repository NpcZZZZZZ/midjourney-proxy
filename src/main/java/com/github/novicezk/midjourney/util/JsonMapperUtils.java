package com.github.novicezk.midjourney.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.github.novicezk.midjourney.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * @author NpcZZZZZZ
 * @version 1.0
 * @email 946123601@qq.com
 * @date 2023/7/29
 **/
@Slf4j
@SuppressWarnings(value = {"unchecked", "rawtypes"})
public enum JsonMapperUtils {

    /**
     * 实列
     */
    X;
    private final ObjectMapper mapper = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);


    public String toJson(Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (Exception exception) {
            log.error("write to json string error: {}", object, exception);
        }
        return "";
    }

    public <T> T fromJson(String jsonString, Class<T> clazz) {
        if (StringUtils.isBlank(jsonString)) {
            throw new IllegalArgumentException("传入json为空, 无法转换");
        }
        try {
            return mapper.readValue(jsonString, clazz);
        } catch (Exception exception) {
            log.warn("parse json string error: {}", jsonString, exception);
            throw new ServiceException("转换json异常");
        }

    }

    public <T> T fromJson(String jsonString, JavaType javaType) {
        if (StringUtils.isBlank(jsonString)) {
            throw new IllegalArgumentException("传入json为空, 无法转换");
        }
        try {
            return mapper.readValue(jsonString, javaType);
        } catch (IOException var4) {
            log.warn("parse json string error: {}", jsonString, var4);
            throw new ServiceException("转换json异常");
        }

    }

    public <T> T fromJson(String jsonString, TypeReference<T> tTypeReference) {
        if (StringUtils.isBlank(jsonString)) {
            throw new IllegalArgumentException("传入json为空, 无法转换");
        }
        try {
            return mapper.readValue(jsonString, tTypeReference);
        } catch (IOException var4) {
            log.warn("parse json string error: {}", jsonString, var4);
            return null;
        }

    }

    public JavaType buildCollectionType(Class<? extends Collection> collectionClass, Class<?> elementClass) {
        return mapper.getTypeFactory().constructCollectionType(collectionClass, elementClass);
    }

    public JavaType buildMapType(Class<? extends Map> mapClass, Class<?> keyClass, Class<?> valueClass) {
        return mapper.getTypeFactory().constructMapType(mapClass, keyClass, valueClass);
    }

    public String toJsonP(String functionName, Object object) {
        return this.toJson(new JSONPObject(functionName, object));
    }

    public void enableEnumUseToString() {
        mapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        mapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
    }

    public ObjectMapper getMapper() {
        return mapper;
    }
}
