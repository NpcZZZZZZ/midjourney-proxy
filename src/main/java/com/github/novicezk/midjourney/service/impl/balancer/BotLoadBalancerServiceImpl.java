package com.github.novicezk.midjourney.service.impl.balancer;

import com.github.novicezk.midjourney.ProxyProperties;
import com.github.novicezk.midjourney.service.LoadBalancerService;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * @author NpcZZZZZZ
 * @version 1.0
 * @email 946123601@qq.com
 * @date 2023/6/28
 **/
@RequiredArgsConstructor
public class BotLoadBalancerServiceImpl implements LoadBalancerService {
    private final ProxyProperties properties;

    @Override
    public String getLoadBalancerKey() {
        List<ProxyProperties.DiscordConfig.BotTokenConfig> botTokenConfigList = properties.getDiscord().getBotTokenConfigList();
        int size = botTokenConfigList.size();
        int i = getAndIncrement() % size;
        ProxyProperties.DiscordConfig.BotTokenConfig botTokenConfig = botTokenConfigList.get(i);
        return botTokenConfig.getBotToken();
    }
}