package com.telluur.slapspring.modules.misc.discord.commands.restricted;

import com.telluur.slapspring.abstractions.discord.commands.AbstractRestrictedSlashCommand;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Slf4j
public class UnregisterSlashCommand extends AbstractRestrictedSlashCommand {
    public static final String COMMAND_NAME = "unregister";
    public static final String COMMAND_DESCRIPTION = "Unregisters all commands from global and bound guild.";
    private static final CommandData COMMAND_DATA = Commands.slash(COMMAND_NAME, COMMAND_DESCRIPTION)
            .setGuildOnly(true)
            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));

    @NonNull
    @Override
    public CommandData data() {
        return COMMAND_DATA;
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
