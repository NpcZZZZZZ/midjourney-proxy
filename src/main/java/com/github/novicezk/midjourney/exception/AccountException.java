package com.github.novicezk.midjourney.exception;

/**
 * @author NpcZZZZZZ
 * @version 1.0
 * @email 946123601@qq.com
 * @date 2023/7/6
 **/
public class AccountException extends RuntimeException {

    /**
     * 错误提示
     */
    private String message;

    /**
     * 空构造方法，避免反序列化问题
     */
    public AccountException() {
        super();
    }

    public AccountException(String message) {
        super(message);
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public AccountException setMessage(String message) {
        this.message = message;
        return this;
    }
}
