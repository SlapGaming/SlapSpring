package com.telluur.slapspring.modules.ltg.listeners;

import com.telluur.slapspring.core.discord.BotProperties;
import com.telluur.slapspring.modules.ltg.LTGQuickSubscribeService;
import lombok.NonNull;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Chat listener that sends a quick subscribe when a LTG role is mentioned in the LTG channel.
 */
@Service
public class LTGChatListener extends ListenerAdapter {

    private final String ltgTxId;

    private final LTGQuickSubscribeService quickSubscribeService;

    public LTGChatListener(@Autowired BotProperties botProperties, @Autowired LTGQuickSubscribeService quickSubscribeService) {
        ltgTxId = botProperties.tx_ltg();
        this.quickSubscribeService = quickSubscribeService;
    }

    @Override
    public void onMessageReceived(@NonNull MessageReceivedEvent event) {
        Message message = event.getMessage();
        if (message.isFromGuild() && !message.getAuthor().isBot() && message.getChannel().getId().equals(ltgTxId)) {
            Set<Role> roles = message.getMentions().getRolesBag().uniqueSet();
            quickSubscribeService.sendQuickSubscribe(roles);
        }
    }


}
