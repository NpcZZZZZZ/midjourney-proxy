package spring.config;

import cn.hutool.core.util.RandomUtil;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Npc
 * @version 1.0
 * @email 946123601@qq.com
 * @date 2023/7/27
 **/
@Configuration
public class SwaggerConfig {

    @Value("${open-api.title}")
    private String openApiTitle;
    @Value("${open-api.description}")
    private String openApiDescription;
    @Value("${open-api.concat}")
    private String openApiConcat;
    @Value("${open-api.url}")
    private String openApiUrl;
    @Value("${open-api.version}")
    private String openApiVersion;
    @Value("${open-api.terms-of-service-url}")
    private String openApiTermsOfServiceUrl;

    /**
     * 根据@Tag 上的排序，写入x-order
     *
     * @return the global open api customizer
     */
    @Bean
    public GlobalOpenApiCustomizer orderGlobalOpenApiCustomizer() {
        return openApi -> {
            if (openApi.getTags() != null) {
                openApi.getTags().forEach(tag -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("x-order", RandomUtil.randomInt(0, 100));
                    tag.setExtensions(map);
                });
            }
//            if (openApi.getPaths() != null) {
//                openApi.addExtension("x-test123", "333");
//                openApi.getPaths().addExtension("x-abb", RandomUtil.randomInt(1, 100));
//            }
        };
    }

    @Bean
    public OpenAPI openApi() {
        Contact contact = new Contact();
        contact.setName(openApiConcat);
        contact.setUrl(openApiUrl);
        return new OpenAPI()
                .info(new Info()
                        .title(openApiTitle)
                        .description(openApiDescription)
                        .contact(contact)
                        .termsOfService(openApiTermsOfServiceUrl)
                        .version(openApiVersion)
                        .license(new License().name("Apache 2.0")));
    }

}