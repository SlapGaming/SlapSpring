package com.telluur.slapspring.services.discord.commands.user;

import com.telluur.slapspring.SlapSpringApplication;
import com.telluur.slapspring.services.discord.commands.ICommand;
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
    private static final CommandData commandData = Commands.slash(COMMAND_NAME, COMMAND_DESCRIPTION);

    @Override
    public CommandData data() {
        return commandData;
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        event.reply(String.format("SlapBot version: `%s`", VERSION)).setEphemeral(true).queue();
    }
}
