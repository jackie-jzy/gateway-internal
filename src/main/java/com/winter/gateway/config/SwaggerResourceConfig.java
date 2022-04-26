package com.winter.gateway.config;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * swagger config
 * </p>
 *
 * @author jzyan
 * @since 2022-04-26
 */
@Slf4j
@Component
@Primary
@AllArgsConstructor
public class SwaggerResourceConfig implements SwaggerResourcesProvider {

    private static final String DISC_CLIENT_PREFIX = "ReactiveCompositeDiscoveryClient_";
    private final RouteLocator routeLocator;

    @Override
    public List<SwaggerResource> get() {
        List<SwaggerResource> resources = new ArrayList<>();
        List<String> routes = new ArrayList<>();
        routeLocator.getRoutes().subscribe(route -> {
            if (route.getId().contains(DISC_CLIENT_PREFIX) && !route.getId().contains("gateway")) {
                routes.add(route.getId().substring(DISC_CLIENT_PREFIX.length()));
            }
        });
        routes.forEach(route -> resources.add(swaggerResource(route, "/" + route + "/v2/api-docs")));
        return resources;
    }

    private SwaggerResource swaggerResource(String name, String location) {
        log.info("Swagger资源, 资源名称:{}, 资源地址:{}", name, location);
        SwaggerResource swaggerResource = new SwaggerResource();
        swaggerResource.setName(name);
        swaggerResource.setLocation(location);
        swaggerResource.setSwaggerVersion("2.0");
        return swaggerResource;
    }

}
