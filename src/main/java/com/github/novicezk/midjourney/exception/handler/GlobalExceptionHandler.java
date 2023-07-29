package com.github.novicezk.midjourney.exception.handler;

import com.github.novicezk.midjourney.ReturnCode;
import com.github.novicezk.midjourney.exception.AccountException;
import com.github.novicezk.midjourney.result.SubmitResultVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


/**
 * @author NpcZZZZZZ
 * @version 1.0
 * @email 946123601@qq.com
 * @date 2023/7/6
 **/
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 拦截账户异常
     */
    @ExceptionHandler(AccountException.class)
    public SubmitResultVO handleRuntimeException(AccountException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',发生账号异常.", requestURI, e);
        return SubmitResultVO.fail(ReturnCode.ACCOUNT_ERROR, e.getMessage());
    }

    /**
     * 拦截未知的运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public SubmitResultVO handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',发生未知异常.", requestURI, e);
        return SubmitResultVO.fail(ReturnCode.FAILURE, e.getMessage());
    }

    /**
     * 系统异常
     */
    @ExceptionHandler(Exception.class)
    public SubmitResultVO handleException(Exception e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',发生系统异常.", requestURI, e);
        return SubmitResultVO.fail(ReturnCode.FAILURE, e.getMessage());
    }


}
