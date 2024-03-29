package com.telluur.slapspring.modules.misc.discord.commands.user;

import com.telluur.slapspring.SlapSpringApplication;
import com.telluur.slapspring.abstractions.discord.commands.ICommand;
import lombok.NonNull;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class VersionSlashCommand implements ICommand {
    public static final String COMMAND_NAME = "version";
    public static final String COMMAND_DESCRIPTION = "Displays the bot's version.";
    public static final String VERSION = Optional.ofNullable(SlapSpringApplication.class.getPackage().getImplementationVersion()).orElse("DEV");
    private static final CommandData COMMAND_DATA = Commands.slash(COMMAND_NAME, COMMAND_DESCRIPTION)
            .setGuildOnly(true);

    @NonNull
    @Override
    public CommandData data() {
        return COMMAND_DATA;
    }

    @Override
    public void handle(@NonNull SlashCommandInteractionEvent event) {
        event.reply(String.format("SlapBot version: `%s`", VERSION)).setEphemeral(true).queue();
    }
}
