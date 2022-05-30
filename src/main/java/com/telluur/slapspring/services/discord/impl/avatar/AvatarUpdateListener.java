package com.telluur.slapspring.services.discord.impl.avatar;

import com.telluur.slapspring.services.discord.BotSession;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AvatarUpdateListener extends ListenerAdapter {
    private final BotSession botSession;

    public AvatarUpdateListener(@Autowired BotSession botSession) {
        this.botSession = botSession;
    }
}
