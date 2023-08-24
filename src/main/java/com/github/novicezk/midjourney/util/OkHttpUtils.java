package com.github.novicezk.midjourney.util;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.HttpStatus;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author NpcZZZZZZ
 * @version 1.0
 * @email 946123601@qq.com
 * @date 2023/7/29
 **/
@Slf4j
public enum OkHttpUtils {

    /**
     * 单例
     */
    X;

    private final OkHttpClient client = new OkHttpClient();



    public String post(String url, String json) {
        return this.post(url, json, new HashMap<>());
    }

    private final MediaType mediaType = MediaType.parse("application/json;charset=UTF-8");

    public String post(String url, String json, Map<String, String> headers) {
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        builder.post(RequestBody.create(json.getBytes(StandardCharsets.UTF_8), mediaType));
        if (null != headers && !headers.isEmpty()) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                builder.header(entry.getKey(), entry.getValue());
            }
        }
        Request request = builder.build();
        return sendRequest(request, url, json);
    }

    /**
     * MediaType不加字符集编码
     * 有一些第三方会用 content-type 作为签名的一部分，content-type:application/json  application/json; charset=UTF-8；
     * 当不用charset=UTF-8作名签名一部分时就会出现签名不通过
     * RequestBody.create（String, MediaType） 时，即使定义：MediaType.parse("application/json"),
     * OkHttp3还是会默认给charset=UTF-8加上，即：content-type=application/json; charset=UTF-8
     * RequestBody.create（byte[], MediaType）时则不会，即：content-type=application/json
     */
    private final MediaType mediaTypeNotCharset = MediaType.parse("application/json");

    public String post(String url, byte[] json, Map<String, String> headers) {
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        builder.post(RequestBody.create(json, mediaTypeNotCharset));
        if (null != headers && !headers.isEmpty()) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                builder.header(entry.getKey(), entry.getValue());
            }
        }
        Request request = builder.build();
        return sendRequest(request, url, new String(json));
    }

    private String sendRequest(Request request, String url, String json) {
        Call call = client.newCall(request);
        try (Response response = call.execute()) {
            int code = response.code();
            if (code == HttpStatus.SC_OK) {
                String string = Objects.requireNonNull(response.body()).string();
                log.info("外部接口请求：url: {}, request: {}, response: {}", url, json, string);
                return string;
            } else {
                if (StringUtils.isNotBlank(json)) {
                    log.error("执行POST请求异常, url: {}, 参数: {}, 状态码: {}", url, json, code);
                } else {
                    log.error("执行GET请求异常, url: {}, 状态码: {}", url, code);
                }
                return StringUtils.EMPTY;
            }
        } catch (Exception e) {
            if (StringUtils.isNotBlank(json)) {
                log.error("执行POST请求异常,url: {}, 参数: {}", url, json, e);
            } else {
                log.error("执行GET请求异常,url: {}", url, e);
            }
            throw new IllegalStateException(e);
        }
    }

    public String get(String url) {
        Request.Builder builder = new Request.Builder();
        builder.get().url(url);
        Request request = builder.build();
        return sendRequest(request, url, null);
    }

    public <T> T post(String url, Object object, Class<T> clazz) {
        return post(url, JsonMapperUtils.X.toJson(object), clazz, new HashMap<>());
    }

    public <T> T post(String url, Object object, Class<T> clazz, Map<String, String> headers) {
        return post(url, JsonMapperUtils.X.toJson(object), clazz, headers);
    }

    public <T> T post(String url, Object object, TypeReference<T> typeReference) {
        return post(url, JsonMapperUtils.X.toJson(object), typeReference, new HashMap<>());
    }

    public <T> T post(String url, Object object, TypeReference<T> typeReference, Map<String, String> headers) {
        return post(url, JsonMapperUtils.X.toJson(object), typeReference, headers);
    }

    public <T> T post(String url, String json, Class<T> clazz, Map<String, String> headers) {
        String content = post(url, json, headers);
        return JsonMapperUtils.X.fromJson(content, clazz);
    }

    public <T> T post(String url, String json, TypeReference<T> typeReference, Map<String, String> headers) {
        String content = post(url, json, headers);
        return JsonMapperUtils.X.fromJson(content, typeReference);
    }
}
