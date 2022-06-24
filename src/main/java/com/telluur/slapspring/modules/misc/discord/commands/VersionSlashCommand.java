package com.telluur.slapspring.modules.misc.discord.commands;

import com.telluur.slapspring.SlapSpringApplication;
import com.telluur.slapspring.abstractions.discord.commands.ICommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.Optional;

@Service
public class VersionSlashCommand implements ICommand {
    public static final String COMMAND_NAME = "version";
    public static final String COMMAND_DESCRIPTION = "Displays the bot's version.";
    public static final String VERSION = Optional.ofNullable(SlapSpringApplication.class.getPackage().getImplementationVersion()).orElse("DEV");
    private static final CommandData commandData = Commands.slash(COMMAND_NAME, COMMAND_DESCRIPTION).setDefaultEnabled(true);

    @Nonnull
    @Override
    public CommandData data() {
        return commandData;
    }

    @Override
    public void handle(@Nonnull SlashCommandInteractionEvent event) {
        event.reply(String.format("SlapBot version: `%s`", VERSION)).setEphemeral(true).queue();
    }
}
