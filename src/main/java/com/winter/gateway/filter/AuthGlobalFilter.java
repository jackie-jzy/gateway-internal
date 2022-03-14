package com.winter.gateway.filter;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.nimbusds.jose.JWSObject;
import com.winter.gateway.exception.ParseTokenException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.winterframework.common.constant.AuthConstant;
import reactor.core.publisher.Mono;

import java.text.ParseException;

/**
 * 将登录用户的JWT转化成用户信息的全局过滤器
 *
 * @author jzyan
 */
@Slf4j
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String token = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (StrUtil.isEmpty(token)) {
            return chain.filter(exchange);
        }

        //从token中解析用户信息并设置到Header中去
        String realToken = token.replace("Bearer ", "");
        JWSObject jwsObject;
        try {
            jwsObject = JWSObject.parse(realToken);
        } catch (ParseException e) {
            log.error("解析token异常,异常信息：{}", e);
            throw new ParseTokenException("解析token异常");
        }

        String userStr = jwsObject.getPayload().toString();
        if (StrUtil.isEmpty(userStr)) {
            log.error("获取Token用户信息为null");
            throw new ParseTokenException("获取Token用户信息为null");
        }

        JSONObject jsonObject = JSONUtil.parseObj(userStr);
        ServerHttpRequest request = exchange.getRequest().mutate()
                .header(AuthConstant.USER_ID, jsonObject.getStr("id"))
                .header(AuthConstant.NICK_NAME, jsonObject.getStr("nick_name"))
                .header(AuthConstant.USER_NAME, jsonObject.getStr("user_name"))
                .header(AuthConstant.USER_ORG_ID, jsonObject.getStr("user_org_id"))
                .header(AuthConstant.USER_ROOT, jsonObject.getStr("user_root"))
                .build();
        exchange = exchange.mutate().request(request).build();
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
