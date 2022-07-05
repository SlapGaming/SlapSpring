package com.telluur.slapspring.modules.ltg.listeners;

import com.telluur.slapspring.modules.ltg.LTGQuickSubscribeService;
import com.telluur.slapspring.core.discord.BotProperties;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.Set;

@Service
public class LTGChatListener extends ListenerAdapter {

    private final String ltgTxId;

    private final LTGQuickSubscribeService quickSubscribeService;

    public LTGChatListener(@Autowired BotProperties botProperties, @Autowired LTGQuickSubscribeService quickSubscribeService) {
        ltgTxId = botProperties.tx_ltg();
        this.quickSubscribeService = quickSubscribeService;
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        Message message = event.getMessage();
        if (message.isFromGuild() && !message.getAuthor().isBot() && message.getTextChannel().getId().equals(ltgTxId)) {
            Set<Role> roles = message.getMentions().getRolesBag().uniqueSet();
            quickSubscribeService.sendQuickSubscribe(roles);
        }
    }


}
