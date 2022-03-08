package com.winter.gateway;

import ch.qos.logback.core.PropertyDefinerBase;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @Version : 1.0
 * @Description : 获取主机名称
 * @Author : jzyan
 * @CreateDate : 2020/09/04 10:23
 */
public class CanonicalHostNamePropertyDefiner extends PropertyDefinerBase {

    @Override
    public String getPropertyValue() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return null;
        }
    }
}
