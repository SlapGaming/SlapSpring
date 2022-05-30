package com.telluur.slapspring.services.discord.impl.deprecation;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

@Service
public class DotCommandDeprecationListener extends ListenerAdapter {
    public static List<String> deprecatedDotCommands = Stream.of(
                    "eval",
                    "kill", "fuckoff", "die",
                    "prune", "clear",
                    "say",
                    "games",
                    "join", "subscribe", "sub",
                    "info", "subscriptions", "subs",
                    "leave", "unsubscribe", "unsub",
                    "avatar",
                    "roll",
                    "teams",
                    "about",
                    "ping",
                    "version")
            .map(s -> "." + s).toList();


    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        for (String dot : deprecatedDotCommands) {
            if (event.getMessage().getContentDisplay().startsWith(dot)) {
                event.getChannel().sendMessageFormat(
                        "The %s command has moved to slash commands.\r\nType `/` in the chat to get started with the new commands."
                ).queue();
                return;
            }
        }
    }
}
