package com.winter.gateway.component;

import cn.hutool.core.date.DateUtil;
import com.winter.gateway.exception.ValidTokenException;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @Version : 1.0.0
 * @Description : 统一异常返回处理器
 * @Author : jzyan
 * @CreateDate : 2020/05/21 14:01
 */
public class CustomErrorWebExceptionHandler extends DefaultErrorWebExceptionHandler {

    public CustomErrorWebExceptionHandler(ErrorAttributes errorAttributes, ResourceProperties resourceProperties, ErrorProperties errorProperties, ApplicationContext applicationContext) {
        super(errorAttributes, resourceProperties, errorProperties, applicationContext);
    }

    @Override
    protected Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        int code = HttpStatus.INTERNAL_SERVER_ERROR.value();
        Throwable error = super.getError(request);
        if (error instanceof org.springframework.cloud.gateway.support.NotFoundException) {
            code = HttpStatus.NOT_FOUND.value();
        }
        if (error instanceof ValidTokenException) {
            code = HttpStatus.UNAUTHORIZED.value();
        }
        return response(code, this.buildMessage(request, error));
    }

    @Override
    protected int getHttpStatus(Map<String, Object> errorAttributes) {
        return (Integer) errorAttributes.get("code");
    }

    /**
     * 构建异常信息
     *
     * @param request
     * @param ex
     * @return
     */
    private String buildMessage(ServerRequest request, Throwable ex) {
        return ex.getMessage();
    }

    /**
     * 构建返回的JSON数据格式
     *
     * @param status       状态码
     * @param errorMessage 异常信息
     * @return
     */
    public static Map<String, Object> response(int status, String errorMessage) {
        Map<String, Object> map = new HashMap<>(4);
        map.put("code", status);
        map.put("message", errorMessage);
        map.put("data", null);
        map.put("dateTime", DateUtil.formatDateTime(new Date()));
        return map;
    }

}
