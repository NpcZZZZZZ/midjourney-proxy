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
public class UserLoadBalancerServiceImpl implements LoadBalancerService {
    private final ProxyProperties properties;

    @Override
    public String getLoadBalancerKey() {
        List<ProxyProperties.DiscordConfig.DiscordAccountConfig> discordAccountConfigList = properties.getDiscord().getDiscordAccountConfigList();
        int size = discordAccountConfigList.size();
        int i = getAndIncrement() % size;
        ProxyProperties.DiscordConfig.DiscordAccountConfig discordAccountConfig = discordAccountConfigList.get(i);
        return discordAccountConfig.getGuildId() + ":" + discordAccountConfig.getChannelId() + ":" + discordAccountConfig.getUserToken();
    }
}