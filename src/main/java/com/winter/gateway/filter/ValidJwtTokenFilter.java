package com.winter.gateway.filter;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.nimbusds.jose.JWSObject;
import com.winter.gateway.exception.ParseTokenException;
import com.winter.gateway.exception.ValidTokenException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.winterframework.common.constant.RedisConstant;
import reactor.core.publisher.Mono;

import java.text.ParseException;

/**
 * 验证token
 *
 * @author jzyan
 */
@Slf4j
@Component
public class ValidJwtTokenFilter implements WebFilter {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String authorization = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (StrUtil.isEmpty(authorization)) {
            return chain.filter(exchange);
        }
        //从token中解析用户信息并设置到Header中去
        String realToken = authorization.replace("Bearer ", "");
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
        String id = jsonObject.getStr("id");
        String clientId = jsonObject.getStr("client_id");
        String jti = clientId + "_" + id + "_" + jsonObject.getStr("jti");
        String token = stringRedisTemplate.opsForValue().get(RedisConstant.RESOURCE_TOKEN + clientId + ":" + id);
        if (StringUtils.isBlank(token) || !token.equals(jti)) {
            log.info("token已过期或token错误");
            throw new ValidTokenException("token已过期或token错误");
        }
        return chain.filter(exchange);
    }
}
