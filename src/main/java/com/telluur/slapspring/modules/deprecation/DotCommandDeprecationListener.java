package com.telluur.slapspring.modules.deprecation;

import com.telluur.slapspring.modules.ltg.commands.*;
import com.telluur.slapspring.modules.misc.discord.commands.admin.PruneChatSlashCommand;
import com.telluur.slapspring.modules.misc.discord.commands.admin.SaySlashCommand;
import com.telluur.slapspring.modules.misc.discord.commands.restricted.EvalSlashCommand;
import com.telluur.slapspring.modules.misc.discord.commands.restricted.KillSlashCommand;
import com.telluur.slapspring.modules.misc.discord.commands.user.*;
import lombok.NonNull;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.util.Map.entry;

/**
 * Chat listener that looks for users using old-style discord commands,
 * and informs them they have been moved to Slash commands.
 */
@Service
public class DotCommandDeprecationListener extends ListenerAdapter {
    public static final List<String> deprecatedPrefixes = List.of(".", "!");

    public static final Map<String, String> commandMappings = Map.ofEntries(
            entry("eval", EvalSlashCommand.COMMAND_NAME),

            entry("kill", KillSlashCommand.COMMAND_NAME),
            entry("fuckoff", KillSlashCommand.COMMAND_NAME),
            entry("die", KillSlashCommand.COMMAND_NAME),

            entry("prune", PruneChatSlashCommand.COMMAND_NAME),
            entry("clear", PruneChatSlashCommand.COMMAND_NAME),

            entry("say", SaySlashCommand.COMMAND_NAME),

            entry("games", LTGListGamesSlashCommand.COMMAND_NAME),

            entry("info", LTGGameInfoSlashCommand.COMMAND_NAME),
            entry("subscriptions", LTGGameInfoSlashCommand.COMMAND_NAME),
            entry("subs", LTGGameInfoSlashCommand.COMMAND_NAME),

            entry("addgame", LTGAddGameSlashCommand.COMMAND_NAME),

            entry("join", LTGSubscribeSlashCommand.COMMAND_NAME),
            entry("subscribe", LTGSubscribeSlashCommand.COMMAND_NAME),
            entry("sub", LTGSubscribeSlashCommand.COMMAND_NAME),

            entry("leave", LTGUnsubscribeSlashCommand.COMMAND_NAME),
            entry("unsubscribe", LTGUnsubscribeSlashCommand.COMMAND_NAME),
            entry("unsub", LTGUnsubscribeSlashCommand.COMMAND_NAME),

            entry("avatar", AvatarSlashCommand.COMMAND_NAME),

            entry("roll", RollSlashCommand.COMMAND_NAME),
            entry("teams", TeamsSlashCommand.COMMAND_NAME),
            //TODO
            entry("about", "UNIMPLEMENTED"),

            entry("ping", PingSlashCommand.COMMAND_NAME),

            entry("version", VersionSlashCommand.COMMAND_NAME)
    );


    @Override
    public void onMessageReceived(@NonNull MessageReceivedEvent event) {
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
                                .thenCompose(message -> {
                                    MessageEmbed me = new EmbedBuilder()
                                            .setColor(Color.RED)
                                            .setImage("https://c.tenor.com/hO1oLqo-lKIAAAAd/tom-scott-explosion.gif")
                                            .build();
                                    return message.editMessageEmbeds(me).submitAfter(30, TimeUnit.SECONDS);
                                })
                                .thenCompose(message -> message.delete()//delete notice
                                        .and(event.getMessage().delete()) //delete command
                                        .submitAfter(10, TimeUnit.SECONDS));
                        return; //early return
                    }
                }
            }
        }
    }
}
