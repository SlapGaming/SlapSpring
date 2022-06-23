package com.telluur.slapspring.services.discord.impl.nsa;

import com.telluur.slapspring.services.discord.BotSession;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageBulkDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


@Service
public class ChatListener extends ListenerAdapter {
    private static final Color NSA_COLOR = new Color(1, 1, 222);

    private final Map<String, Message> db = new HashMap<>();


    @Autowired
    private BotSession session;

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        //Skip bot messages
        if (event.getAuthor().isBot()) {
            return;
        }
        db.put(event.getMessageId(), event.getMessage());
    }

    @Override
    public void onMessageUpdate(@NotNull MessageUpdateEvent event) {
        if (event.isFromGuild() && db.containsKey(event.getMessageId())) {
            Message oldMsg = db.get(event.getMessageId());
            Message updatedMsg = event.getMessage();

            String channelTypeString = switch (event.getChannelType()) {
                case TEXT -> "Text Channel";
                case VOICE -> "Voice Channel";
                case GUILD_PUBLIC_THREAD -> "Public Thread";
                case GUILD_PRIVATE_THREAD -> "Private Thread";
                default -> "Unknown Channel Type";
            };

            String description = String.format("""
                            **%s:** %s
                            **Member:** %s (%s#%s)
                                                
                            **\u2015\u2015\u2015\u2015\u2015\u2015\u2015\u2015\u2015\u2015\u2015 \u21C3Old\u21C2 \u2015\u2015\u2015\u2015\u2015\u2015\u2015\u2015\u2015\u2015\u2015**
                                                
                            %s
                                                
                            **\u2015\u2015\u2015\u2015\u2015\u2015\u2015\u2015 \u21BFOld\u21BE | \u21C3New\u21C2 \u2015\u2015\u2015\u2015\u2015\u2015\u2015\u2015**
                                                        
                            %s
                            """,
                    channelTypeString,
                    event.getTextChannel().getName(),
                    Objects.requireNonNull(event.getMember()).getAsMention(), //Checked by event.isFromGuild()
                    event.getAuthor().getName(),
                    event.getAuthor().getDiscriminator(),
                    oldMsg.getContentDisplay(),
                    updatedMsg.getContentDisplay()
            );

            //TODO attachements

            MessageEmbed me = new EmbedBuilder()
                    .setColor(NSA_COLOR)
                    .setTitle("NSA: A message was edited")
                    .setDescription(description)
                    .build();

            session.getNSATX().sendMessageEmbeds(me).queue();
        }
    }

    @Override
    public void onMessageDelete(@NotNull MessageDeleteEvent event) {
        super.onMessageDelete(event);
    }

    @Override
    public void onMessageBulkDelete(@NotNull MessageBulkDeleteEvent event) {
        super.onMessageBulkDelete(event);
    }
}
