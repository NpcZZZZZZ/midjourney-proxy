package spring.config;

import cn.hutool.core.io.resource.ResourceUtil;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.github.novicezk.midjourney.ProxyProperties;
import com.github.novicezk.midjourney.service.DiscordService;
import com.github.novicezk.midjourney.service.TaskStoreService;
import com.github.novicezk.midjourney.service.TranslateService;
import com.github.novicezk.midjourney.service.impl.DiscordServiceImpl;
import com.github.novicezk.midjourney.service.impl.store.InMemoryTaskStoreServiceImpl;
import com.github.novicezk.midjourney.service.impl.store.RedisTaskStoreServiceImpl;
import com.github.novicezk.midjourney.service.impl.translate.BaiduTranslateServiceImpl;
import com.github.novicezk.midjourney.service.impl.translate.GPTTranslateServiceImpl;
import com.github.novicezk.midjourney.support.DiscordHelper;
import com.github.novicezk.midjourney.support.Task;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
@EnableConfigurationProperties(ProxyProperties.class)
public class BeanConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }


    @Bean
    public Map<String, DiscordService> discordService(ProxyProperties properties, DiscordHelper discordHelper, RestTemplate restTemplate) {
        ProxyProperties.DiscordConfig discord = properties.getDiscord();
        String serverUrl = discordHelper.getServer();
        return discord.getDiscordAccountConfigList().stream().map(x -> new DiscordServiceImpl(x.getGuildId(), x.getChannelId(), x.getUserToken(),
                x.getSessionId(), discord.getUserAgent(), serverUrl + "/api/v9/interactions",
                serverUrl + "/api/v9/channels/" + x.getChannelId() + "/attachments",
                serverUrl + "/api/v9/channels/" + x.getChannelId() + "/messages",
                ResourceUtil.readUtf8Str("api-params/imagine.json"),
                ResourceUtil.readUtf8Str("api-params/upscale.json"),
                ResourceUtil.readUtf8Str("api-params/variation.json"),
                ResourceUtil.readUtf8Str("api-params/reroll.json"),
                ResourceUtil.readUtf8Str("api-params/describe.json"),
                ResourceUtil.readUtf8Str("api-params/blend.json"),
                ResourceUtil.readUtf8Str("api-params/message.json"), restTemplate)
        ).collect(Collectors.toMap(x -> x.getDiscordGuildId() + ":" + x.getDiscordChannelId() + ":" + x.getDiscordUserToken(), Function.identity()));
    }

    @Bean
    public TranslateService translateService(ProxyProperties properties, RestTemplate restTemplate) {
        return switch (properties.getTranslateWay()) {
            case BAIDU -> new BaiduTranslateServiceImpl(properties.getBaiduTranslate(), restTemplate);
            case GPT -> new GPTTranslateServiceImpl(properties);
            default -> prompt -> prompt;
        };
    }

    @Bean
    public TaskStoreService taskStoreService(ProxyProperties proxyProperties, RedisConnectionFactory redisConnectionFactory) {
        ProxyProperties.TaskStore.Type type = proxyProperties.getTaskStore().getType();
        Duration timeout = proxyProperties.getTaskStore().getTimeout();
        return switch (type) {
            case IN_MEMORY -> new InMemoryTaskStoreServiceImpl(timeout);
            case REDIS -> new RedisTaskStoreServiceImpl(timeout, taskRedisTemplate(redisConnectionFactory));
        };
    }

    @Bean
    public RedisTemplate<String, Object> taskRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        // 设置序列化
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.WRAPPER_ARRAY);
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(om, Object.class);
        // 配置redisTemplate
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        RedisSerializer<String> stringSerializer = new StringRedisSerializer();
        // key序列化
        redisTemplate.setKeySerializer(stringSerializer);
        // value序列化
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        // Hash key序列化
        redisTemplate.setHashKeySerializer(stringSerializer);
        // Hash value序列化
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}
