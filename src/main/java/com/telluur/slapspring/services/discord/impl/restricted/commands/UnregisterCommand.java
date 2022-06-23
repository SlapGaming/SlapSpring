package com.telluur.slapspring.services.discord.impl.restricted.commands;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.Objects;

@Service
@Slf4j
public class UnregisterCommand extends AbstractRestrictedCommand {
    public static final String COMMAND_NAME = "unregister";
    public static final String COMMAND_DESCRIPTION = "Unregisters all commands from global and bound guild.";
    private static final CommandData commandData = Commands.slash(COMMAND_NAME, COMMAND_DESCRIPTION)
            .setDefaultEnabled(false);

    @Nonnull
    @Override
    public CommandData data() {
        return commandData;
    }

    @Override
    public void systemHandle(SlashCommandInteractionEvent event) {
        event.reply("Unregistering all commands, please check the sys logs...").setEphemeral(true).queue();

        //Should never be null as commands are always registered to a guild.
        Guild g = Objects.requireNonNull(event.getGuild());

        g.retrieveCommands().queue(commands -> commands.forEach(
                        command -> g.deleteCommandById(command.getId()).queue(
                                ok -> log.info("Successfully unregistered command: {}", command.getName()),
                                error -> log.error("Failed to unregister command: {}", command.getName(), error))),
                error -> log.error("Failed to fetch existing commands", error)
        );
    }
}
