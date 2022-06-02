package com.telluur.slapspring.services.discord.impl.deprecation;

import com.telluur.slapspring.services.discord.commands.system.EvalCommand;
import com.telluur.slapspring.services.discord.commands.system.KillCommand;
import com.telluur.slapspring.services.discord.commands.user.*;
import com.telluur.slapspring.services.discord.commands.user.avatar.AvatarSlashCommand;
import com.telluur.slapspring.services.discord.commands.user.ltg.AddGameSlashCommand;
import com.telluur.slapspring.services.discord.commands.user.ltg.SubscribeSlashCommand;
import com.telluur.slapspring.services.discord.commands.user.ltg.UnsubscribeSlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.util.Map.entry;

@Service
public class DotCommandDeprecationListener extends ListenerAdapter {
    public static final List<String> deprecatedPrefixes = List.of(".", "!");

    public static final Map<String, String> commandMappings = Map.ofEntries(
            entry("eval", EvalCommand.COMMAND_NAME),

            entry("kill", KillCommand.COMMAND_NAME),
            entry("fuckoff", KillCommand.COMMAND_NAME),
            entry("die", KillCommand.COMMAND_NAME),

            entry("prune", PruneChatSlashCommand.COMMAND_NAME),
            entry("clear", PruneChatSlashCommand.COMMAND_NAME),

            entry("say", SaySlashCommand.COMMAND_NAME),
            //TODO
            entry("games", "UNIMPLEMENTED"),

            entry("info", "UNIMPLEMENTRED"),
            entry("subscriptions", "UNIMPLEMENTRED"),
            entry("subs", "UNIMPLEMENTRED"),

            entry("addgame", AddGameSlashCommand.COMMAND_NAME),

            entry("join", SubscribeSlashCommand.COMMAND_NAME),
            entry("subscribe", SubscribeSlashCommand.COMMAND_NAME),
            entry("sub", SubscribeSlashCommand.COMMAND_NAME),

            entry("leave", UnsubscribeSlashCommand.COMMAND_NAME),
            entry("unsubscribe", UnsubscribeSlashCommand.COMMAND_NAME),
            entry("unsub", UnsubscribeSlashCommand.COMMAND_NAME),

            entry("avatar", AvatarSlashCommand.COMMAND_NAME),

            entry("roll", RollSlashCommand.COMMAND_NAME),
            //TODO
            entry("teams", "UNIMPLEMENTED"),
            //TODO
            entry("about", "UNIMPLEMENTED"),

            entry("ping", PingSlashCommand.COMMAND_NAME),

            entry("version", VersionSlashCommand.COMMAND_NAME)
    );


    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String contentDisplay = event.getMessage().getContentDisplay();
        for (String prefix : deprecatedPrefixes) {
            if (contentDisplay.startsWith(prefix)) {
                for (String cmd : commandMappings.keySet()) {
                    if (contentDisplay.substring(prefix.length()).startsWith(cmd)) {
                        MessageEmbed deprecationNotice = new EmbedBuilder()
                                .setColor(Color.RED)
                                .setTitle("Deprecation Notice")
                                .setDescription(String.format("The `%s%s` command has moved to slash commands,\r\n" +
                                        "type `/%s` to get started.", prefix, cmd, commandMappings.get(cmd)))
                                .setFooter("This message will self-destruct after 30 seconds.")
                                .build();
                        event.getChannel().sendMessageEmbeds(deprecationNotice).submit()
                                .thenCompose(message -> message.delete()//delete notice
                                        .and(event.getMessage().delete()) //delete command
                                        .submitAfter(30, TimeUnit.SECONDS)
                                )
                                .whenComplete((v, e) -> {
                                    //Ignore errors.
                                });
                        return; //early return
                    }
                }
            }
        }
    }
}
