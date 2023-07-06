package com.github.novicezk.midjourney.result;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author NpcZZZZZZ
 * @version 1.0
 * @email 946123601@qq.com
 * @date 2023/7/6
 **/
@Data
@NoArgsConstructor
public class Response<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 成功
     */
    public static final int SUCCESS = 200;

    /**
     * 失败
     */
    public static final int FAIL = 500;

    private int code;

    private String msg;

    private T data;

    public static <T> Response<T> success() {
        return restResult(null, SUCCESS, "操作成功");
    }

    public static <T> Response<T> success(T data) {
        return restResult(data, SUCCESS, "操作成功");
    }

    public static <T> Response<T> success(String msg) {
        return restResult(null, SUCCESS, msg);
    }

    public static <T> Response<T> success(String msg, T data) {
        return restResult(data, SUCCESS, msg);
    }

    public static <T> Response<T> fail() {
        return restResult(null, FAIL, "操作失败");
    }

    public static <T> Response<T> fail(String msg) {
        return restResult(null, FAIL, msg);
    }

    public static <T> Response<T> fail(T data) {
        return restResult(data, FAIL, "操作失败");
    }

    public static <T> Response<T> fail(String msg, T data) {
        return restResult(data, FAIL, msg);
    }

    public static <T> Response<T> fail(int code, String msg) {
        return restResult(null, code, msg);
    }

    private static <T> Response<T> restResult(T data, int code, String msg) {
        Response<T> response = new Response<>();
        response.setCode(code);
        response.setData(data);
        response.setMsg(msg);
        return response;
    }

    public static <T> Boolean isError(Response<T> ret) {
        return !isSuccess(ret);
    }

    public static <T> Boolean isSuccess(Response<T> ret) {
        return Response.SUCCESS == ret.getCode();
    }
}